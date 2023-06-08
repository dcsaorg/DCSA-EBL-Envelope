package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.Party;
import org.dcsa.endorsementchain.transferobjects.PartyTO;

@UtilityClass
public class PartyDataFactory {

  public static PartyTO partyTO() {
    return PartyTO.builder()
      .id("test@dcsa-testing.org.invalid:8443")
      .legalName("it's my party and I'll code if I want to")
      .registrationNumber("what a lovely reg number")
      .locationOfRegistration("DK")
      .taxReference("testdata is taxing")
      .lei("254900G14ALGVKORFN62")
      .did("did:example:123456789abcdefghi")
      .build();
  }

  public static Party party() {
    return Party.builder()
      .id("test@dcsa-testing.org.invalid:8443")
      .legalName("it's my party and I'll code if I want to")
      .registrationNumber("what a lovely reg number")
      .locationOfRegistration("DK")
      .taxReference("testdata is taxing")
      .lei("254900G14ALGVKORFN62")
      .did("did:example:123456789abcdefghi")
      .build();
  }
}
