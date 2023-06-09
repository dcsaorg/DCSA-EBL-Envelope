package org.dcsa.endorsementchain.persistence.entity.enums;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PartyCodeListProvider {
  DID("Decentralized Identifier"),
  LEI("Legal Entity Identifier"),
  ;

  @Getter
  private final String value;
}
