package org.dcsa.endorsementchain.transferobjects;

import lombok.Builder;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EndorsementChainEntryTO(
  @NotNull  // TODO: This is wrong; it is nullable in some cases. Will fix in a later commit.
  @Size(max = 64, min = 64)
  String previousEnvelopeHash,  // TODO: Rename when we are ready to change the API
  @NotNull
  @Size(max = 64, min = 64)
  String documentHash,
  @NotEmpty
  List<EndorsementChainTransactionTO> transactions
) {
  @Builder(toBuilder = true)
  public EndorsementChainEntryTO {}
}
