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

import java.net.URL;
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
                    .envelopeHash(envelope.getEnvelopeHash())
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
  SignedEblEnvelopeTO exportEblEnvelope(
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
            .envelopeHash(signedEblEnvelopeTO.envelopeHash())
            .previousEnvelopeHash(eblEnvelopeTO.previousEblEnvelopeHash())
            .signature(signedEblEnvelopeTO.signature())
            .transportDocument(transportDocument)
            .build();

    eblEnvelopeRepository.save(envelope);

    return signedEblEnvelopeTO;
  }

  public String verifyEblEnvelopeResponseSignature(
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

  void verifyEblEnvelopeSignature(EblEnvelopeTO envelope, SignedEblEnvelopeTO signedEblEnvelopeTO) {
    // Since the platformhost is the host of the originating transaction and all transactions within
    // an EBL envelope are from the same platform we can take any of the transactions to retrieve
    // the platformhost.
    String platformHost = envelope.transactions().get(0).platformHost();
    if (!signature.verifyEnvelope(platformHost, signedEblEnvelopeTO.signature(), signedEblEnvelopeTO.eblEnvelope())) {
      throw ConcreteRequestErrorMessageException.invalidInput("Signature could not be validated");
    }
  }

  EblEnvelopeTO parseEblEnvelope(String eblEnvelope) {
    EblEnvelopeTO parsedEblEnvelope;

    try {
      parsedEblEnvelope = mapper.readValue(eblEnvelope, EblEnvelopeTO.class);
    } catch (JsonProcessingException e) {
      throw ConcreteRequestErrorMessageException.invalidInput(
          "Provided EBL envelope is not valid", e);
    }
    return parsedEblEnvelope;
  }

  String saveEblEnvelopes(List<EblEnvelope> eblEnvelopes) {

    eblEnvelopeRepository.saveAll(eblEnvelopes);
    String envelopeHash =
        EblEnvelopeList.last(eblEnvelopes)
            .map(EblEnvelope::getEnvelopeHash)
            .orElseThrow(
                () ->
                    ConcreteRequestErrorMessageException.internalServerError(
                        "Could not find a Envelope Hash on the EblEnvelope"));

    return signature.signEnvelopeHash(envelopeHash);
  }

  EblEnvelope signedEblEnvelopeToEblEnvelope(
      SignedEblEnvelopeTO signedEblEnvelopeTO,
      EblEnvelopeTO eblEnvelopeTO,
      String transportDocumentJson,
      String platformHost) {

    TransportDocument transportDocument =
      TransportDocument.builder()
        .documentHash(eblEnvelopeTO.documentHash())
        .transportDocumentJson(transportDocumentJson)
        .isExported(false)
        .build();

    Set<Transaction> transactions =
        eblEnvelopeTO.transactions().stream()
            .map(
                endorsementChainTransactionTO ->
                    transactionMapper.endorsementChainTransactionToTransaction(
                        endorsementChainTransactionTO, platformHost))
            .map(transaction -> transaction.linkTransactionToTransportDocument(transportDocument))
            .collect(Collectors.toSet());

    return EblEnvelope.builder()
        .signature(signedEblEnvelopeTO.signature())
        .eblEnvelopeJson(signedEblEnvelopeTO.eblEnvelope())
        .envelopeHash(signedEblEnvelopeTO.envelopeHash())
        .transportDocument(transportDocument)
        .transactions(transactions)
        .previousEnvelopeHash(eblEnvelopeTO.previousEblEnvelopeHash())
        .build();
  }

}
