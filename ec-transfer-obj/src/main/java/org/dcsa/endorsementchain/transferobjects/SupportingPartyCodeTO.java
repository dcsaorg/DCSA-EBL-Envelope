package org.dcsa.endorsementchain.transferobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.dcsa.endorsementchain.transferobjects.enums.PartyCodeListProviderTO;

public record SupportingPartyCodeTO(
  @Pattern(regexp = "^\\S+(\\s*\\S+)*$")
  @NotBlank
  String partyCode,
  @NotNull
  PartyCodeListProviderTO partyCodeListProvider
) {
  @Builder
  public SupportingPartyCodeTO { }
}
