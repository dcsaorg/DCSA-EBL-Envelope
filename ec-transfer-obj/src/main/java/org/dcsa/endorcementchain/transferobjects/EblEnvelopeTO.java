package org.dcsa.endorcementchain.transferobjects;

import lombok.Builder;

import java.util.List;

public record EblEnvelopeTO(
  String previousEblEnvelopeHash,
  String documentHash,
  List<EndorcementChainTransaction> transactions
) {
  @Builder(toBuilder = true)
  public EblEnvelopeTO{}
}
