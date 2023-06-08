package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PartyTO(
  @NotBlank @Size(max = 255) String id,
  @NotBlank @Size(max = 255) String legalName,
  @NotBlank @Size(max = 255) String registrationNumber,
  @NotBlank @Size(max = 2) String locationOfRegistration,
  @Size(max = 255) String taxReference,
  @Size(max = 20, min = 20) String lei,
  @Size(max = 255) String did
) {
  @Builder
  public PartyTO { }
}
