package org.dcsa.endorsementchain.persistence.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Table(name = "ebl_envelope")
public class EblEnvelope {

  @Id
  @Column(name = "envelope_hash", length = 64, nullable = false)
  private String envelopeHash;

  @Column(name = "previous_envelope_hash", length = 64, unique = true)
  private String previousEnvelopeHash;

  @Column(name = "signature", nullable = false, columnDefinition = "TEXT")
  private String signature;

  @Column(name = "ebl_envelope_json", columnDefinition = "TEXT")
  private String eblEnvelopeJson;

  @ManyToOne
  @JoinColumn(name = "document_hash", nullable = false)
  private TransportDocument transportDocument;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "eblEnvelope")
  private Set<Transaction> transactions;
}
