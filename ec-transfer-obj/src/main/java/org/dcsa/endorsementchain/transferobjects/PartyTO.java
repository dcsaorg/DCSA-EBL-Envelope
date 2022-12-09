package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;

public record PartyTO(
  String id,
  String name,
  String registrationNumber,
  String countryOfRegistration,
  String address,
  String taxReference,
  String lei,
  String cbsa,
  String fmc,
  String exis,
  String smdg,
  String itu,
  String itigg,
  String scac,
  String imo,
  String bic,
  String lloyd,
  String unece,
  String iso
) {
  @Builder
  public PartyTO { }
}
