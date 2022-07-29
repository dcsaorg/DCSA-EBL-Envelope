package org.dcsa.endorcementchain.persistence.entity;

import lombok.*;
import org.dcsa.endorcementchain.persistence.entity.enums.TransactionInstruction;

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

  //FK to transportDocument not defined here since this association should not be fetched
  @Column(name = "document_hash", nullable = false, length = 64)
  private String documentHash;

  @Column(name = "exported")
  private Boolean isExported;

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
//  @Type(type = "jsonb")
//  @Column(name = "transaction_content", nullable = false, columnDefinition = "jsonb")
//  private JsonNode transactionContent;

}
