package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignedEndorsementChainEntryTO(
  @NotNull
  @Size(max = 64, min = 64)
  String envelopeHash,  // TODO: Rename when we are ready to change the API

  @NotNull
  String signature
) {
  @Builder(toBuilder = true)
  public SignedEndorsementChainEntryTO {}

}
