package org.dcsa.endorsementchain.transferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record SignedEblEnvelopeTO(
  @NotNull
  @Size(max = 64, min = 64)
  String envelopeHash,
  @NotNull
  @JsonRawValue
  String eblEnvelope,
  @NotNull
  String signature
) {
  @Builder(toBuilder = true)
  public SignedEblEnvelopeTO {}

  @JsonCreator
  @Builder(toBuilder = true)
  public SignedEblEnvelopeTO(String envelopeHash, JsonNode eblEnvelope, String signature) {
    this(envelopeHash, eblEnvelope.toString(), signature);
  }
}
