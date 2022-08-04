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
      .documentHash("3696079683666fb0214472ca9f3cab4f42d6c9aeb40677c25e7f115d8cea513e")
      .isExported(false)
      .build();
  }

  public TransportDocument exportedTransportDocumentEntityWithoutTransactions() {
    return TransportDocument.builder()
      .transportDocumentJson("{\"test\":\"testvalue\"}")
      .documentHash("3696079683666fb0214472ca9f3cab4f42d6c9aeb40677c25e7f115d8cea513e")
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
    return DigestUtils.sha256Hex("this is just a dummy hash");
  }

  public TransportDocument transportDocumentEntityWithTransactions() {
    return TransportDocument.builder()
      .isExported(false)
      .transactions(new HashSet<>(TransactionDataFactory.transactionEntityList()))
      .transportDocumentJson("{\"test\":\"testvalue\"}")
      .documentHash("3696079683666fb0214472ca9f3cab4f42d6c9aeb40677c25e7f115d8cea513e")
      .build();
  }

}
