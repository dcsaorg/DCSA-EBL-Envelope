package org.dcsa.endorsementchain.transferobjects;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Builder;

import java.util.List;

public record TransferblockTO(
  @JsonRawValue
  String transferDocument,
  List<SignedEblEnvelopeTO> endorcementChain
) {
  @Builder(toBuilder = true)
  public TransferblockTO {}
}
