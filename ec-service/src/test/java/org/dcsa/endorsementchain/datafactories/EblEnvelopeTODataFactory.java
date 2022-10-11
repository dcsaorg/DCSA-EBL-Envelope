package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;

import java.util.List;

@UtilityClass
public class EblEnvelopeTODataFactory {

  public EblEnvelopeTO eblEnvelopeTO() {
    return EblEnvelopeTO.builder()
      .transactions(EndorsementChainTransactionTODataFactory.endorsementChainTransactionTOList())
      .documentHash(TransportDocumentDataFactory.transportDocumentEntityWithTransactions().getDocumentHash())
      .previousEnvelopeHash(null)
      .transactions(EndorsementChainTransactionTODataFactory.endorsementChainTransactionTOList())
      .build();
  }

  public List<EblEnvelopeTO> eblEnvelopeTOList() {
    EblEnvelopeTO secondEblEnvelope = EblEnvelopeTO.builder()
      .previousEnvelopeHash("a25286672be331c6770fa590f8eb7ab7cf105fd76f0db4b7cabd258a5953482e")
      .documentHash(TransportDocumentDataFactory.transportDocumentHash())
      .transactions(EndorsementChainTransactionTODataFactory.endorsementChainTransactionTOList())
      .build();

    return List.of(EblEnvelopeTODataFactory.eblEnvelopeTO(), secondEblEnvelope);
  }

}
