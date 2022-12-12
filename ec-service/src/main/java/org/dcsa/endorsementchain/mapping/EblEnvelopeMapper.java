package org.dcsa.endorsementchain.mapping;

import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TransactionMapper.class)
public interface EblEnvelopeMapper {

  @Mapping(source = "envelope.transportDocument.transactions", target = "transactions")
  @Mapping(source = "envelope.transportDocument.documentHash", target = "documentHash")
  EblEnvelopeTO eblEnvelopeToTo(EblEnvelope envelope);
}
