package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PartyTO(
  @NotBlank @Size(max = 255) String id,
  @NotBlank @Size(max = 255) String legalName,
  @NotBlank @Size(max = 255) String registrationNumber,
  @NotBlank @Size(max = 2) String locationOfRegistration,
  @Size(max = 255) String taxReference,
  List<SupportingPartyCodeTO> supportingPartyCodes
) {
  @Builder
  public PartyTO { }
}
