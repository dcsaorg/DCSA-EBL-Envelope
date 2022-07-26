package org.dcsa.endorcementchain.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

  @Type(type = "jsonb")
  @Column(name = "transport_document_json", columnDefinition = "jsonb")
  private JsonNode transportDocumentJson;

}
