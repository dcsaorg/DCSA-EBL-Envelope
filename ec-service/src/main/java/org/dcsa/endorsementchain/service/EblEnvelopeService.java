package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeList;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeSignature;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.EblEnvelopeRepository;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EblEnvelopeService {
  private final PartyService partyService;
  private final EblEnvelopeRepository eblEnvelopeRepository;
  private final EblEnvelopeSignature signature;
  private final TransactionMapper transactionMapper;
  private final TransportDocumentRepository transportDocumentRepository;
  private final ObjectMapper mapper;

  public List<SignedEblEnvelopeTO> convertExistingEblEnvelopesToSignedEnvelopes(
      List<EblEnvelope> eblEnvelopes) {
    return eblEnvelopes.stream()
        .map(
            envelope ->
                SignedEblEnvelopeTO.builder()
                    .envelopeHash(envelope.getEnvelopeHash())
                    .signature(envelope.getSignature())
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
        .previousEnvelopeHash(previousEnvelopeHash)
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

    SignedEblEnvelopeTO signedEblEnvelopeTO = signature.createSignedEblEnvelope(rawEblEnvelope);

    EblEnvelope envelope =
        EblEnvelope.builder()
            .envelopeHash(signedEblEnvelopeTO.envelopeHash())
            .previousEnvelopeHash(eblEnvelopeTO.previousEnvelopeHash())
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

  @SneakyThrows
  EblEnvelopeTO verifyEndorsementChainSignature(String parsedSignature) {

    JWSObject jwsObject = null;
    try {
      jwsObject = JWSObject.parse(parsedSignature);
    } catch (ParseException e) {
      throw ConcreteRequestErrorMessageException.invalidInput("Provided EBL envelope is not valid");
    }

    EblEnvelopeTO parsedEblEnvelope = mapper.convertValue(jwsObject.getPayload().toJSONObject(), EblEnvelopeTO.class);
    String platformHost = parsedEblEnvelope.transactions().get(0).platformHost();
    if (!signature.verifySignature(platformHost, jwsObject)) {
      throw ConcreteRequestErrorMessageException.invalidInput("Signature could not be validated");
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

    return signature.sign(envelopeHash);
  }

  EblEnvelope signedEblEnvelopeToEblEnvelope(
      SignedEblEnvelopeTO signedEblEnvelopeTO,
      EblEnvelopeTO eblEnvelopeTO,
      String transportDocumentJson,
      String platformHost) {

    TransportDocument transportDocument = transportDocumentRepository.findById(eblEnvelopeTO.documentHash())
      .map(td -> {
        td.reimported();
        return td;
      })
      .orElseGet(() -> TransportDocument.builder()
        .documentHash(eblEnvelopeTO.documentHash())
        .transportDocumentJson(transportDocumentJson)
        .isExported(false)
        .build()
      );

    Set<Transaction> transactions =
        eblEnvelopeTO.transactions().stream()
            .map(
                endorsementChainTransactionTO ->
                    transactionMapper.endorsementChainTransactionToTransaction(
                        endorsementChainTransactionTO, platformHost, partyService.getPartyByTransferee(endorsementChainTransactionTO.transferee())))
            .map(transaction -> transaction.linkTransactionToTransportDocument(transportDocument))
            .collect(Collectors.toSet());

    return EblEnvelope.builder()
        .signature(signedEblEnvelopeTO.signature())
        .envelopeHash(signedEblEnvelopeTO.envelopeHash())
        .transportDocument(transportDocument)
        .transactions(transactions)
        .previousEnvelopeHash(eblEnvelopeTO.previousEnvelopeHash())
        .build();
  }

}
