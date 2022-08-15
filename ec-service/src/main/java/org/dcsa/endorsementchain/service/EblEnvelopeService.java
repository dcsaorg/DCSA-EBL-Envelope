package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeList;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeSignature;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.EblEnvelopeRepository;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EblEnvelopeService {
  private final EblEnvelopeRepository eblEnvelopeRepository;
  private final EblEnvelopeSignature signature;
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

    SignedEblEnvelopeTO signedEblEnvelopeTO = signature.sign(rawEblEnvelope);

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

  public String verifyResponse(String eblEnvelopeHash, String signatureResponse) {
    return Optional.ofNullable(signatureResponse)
        .map(responseSignature -> signature.verify(responseSignature, eblEnvelopeHash))
        .filter(aBoolean -> aBoolean)
        .map(aBoolean -> signatureResponse)
        .orElseThrow(
            () -> ConcreteRequestErrorMessageException.internalServerError("Signature not valid"));
  }
}
