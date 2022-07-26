package org.dcsa.endorcementchain.transferobjects;

import lombok.Builder;
import org.dcsa.endorcementchain.transferobjects.enums.BlInstruction;

public record EndorcementChainEntryTO(
  BlInstruction instruction,
  String comments,
  String transferee, //ToDo must be a (subset of) the party object
  Long timestamp,
  Boolean isToOrder,
  String platformHost
) {
  @Builder(toBuilder = true)
  public EndorcementChainEntryTO{}
}
