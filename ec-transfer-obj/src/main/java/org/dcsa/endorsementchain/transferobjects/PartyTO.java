package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PartyTO(
  @NotBlank @Size(max = 255) String id,
  @NotBlank @Size(max = 255) String name,
  @NotBlank @Size(max = 255) String registrationNumber,
  @NotBlank @Size(max = 2) String countryOfRegistration,
  @Size(max = 1024) String address,
  @Size(max = 255) String taxReference,
  @Size(max = 255) String lei,
  @Size(max = 255) String cbsa,
  @Size(max = 255) String fmc,
  @Size(max = 255) String exis,
  @Size(max = 255) String smdg,
  @Size(max = 255) String itu,
  @Size(max = 255) String itigg,
  @Size(max = 255) String scac,
  @Size(max = 255) String imo,
  @Size(max = 255) String bic,
  @Size(max = 255) String lloyd,
  @Size(max = 255) String unece,
  @Size(max = 255) String iso
) {
  @Builder
  public PartyTO { }
}
