package org.dcsa.endorcementchain.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.hibernate.annotations.Type;

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

  @Column(name = "timestamp", nullable = false)
  private Long timestamp;

  //FK to transportDocument not defined here since this association should not be fetched
  @Column(name = "document_hash", nullable = false, length = 64)
  private String documentHash;

  @Column(name = "exported")
  private Boolean isExported;

  @Type(type = "jsonb")
  @Column(name = "transaction_content", nullable = false, columnDefinition = "jsonb")
  private JsonNode transactionContent; //ToDo verify if this needs an explicit pojo
}
