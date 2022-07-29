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
@Table(name = "transportdocument")
public class TransportDocument {

  @Id
  @Column(name = "document_hash", length = 64, nullable = false)
  private String documentHash;

  @Column(name = "transport_document_json")
  private String transportDocumentJson;

  @Column(name = "is_exported", nullable = false,  columnDefinition = "boolean default false")
  private Boolean isExported;

  @OneToMany(mappedBy = "transportDocument")
  private Set<Transaction> transactions;

}
