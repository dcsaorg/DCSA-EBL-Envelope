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

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "document_hash", nullable = false)
  private TransportDocument transportDocument;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "envelope_hash")
  private Set<Transaction> transactions;
}
