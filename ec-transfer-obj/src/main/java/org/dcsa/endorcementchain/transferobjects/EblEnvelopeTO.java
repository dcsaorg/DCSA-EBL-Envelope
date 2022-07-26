package org.dcsa.endorcementchain.transferobjects;

import lombok.Builder;

import java.util.List;

public record EblEnvelopeTO(
  String previousEblEnvelopeHash,
  String documentHash,
  List<EndorcementChainEntryTO> endorcementChain
) {
  @Builder(toBuilder = true)
  public EblEnvelopeTO{}
}
