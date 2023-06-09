package org.dcsa.endorsementchain.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dcsa.endorsementchain.persistence.entity.enums.PartyCodeListProvider;

import java.util.UUID;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class SupportingPartyCode {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "party_id", nullable = false)
  private String partyId;

  @Pattern(regexp = "^\\S+(\\s*\\S+)*$")
  @NotBlank
  private String partyCode;

  @Enumerated(EnumType.STRING)
  @NotNull
  private PartyCodeListProvider partyCodeListProvider;
}
