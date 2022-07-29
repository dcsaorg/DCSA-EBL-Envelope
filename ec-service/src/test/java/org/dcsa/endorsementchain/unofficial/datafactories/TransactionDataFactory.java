package org.dcsa.endorsementchain.unofficial.datafactories;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.enums.TransactionInstruction;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransaction;
import org.dcsa.endorsementchain.transferobjects.enums.BlInstruction;

import java.util.UUID;

@UtilityClass
public class TransactionDataFactory {

  public Transaction transactionEntity() {
    return Transaction.builder()
      .comments("this is a free text comment")
      .instruction(TransactionInstruction.ISSU)
      .isToOrder(true)
      .platformHost("localhost:8443")
      .id(UUID.fromString("326137d8-bd60-4dea-88cc-52687fcb303a"))
      .timestamp(System.currentTimeMillis())
      .transferee("Receiving party")
      .transportDocument(TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions())
      .build();
  }

  public EndorsementChainTransaction endorsementChainTransaction() {
    return EndorsementChainTransaction.builder()
      .instruction(BlInstruction.ISSU)
      .comments("this is a free text comment")
      .isToOrder(true)
      .transferee("Receiving party")
      .build();
  }

  public String endorsementChainTransactionJson() throws JsonProcessingException {

    return """
      {
        "instruction": "ISSU",
        "comments": "The B/L has been issued.",
        "isToOrder": true,
        "transferee": "dummy transferee"
      }
      """;
  }
}
