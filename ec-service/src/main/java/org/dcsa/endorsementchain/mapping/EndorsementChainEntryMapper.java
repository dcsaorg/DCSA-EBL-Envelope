package org.dcsa.endorsementchain.mapping;

import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TransactionMapper.class)
public interface EndorsementChainEntryMapper {

  @Mapping(source = "endorsementChainEntry.transportDocument.transactions", target = "transactions")
  @Mapping(source = "endorsementChainEntry.transportDocument.documentHash", target = "documentHash")
  EndorsementChainEntryTO endorsementChainEntryToTO(EndorsementChainEntry endorsementChainEntry);
}
