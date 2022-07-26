package org.dcsa.endorcementchain.transferobjects;

import lombok.Builder;

public record SignedEblEnvelopeTO(
  String eblEnvelopeHash,
  String signature,
  EblEnvelopeTO eblEnvelope
) {
  @Builder(toBuilder = true)
  public SignedEblEnvelopeTO {}
}
