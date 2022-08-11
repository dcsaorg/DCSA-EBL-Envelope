package org.dcsa.endorsementchain.mapping;

import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EblEnvelopeMapper {

  @Mapping(source = "envelope.transportDocument.transactions", target = "transactions")
  @Mapping(source = "envelope.transportDocument.documentHash", target = "documentHash")
  EblEnvelopeTO eblEnvelopeToTo(EblEnvelope envelope);
}
