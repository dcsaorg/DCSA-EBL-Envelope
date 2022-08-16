package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.enums.BlInstruction;

import java.util.List;

@UtilityClass
public class EndorsementChainTransactionTODataFactory {

  public EndorsementChainTransactionTO endorsementChainTransactionTO() {
    return EndorsementChainTransactionTO.builder()
      .comments("This is a freetext comment")
      .instruction(BlInstruction.SURR)
      .isToOrder(false)
      .platformHost("localhost:8443")
      .timestamp(System.currentTimeMillis())
      .transferee("Transferee@localhost:8443")
      .build();
  }

  public List<EndorsementChainTransactionTO> endorsementChainTransactionTOList() {
    EndorsementChainTransactionTO initialTransaction = EndorsementChainTransactionTO.builder()
      .comments("The B/L is issued")
      .instruction(BlInstruction.ISSU)
      .isToOrder(false)
      .platformHost("localhost:8443")
      .timestamp(System.currentTimeMillis())
      .transferee("initialTransferee@localhost:8443")
      .build();

    return List.of(initialTransaction, EndorsementChainTransactionTODataFactory.endorsementChainTransactionTO());
  }
}
