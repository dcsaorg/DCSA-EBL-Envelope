package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.Party;
import org.dcsa.endorsementchain.persistence.entity.SupportingPartyCode;
import org.dcsa.endorsementchain.persistence.entity.enums.PartyCodeListProvider;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.dcsa.endorsementchain.transferobjects.SupportingPartyCodeTO;
import org.dcsa.endorsementchain.transferobjects.enums.PartyCodeListProviderTO;

import java.util.List;

@UtilityClass
public class PartyDataFactory {

  public static PartyTO partyTO() {
    return PartyTO.builder()
      .id("test@dcsa-testing.org.invalid:8443")
      .legalName("it's my party and I'll code if I want to")
      .registrationNumber("what a lovely reg number")
      .locationOfRegistration("DK")
      .taxReference("testdata is taxing")
      .supportingPartyCodes(List.of(
        SupportingPartyCodeTO.builder()
          .partyCode("254900G14ALGVKORFN62")
          .partyCodeListProvider(PartyCodeListProviderTO.LEI)
          .build(),
        SupportingPartyCodeTO.builder()
          .partyCode("did:example:123456789abcdefghi")
          .partyCodeListProvider(PartyCodeListProviderTO.DID)
          .build()
      ))
      .build();
  }

  public static Party party() {
    return Party.builder()
      .id("test@dcsa-testing.org.invalid:8443")
      .legalName("it's my party and I'll code if I want to")
      .registrationNumber("what a lovely reg number")
      .locationOfRegistration("DK")
      .taxReference("testdata is taxing")
      .supportingPartyCodes(List.of(
        SupportingPartyCode.builder()
          .partyCode("254900G14ALGVKORFN62")
          .partyCodeListProvider(PartyCodeListProvider.LEI)
          .build(),
        SupportingPartyCode.builder()
          .partyCode("did:example:123456789abcdefghi")
          .partyCodeListProvider(PartyCodeListProvider.DID)
          .build()
      ))
      .build();
  }
}
