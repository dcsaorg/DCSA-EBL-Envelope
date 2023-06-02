package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.dcsa.endorsementchain.components.endorsementchain.EndorsementChainEntryList;
import org.dcsa.endorsementchain.components.endorsementchain.EndorsementChainEntrySignature;
import org.dcsa.endorsementchain.components.jws.SignatureVerifier;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.persistence.entity.TransactionByTimestampComparator;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.EndorsementChainEntryRepository;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EndorsementChainEntryService {
  private final PartyService partyService;
  private final EndorsementChainEntryRepository endorsementChainEntryRepository;
  private final EndorsementChainEntrySignature signature;
  private final SignatureVerifier signatureVerifier;
  private final TransactionMapper transactionMapper;
  private final TransportDocumentRepository transportDocumentRepository;
  private final ObjectMapper mapper;

  public List<SignedEndorsementChainEntryTO> convertExistingEndorsementChainEntriesToSignedEntries(
      List<EndorsementChainEntry> endorsementChainEntries) {
    return endorsementChainEntries.stream()
        .map(
            envelope ->
                SignedEndorsementChainEntryTO.builder()
                    .envelopeHash(envelope.getEnvelopeHash())
                    .signature(envelope.getSignature())
                    .build())
        .toList();
  }

  public List<EndorsementChainEntry> findPreviousEndorsementChainEntries(String documentHash) {
    return endorsementChainEntryRepository
        .findByTransportDocument_DocumentHash(documentHash)
        .orElse(Collections.emptyList());
  }

  public String findPreviousEndorsementChainEntryHash(List<EndorsementChainEntry> endorsementChainEntries) {
    return EndorsementChainEntryList.last(endorsementChainEntries)
        .map(EndorsementChainEntry::getPreviousEnvelopeHash)
        .orElse(null);
  }

  public EndorsementChainEntryTO createEndorsementChainEntry(
      String documentHash,
      List<EndorsementChainTransactionTO> exportedTransactions,
      String previousEnvelopeHash) {
    return EndorsementChainEntryTO.builder()
        .documentHash(documentHash)
        .previousEnvelopeHash(previousEnvelopeHash)
        .transactions(exportedTransactions)
        .build();
  }

  @SneakyThrows
  SignedEndorsementChainEntryTO exportEndorsementChainEntry(
      TransportDocument transportDocument, EndorsementChainEntryTO endorsementChainEntryTO) {

    if (!endorsementChainEntryTO.documentHash().equals(transportDocument.getDocumentHash())) {
      throw ConcreteRequestErrorMessageException.internalServerError(
          "EndorsementChainEntry refers to a different transportDocument.");
    }

    String rawEndorsementChainEntry = mapper.writeValueAsString(endorsementChainEntryTO);

    SignedEndorsementChainEntryTO signedEndorsementChainEntryTO = signature.createSignedEndorsementChainEntry(rawEndorsementChainEntry);

    EndorsementChainEntry envelope =
        EndorsementChainEntry.builder()
            .envelopeHash(signedEndorsementChainEntryTO.envelopeHash())
            .previousEnvelopeHash(endorsementChainEntryTO.previousEnvelopeHash())
            .signature(signedEndorsementChainEntryTO.signature())
            .transportDocument(transportDocument)
            .build();

    endorsementChainEntryRepository.save(envelope);

    return signedEndorsementChainEntryTO;
  }

  public String verifyEndorsementChainEntryResponseSignature(
      String platformHost, String endorsementChainEntryHash, String signatureResponse) {
    return Optional.ofNullable(signatureResponse)
        .map(
            responseSignature ->
                signature.verifyEndorsementChainHash(platformHost, responseSignature, endorsementChainEntryHash))
        .filter(aBoolean -> aBoolean)
        .map(aBoolean -> signatureResponse)
        .orElseThrow(
            () -> ConcreteRequestErrorMessageException.internalServerError("Signature not valid"));
  }

  @SneakyThrows
  EndorsementChainEntryTO verifyEndorsementChainSignature(String parsedSignature) {

    JWSObject jwsObject = null;
    try {
      jwsObject = JWSObject.parse(parsedSignature);
    } catch (ParseException e) {
      throw ConcreteRequestErrorMessageException.invalidInput("Provided EBL envelope is not valid");
    }

    EndorsementChainEntryTO parsedEndorsementEntries = mapper.convertValue(jwsObject.getPayload().toJSONObject(), EndorsementChainEntryTO.class);
    String platformHost = parsedEndorsementEntries.transactions().get(0).platformHost();
    if (!signatureVerifier.verifySignature(platformHost, jwsObject)) {
      throw ConcreteRequestErrorMessageException.invalidInput("Signature could not be validated");
    }
    return parsedEndorsementEntries;
  }

  String saveEndorsementEntries(List<EndorsementChainEntry> endorsementChainEntries) {

    endorsementChainEntryRepository.saveAll(endorsementChainEntries);
    String envelopeHash =
        EndorsementChainEntryList.last(endorsementChainEntries)
            .map(EndorsementChainEntry::getEnvelopeHash)
            .orElseThrow(
                () ->
                    ConcreteRequestErrorMessageException.internalServerError(
                        "Could not find a Envelope Hash on the EndorsementChainEntry"));

    return signature.sign(envelopeHash);
  }

  EndorsementChainEntry signedEndorsementEntryToEndorsementChainEntry(
      SignedEndorsementChainEntryTO signedEndorsementChainEntryTO,
      EndorsementChainEntryTO endorsementChainEntryTO,
      String transportDocumentJson,
      String platformHost) {

    TransportDocument transportDocument = transportDocumentRepository.findById(endorsementChainEntryTO.documentHash())
      .map(td -> {
        td.reimported();
        return td;
      })
      .orElseGet(() -> TransportDocument.builder()
        .documentHash(endorsementChainEntryTO.documentHash())
        .transportDocumentJson(transportDocumentJson)
        .isExported(false)
        .build()
      );

    var transactions =
        endorsementChainEntryTO.transactions().stream()
            .map(
                endorsementChainTransactionTO ->
                    transactionMapper.endorsementChainTransactionToTransaction(
                        endorsementChainTransactionTO, platformHost, partyService.getPartyByTransferee(endorsementChainTransactionTO.transferee())))
            .map(transaction -> transaction.linkTransactionToTransportDocument(transportDocument))
            .collect(Collectors.toCollection(() -> new TreeSet<>(TransactionByTimestampComparator.INSTANCE)));

    return EndorsementChainEntry.builder()
        .signature(signedEndorsementChainEntryTO.signature())
        .envelopeHash(signedEndorsementChainEntryTO.envelopeHash())
        .transportDocument(transportDocument)
        .transactions(transactions)
        .previousEnvelopeHash(endorsementChainEntryTO.previousEnvelopeHash())
        .build();
  }

}
