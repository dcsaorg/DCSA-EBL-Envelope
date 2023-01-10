package org.dcsa.endorsementchain.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SortComparator;

import java.util.Comparator;
import java.util.SortedSet;

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
  @SortComparator(TransactionByTimestampComparator.class)
  private SortedSet<Transaction> transactions;
}
