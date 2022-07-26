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
@Table(name = "endorcement_chain")
public class EndorcementChain {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  private UUID id;

  @Type(type = "jsonb")
  @Column(name = "signed_bl_envelopes", columnDefinition = "jsonb")
  private JsonNode signedBlEnvelopes; //TODO evaluate if the JsonNode needs to be replaced with PoJo's -> likely needed if we want to provide some domain logic in there
}
