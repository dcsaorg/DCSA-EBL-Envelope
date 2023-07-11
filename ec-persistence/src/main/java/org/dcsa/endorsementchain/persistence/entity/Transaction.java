package org.dcsa.endorsementchain.persistence.entity;

import lombok.*;
import org.dcsa.endorsementchain.persistence.entity.enums.TransactionAction;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;

import jakarta.persistence.*;
import java.util.UUID;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
//only one controller can make a transaction at a time
@Table(name = "transaction", uniqueConstraints = { @UniqueConstraint(name = "UniqueTimestampAndDocumentHash", columnNames = { "timestamp", "document_hash" }) })
public class Transaction {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  private UUID id;

  @JoinColumn(name = "document_hash", nullable = false)
  @ManyToOne
  private TransportDocument transportDocument;

  @Column(name = "envelope_hash", length = 64)
  String envelopeHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", length = 4, nullable = false)
  private TransactionAction action;

  @Column(name = "comments")
  private String comments;

  @Column(name = "timestamp", nullable = false)
  private Long timestamp;

  private String platformHost;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "transferee")
  private Party party;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "actor")
  private Party actor;

  public Transaction linkTransactionToTransportDocument(TransportDocument transportDocument) {
    if (Boolean.TRUE.equals(transportDocument.getIsExported())) {
      throw ConcreteRequestErrorMessageException.internalServerError("Cannot link a transaction to an exported transportDocument");
    }
    this.setTransportDocument(transportDocument);
    return this;
  }

}
