package org.dcsa.endorsementchain.transferobjects;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record SignedEblEnvelopeTO(
  @NotNull
  @Size(max = 64, min = 64)
  String eblEnvelopeHash,
  @NotNull
  @JsonRawValue
  String eblEnvelope,
  @NotNull
  String signature
) {
  @Builder(toBuilder = true)
  public SignedEblEnvelopeTO {}
}
