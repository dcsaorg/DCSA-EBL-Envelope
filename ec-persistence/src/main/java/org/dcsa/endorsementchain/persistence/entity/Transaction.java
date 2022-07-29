package org.dcsa.endorsementchain.persistence.entity;

import lombok.*;
import org.dcsa.endorsementchain.persistence.entity.enums.TransactionInstruction;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;

import javax.persistence.*;
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

  @Enumerated(EnumType.STRING)
  @Column(name = "instruction", length = 4, nullable = false)
  private TransactionInstruction instruction;

  @Column(name = "comments")
  private String comments;

  @Column(name = "timestamp", nullable = false)
  private Long timestamp;

  private Boolean isToOrder;

  private String platformHost;

  //ToDo this needs to be the DCSA Party object
  private String transferee;

  public Transaction linkTransactionToTransportDocument(TransportDocument transportDocument) {
    if (Boolean.TRUE.equals(transportDocument.getIsExported())) {
      throw ConcreteRequestErrorMessageException.internalServerError("Cannot link a transaction to an exported transportDocument");
    }
    this.setTransportDocument(transportDocument);
    return this;
  }

}
