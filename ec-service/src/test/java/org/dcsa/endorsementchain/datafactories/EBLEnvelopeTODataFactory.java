package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.transferobjects.EBLEnvelopeTO;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;

@UtilityClass
public class EBLEnvelopeTODataFactory {

  public EBLEnvelopeTO eblEnvelopeTO() {
    return EBLEnvelopeTO.builder()
        .document(TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions().getTransportDocumentJson())
        .endorsementChain(
            SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTOList())
        .build();
  }

  public EBLEnvelopeTO eblEnvelopeTO(String rawEnvelope, String secondRawEnvelope) {
    // TODO: Both parameters are unused!?
    return EBLEnvelopeTO.builder()
      .document(TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions().getTransportDocumentJson())
      .endorsementChain(
        SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTOList())
      .build();
  }
}
