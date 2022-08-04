package org.dcsa.endorsementchain.service;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeList;
import org.dcsa.endorsementchain.mapping.EblEnvelopeMapper;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.repository.EblEnvelopeRepository;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EblEnvelopeService {
  private final EblEnvelopeMapper eblEnvelopeMapper;
  private final EblEnvelopeRepository eblEnvelopeRepository;

 public List<SignedEblEnvelopeTO> convertExistingEblEnvelopesToSignedEnvelopes(List<EblEnvelope> eblEnvelopes) {
    return eblEnvelopes.stream()
      .map(envelope -> SignedEblEnvelopeTO.builder()
        .eblEnvelopeHash(envelope.getEnvelopeHash())
        .signature(envelope.getSignature())
        .eblEnvelope(eblEnvelopeMapper.eblEnvelopeToTo(envelope))
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
    String documentHash, List<EndorsementChainTransactionTO> exportedTransactions, String previousEnvelopeHash) {
    return EblEnvelopeTO.builder()
      .documentHash(documentHash)
      .previousEblEnvelopeHash(previousEnvelopeHash)
      .transactions(exportedTransactions)
      .build();
  }
}
