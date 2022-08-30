package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.transferobjects.TransferblockTO;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;

@UtilityClass
public class TransferblockTODataFactory {

  public TransferblockTO transferblockTO() {
    return TransferblockTO.builder()
        .document(TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions().getTransportDocumentJson())
        .endorsementChain(
            SignedEblEnvelopeTODataFactory.signedEblEnvelopeTOList(
                EblEnvelopeDataFactory.getEblEnvelopeList().get(0).getEblEnvelopeJson(),
                EblEnvelopeDataFactory.getEblEnvelopeList().get(1).getEblEnvelopeJson()))
        .build();
  }

  public TransferblockTO transferblockTO(String rawEnvelope, String secondRawEnvelope) {
    return TransferblockTO.builder()
      .document(TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions().getTransportDocumentJson())
      .endorsementChain(
        SignedEblEnvelopeTODataFactory.signedEblEnvelopeTOList(
          rawEnvelope,
          secondRawEnvelope))
      .build();
  }
}
