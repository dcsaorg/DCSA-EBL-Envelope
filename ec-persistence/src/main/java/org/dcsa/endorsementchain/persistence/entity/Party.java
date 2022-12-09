package org.dcsa.endorsementchain.persistence.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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

  @Column(name = "lei")
  private String lei;

  @Column(name = "cbsa")
  private String cbsa;

  @Column(name = "fmc")
  private String fmc;

  @Column(name = "exis")
  private String exis;

  @Column(name = "smdg")
  private String smdg;

  @Column(name = "itu")
  private String itu;

  @Column(name = "itigg")
  private String itigg;

  @Column(name = "scac")
  private String scac;

  @Column(name = "imo")
  private String imo;

  @Column(name = "bic")
  private String bic;

  @Column(name = "lloyd")
  private String lloyd;

  @Column(name = "unece")
  private String unece;

  @Column(name = "iso")
  private String iso;
}
