package org.dcsa.endorsementchain.unofficial.mappers;

import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

  @Mapping(expression = "java(System.currentTimeMillis())", target = "timestamp")
  @Mapping(source = "platformName", target = "platformHost")
  Transaction EndorsementChainTransactionToTransaction(EndorsementChainTransaction endorsementChainTransaction, String platformName);
}
