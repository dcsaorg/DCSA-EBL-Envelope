package org.dcsa.endorcementchain.transferobjects;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Builder;

public record Transferblock(
  @JsonRawValue
  String transferDocument,
  SignedEblEnvelopeTO endorcementChain
) {
  @Builder(toBuilder = true)
  public Transferblock {}
}
