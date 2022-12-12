package org.dcsa.endorsementchain.transferobjects;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import org.dcsa.endorsementchain.transferobjects.enums.BlAction;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record EndorsementChainTransactionTO(
  @NotNull
  @JsonAlias("instruction")
  BlAction action,

  @Size(max = 255)
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
  public EndorsementChainTransactionTO {}
}
