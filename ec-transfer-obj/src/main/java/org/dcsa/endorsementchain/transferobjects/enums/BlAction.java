package org.dcsa.endorsementchain.transferobjects.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum BlAction {
  ISSU("Issue"),
  TRNS("Transfer"),
  SURR("Surrender"),
  SREJ("Surrender Rejected"),
  SREQ("Request to surrender"),
  AREQ("Request to Amend"),
  ;

  @Getter
  private final String value;
}
