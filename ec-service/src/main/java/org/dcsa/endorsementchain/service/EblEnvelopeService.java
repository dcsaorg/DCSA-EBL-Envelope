package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeList;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeSignature;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.EblEnvelopeRepository;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EblEnvelopeService {
  private final EblEnvelopeRepository eblEnvelopeRepository;
  private final EblEnvelopeSignature signature;
  private final TransactionMapper transactionMapper;
  private final ObjectMapper mapper;

  public List<SignedEblEnvelopeTO> convertExistingEblEnvelopesToSignedEnvelopes(
      List<EblEnvelope> eblEnvelopes) {
    return eblEnvelopes.stream()
        .map(
            envelope ->
                SignedEblEnvelopeTO.builder()
                    .eblEnvelopeHash(envelope.getEnvelopeHash())
                    .signature(envelope.getSignature())
                    .eblEnvelope(envelope.getEblEnvelopeJson())
                    .build())
        .toList();
  }

  public List<EblEnvelope> findPreviousEblEnvelopes(String documentHash) {
    return eblEnvelopeRepository
        .findByTransportDocument_DocumentHash(documentHash)
        .orElse(Collections.emptyList());
  }

  public String findPreviousEblEnvelopeHash(List<EblEnvelope> eblEnvelopes) {
    return EblEnvelopeList.last(eblEnvelopes)
        .map(EblEnvelope::getPreviousEnvelopeHash)
        .orElse(null);
  }

  public EblEnvelopeTO createEblEnvelope(
      String documentHash,
      List<EndorsementChainTransactionTO> exportedTransactions,
      String previousEnvelopeHash) {
    return EblEnvelopeTO.builder()
        .documentHash(documentHash)
        .previousEblEnvelopeHash(previousEnvelopeHash)
        .transactions(exportedTransactions)
        .build();
  }

  @SneakyThrows
  public SignedEblEnvelopeTO exportEblEnvelope(
      TransportDocument transportDocument, EblEnvelopeTO eblEnvelopeTO) {

    if (!eblEnvelopeTO.documentHash().equals(transportDocument.getDocumentHash())) {
      throw ConcreteRequestErrorMessageException.internalServerError(
          "EblEnvelope refers to a different transportDocument.");
    }

    String rawEblEnvelope = mapper.writeValueAsString(eblEnvelopeTO);

    SignedEblEnvelopeTO signedEblEnvelopeTO = signature.signEnvelope(rawEblEnvelope);

    EblEnvelope envelope =
        EblEnvelope.builder()
            .eblEnvelopeJson(rawEblEnvelope)
            .envelopeHash(signedEblEnvelopeTO.eblEnvelopeHash())
            .previousEnvelopeHash(eblEnvelopeTO.previousEblEnvelopeHash())
            .signature(signedEblEnvelopeTO.signature())
            .transportDocument(transportDocument)
            .build();

    eblEnvelopeRepository.save(envelope);

    return signedEblEnvelopeTO;
  }

  public String verifyResponse(
      String platformHost, String eblEnvelopeHash, String signatureResponse) {
    return Optional.ofNullable(signatureResponse)
        .map(
            responseSignature ->
                signature.verifyEnvelopeHash(platformHost, responseSignature, eblEnvelopeHash))
        .filter(aBoolean -> aBoolean)
        .map(aBoolean -> signatureResponse)
        .orElseThrow(
            () -> ConcreteRequestErrorMessageException.internalServerError("Signature not valid"));
  }

  public EblEnvelope verifyEnvelopeSignature(SignedEblEnvelopeTO signedEblEnvelope) {
    String envelopeSignature = signedEblEnvelope.signature();
    String eblEnvelope = signedEblEnvelope.eblEnvelope();
    EblEnvelopeTO parsedEblEnvelope;
    try {
      parsedEblEnvelope = mapper.readValue(eblEnvelope, EblEnvelopeTO.class);
    } catch (JsonProcessingException e) {
      throw ConcreteRequestErrorMessageException.invalidInput(
          "Provided EBL envelope is not valid", e);
    }

    // Since the platformhost is the host of the originating transaction and all transactions within
    // an EBL envelope are from the same platform we can take any of the transactions to retrieve
    // the platformhost.
    String platformHost = parsedEblEnvelope.transactions().get(0).platformHost();
    if (!signature.verifyEnvelope(platformHost, envelopeSignature, eblEnvelope)) {
      throw ConcreteRequestErrorMessageException.invalidInput("Signature could not be validated");
    }
    return signedEblEnvelopeToEblEnvelope(signedEblEnvelope, parsedEblEnvelope, platformHost);
  }

  public String saveEblEnvelopes(List<EblEnvelope> eblEnvelopes) {

    List<EblEnvelope> savedEblEnvelopes = eblEnvelopeRepository.saveAll(eblEnvelopes);

    String envelopeHash =
        EblEnvelopeList.last(savedEblEnvelopes)
            .map(EblEnvelope::getEnvelopeHash)
            .orElseThrow(
                () ->
                    ConcreteRequestErrorMessageException.internalServerError(
                        "Could not find a Envelope Hash on the EblEnvelope"));

    return signature.signEnvelopeHash(envelopeHash);
  }

  private EblEnvelope signedEblEnvelopeToEblEnvelope(
      SignedEblEnvelopeTO signedEblEnvelopeTO, EblEnvelopeTO eblEnvelopeTO, String platformHost) {

    Set<Transaction> transactions =
        eblEnvelopeTO.transactions().stream()
            .map(
                endorsementChainTransactionTO ->
                    transactionMapper.endorsementChainTransactionToTransaction(
                        endorsementChainTransactionTO, platformHost))
            .collect(Collectors.toSet());

    return EblEnvelope.builder()
        .signature(signedEblEnvelopeTO.signature())
        .eblEnvelopeJson(signedEblEnvelopeTO.eblEnvelope())
        .envelopeHash(signedEblEnvelopeTO.eblEnvelopeHash())
        .transactions(transactions)
        .previousEnvelopeHash(eblEnvelopeTO.previousEblEnvelopeHash())
        .build();
  }
}
