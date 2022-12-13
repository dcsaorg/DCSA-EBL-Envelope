package org.dcsa.endorsementchain.transferobjects.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum BlAction {
  ISSU("Issue"),
  TRNS("Transfer"),
  SURR("Surrender"),
  AMND("Amend"),
  SW2P("Switch to paper"),
  SREQ("Request to surrender"),
  AREQ("Request to Amend"),
  PREQ("Request to switch to paper")
  ;

  @Getter
  private final String value;
}
