package org.dcsa.endorsementchain.mapping;

import org.dcsa.endorsementchain.persistence.entity.Party;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PartyMapper {
  PartyTO toDTO(Party party);

  @Mapping(expression = "java(partyId)", target = "id")
  Party toDAO(PartyTO party, String partyId);
}
