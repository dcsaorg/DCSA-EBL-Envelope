package org.dcsa.endorsementchain.transferobjects.enums;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PartyCodeListProviderTO {
  DID("Decentralized Identifier"),
  LEI("Legal Entity Identifier"),
  ;

  @Getter
  private final String value;
}
