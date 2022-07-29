package org.dcsa.endorcementchain.eblenvelope.service;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorcementchain.persistence.repository.TransactionRepository;
import org.dcsa.endorcementchain.transferobjects.EndorcementChainTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
  private final TransactionRepository transactionRepository;


  @Transactional
  public UUID createLocalTransaction(String documentHash, EndorcementChainTransaction transaction) {
    //ToDo implement me
    return UUID.randomUUID();
  }


}
