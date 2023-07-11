package org.dcsa.endorsementchain.mapping;

import java.util.List;
import org.dcsa.endorsementchain.persistence.entity.Party;
import org.dcsa.endorsementchain.persistence.entity.SupportingPartyCode;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = {
  SupportingPartyCodeMapper.class,
})
@Component
public abstract class PartyMapper {

  @Autowired
  protected SupportingPartyCodeMapper codeMapper;

  public abstract PartyTO toDTO(Party party);

  @Mapping(expression = "java(partyId)", target = "id")
  @Mapping(expression = "java(mapSupportingCodes(party))", target = "supportingPartyCodes")
  public abstract Party toDAO(PartyTO party, String partyId);

  protected List<SupportingPartyCode> mapSupportingCodes(PartyTO partyTO) {
    var codes = partyTO.supportingPartyCodes();
    if (codes == null) {
      return null;
    }
    return codes.stream().map(c -> codeMapper.toDAO(c, partyTO.id())).toList();
  }
}
