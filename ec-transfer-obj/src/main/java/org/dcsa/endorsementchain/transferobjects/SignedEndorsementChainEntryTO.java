package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignedEblEnvelopeTO(
  @NotNull
  @Size(max = 64, min = 64)
  String envelopeHash,

  @NotNull
  String signature
) {
  @Builder(toBuilder = true)
  public SignedEblEnvelopeTO {}

}
