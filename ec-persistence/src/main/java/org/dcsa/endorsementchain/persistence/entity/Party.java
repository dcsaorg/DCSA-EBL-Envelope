package org.dcsa.endorsementchain.persistence.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
  private String name;

  @Column(name = "registration_number", nullable = false)
  private String registrationNumber;

  @Column(name = "country_of_registration", nullable = false, length = 2)
  private String countryOfRegistration;

  @Column(name = "address")
  private String address;

  @Column(name = "tax_reference")
  private String taxReference;

  @Column(name = "lei", length = 20)
  private String lei;

  @Column(name = "did")
  private String did;
}
