package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;
import org.dcsa.endorsementchain.transferobjects.enums.BlInstruction;

import javax.validation.constraints.NotNull;

public record EndorsementChainTransaction(
  @NotNull
  BlInstruction instruction,
  String comments,
  @NotNull
  Long timestamp,
  @NotNull
  Boolean isToOrder,
  @NotNull
  String platformHost,
  @NotNull
  String transferee //ToDo must be a (subset of) the party object
) {
  @Builder(toBuilder = true)
  public EndorsementChainTransaction {}
}
