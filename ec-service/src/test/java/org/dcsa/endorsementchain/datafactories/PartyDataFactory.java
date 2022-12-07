package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.Party;
import org.dcsa.endorsementchain.transferobjects.PartyTO;

@UtilityClass
public class PartyDataFactory {

  public static PartyTO partyTO() {
    return PartyTO.builder()
      .id("test@dcsa-testing.org.invalid:8443")
      .name("it's my party and I'll code if I want to")
      .registrationNumber("what a lovely reg number")
      .countryOfRegistration("DK")
      .address("Somewhere 314, Totally Real Town, DK")
      .taxReference("testdata is taxing")
      .lei("lei")
      .cbsa("cbsa")
      .fmc("fmc")
      .exis("t")
      .smdg("smdg")
      .itu("what tu?")
      .itigg("itigg")
      .scac("cacs")
      .imo("asdfgh")
      .bic("cus dikkus")
      .lloyd("dyoll")
      .unece("ecenu")
      .iso("8859-1")
      .build();
  }

  public static Party party() {
    return Party.builder()
      .id("test@dcsa-testing.org.invalid:8443")
      .name("it's my party and I'll code if I want to")
      .registrationNumber("what a lovely reg number")
      .countryOfRegistration("DK")
      .address("Somewhere 314, Totally Real Town, DK")
      .taxReference("testdata is taxing")
      .lei("lei")
      .cbsa("cbsa")
      .fmc("fmc")
      .exis("t")
      .smdg("smdg")
      .itu("what tu?")
      .itigg("itigg")
      .scac("cacs")
      .imo("asdfgh")
      .bic("cus dikkus")
      .lloyd("dyoll")
      .unece("ecenu")
      .iso("8859-1")
      .build();
  }
}
