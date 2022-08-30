package org.dcsa.endorsementchain.unofficial.datafactories;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;

import java.util.HashSet;

@UtilityClass
public class TransportDocumentDataFactory {

  public TransportDocument transportDocumentEntityWithoutTransactions() {
    return TransportDocument.builder()
      .transportDocumentJson("{\"test\":\"testvalue\"}")
      .documentHash("21a9c5f49bfc7248fecbeba49e0b3414376e0c77a84ce1dae383dccd66f03000")
      .isExported(false)
      .build();
  }

  public TransportDocument exportedTransportDocumentEntityWithoutTransactions() {
    return TransportDocument.builder()
      .transportDocumentJson("{\"test\":\"testvalue\"}")
      .documentHash("21a9c5f49bfc7248fecbeba49e0b3414376e0c77a84ce1dae383dccd66f03000")
      .isExported(true)
      .build();
  }

  public TransportDocument transportDocumentEntityWithoutDocumentHash() {
    return TransportDocument.builder()
      .transportDocumentJson("{\"test\":\"testvalue\"}")
      .isExported(false)
      .build();
  }

  public String transportDocumentHash() {
    return DigestUtils.sha256Hex("{\"test\":\"testvalue\"}");
  }

  public TransportDocument transportDocumentEntityWithTransactions() {
    return TransportDocument.builder()
      .isExported(false)
      .transactions(new HashSet<>(TransactionDataFactory.transactionEntityList()))
      .transportDocumentJson("{\"test\":\"testvalue\"}")
      .documentHash("21a9c5f49bfc7248fecbeba49e0b3414376e0c77a84ce1dae383dccd66f03000")
      .build();
  }

}
