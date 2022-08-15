package org.dcsa.endorsementchain.datafactories;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
      .previousEblEnvelopeHash(null)
      .build();
  }

  public List<EblEnvelopeTO> eblEnvelopeTOList() {
    EblEnvelopeTO secondEblEnvelope = EblEnvelopeTO.builder()
      .previousEblEnvelopeHash("a7fd75ab75107c4c01aa71b42517a902710cd37a921514e48d798a32b6d0ebd0")
      .documentHash(TransportDocumentDataFactory.transportDocumentHash())
      .transactions(EndorsementChainTransactionTODataFactory.endorsementChainTransactionTOList())
      .build();

    return List.of(EblEnvelopeTODataFactory.eblEnvelopeTO(), secondEblEnvelope);
  }

  @SneakyThrows
  public String rawEblEnvelope() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(eblEnvelopeTO());
  }
}
