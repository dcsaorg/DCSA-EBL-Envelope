package org.dcsa.endorsementchain.mapping;

import org.dcsa.endorsementchain.persistence.entity.Party;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PartyMapper {
  PartyTO toDTO(Party party);
}
