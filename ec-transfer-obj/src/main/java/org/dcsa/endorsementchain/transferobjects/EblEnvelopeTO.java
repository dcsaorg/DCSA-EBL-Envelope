package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public record EblEnvelopeTO(
  @NotNull
  @Size(max = 64, min = 64)
  String previousEnvelopeHash,
  @NotNull
  @Size(max = 64, min = 64)
  String documentHash,
  @NotEmpty
  List<EndorsementChainTransactionTO> transactions
) {
  @Builder(toBuilder = true)
  public EblEnvelopeTO{}
}
