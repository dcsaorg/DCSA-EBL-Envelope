package org.dcsa.endorsementchain.mapping;

import org.dcsa.endorsementchain.persistence.entity.SupportingPartyCode;
import org.dcsa.endorsementchain.transferobjects.SupportingPartyCodeTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupportingPartyCodeMapper {
  SupportingPartyCodeTO toDTO(SupportingPartyCode supportingPartyCode);

  @Mapping(source = "partyId", target = "partyId")
  SupportingPartyCode toDAO(SupportingPartyCodeTO supportingPartyCodeTO, String partyId);
}
