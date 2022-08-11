package org.dcsa.endorsementchain.unofficial.mapping;

import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

  @Mapping(expression = "java(System.currentTimeMillis())", target = "timestamp")
  @Mapping(source = "platformName", target = "platformHost")
  Transaction endorsementChainTransactionToTransaction(EndorsementChainTransactionTO endorsementChainTransactionTO, String platformName);

  EndorsementChainTransactionTO transactionToEndorcementChainTransaction(Transaction transaction);
}
