package org.dcsa.endorsementchain.unofficial.datafactories;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.enums.TransactionAction;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.enums.BlAction;

import java.util.List;
import java.util.UUID;

@UtilityClass
public class TransactionDataFactory {

  public Transaction transactionEntity() {
    return Transaction.builder()
      .comments("this is a free text comment")
      .action(TransactionAction.ISSU)
      .isToOrder(true)
      .platformHost("localhost:8443")
      .id(UUID.fromString("326137d8-bd60-4dea-88cc-52687fcb303a"))
      .timestamp(System.currentTimeMillis())
      .transferee("Receiving party")
      .transportDocument(TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions())
      .build();
  }

  public List<Transaction> transactionEntityList() {
    Transaction transaction = Transaction.builder()
      .comments("this is a free text comment")
      .action(TransactionAction.TRNS)
      .isToOrder(true)
      .platformHost("localhost:8443")
      .id(UUID.fromString("cb4c7721-f87c-485e-a6ec-3d0682faa24c"))
      .timestamp(System.currentTimeMillis())
      .transferee("Receiving party")
      .transportDocument(TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions())
      .build();

    return List.of(transaction, TransactionDataFactory.transactionEntity());
  }

  public EndorsementChainTransactionTO endorsementChainTransaction() {
    return EndorsementChainTransactionTO.builder()
      .action(BlAction.ISSU)
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
