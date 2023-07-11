package org.dcsa.endorsementchain.persistence.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class Party {

  @Id
  @Column(name = "ebl_platform_identifier", nullable = false)
  private String id;

  @Column(name = "party_name", nullable = false)
  private String legalName;

  @Column(name = "registration_number", nullable = false)
  private String registrationNumber;

  @Column(name = "location_of_registration", nullable = false, length = 2)
  private String locationOfRegistration;

  @Column(name = "tax_reference")
  private String taxReference;

  @OneToMany
  @JoinColumn(name = "party_id", referencedColumnName = "ebl_platform_identifier")
  private List<SupportingPartyCode> supportingPartyCodes;
}
