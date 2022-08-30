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

  @Column(name = "transport_document_json", columnDefinition = "TEXT")
  private String transportDocumentJson;

  @Column(name = "is_exported", nullable = false,  columnDefinition = "boolean default false")
  private Boolean isExported;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "transportDocument", cascade = CascadeType.MERGE)
  private Set<Transaction> transactions;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OneToMany(mappedBy = "transportDocument", cascade = CascadeType.ALL)
  private Set<EblEnvelope> eblEnvelopes;

  public TransportDocument export() {
    this.setIsExported(true);
    return this;
  }

}
