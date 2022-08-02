package org.dcsa.endorsementchain.persistence.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransactionInstruction {
  ISSU("Issue"),
  TRNS("Transfer"),
  SURR("Surrender"),
  AMND("Ammend"),
  SW2P("Switch to paper");
  private final String value;
}
