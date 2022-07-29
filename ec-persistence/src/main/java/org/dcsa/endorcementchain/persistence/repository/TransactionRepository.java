package org.dcsa.endorcementchain.persistence.repository;

import org.dcsa.endorcementchain.persistence.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {


//  //It is only possible to create a new transaction if the other transactions for this B/L are not exported
//  @Query(value = """
//  insert into transaction (document_hash, timestamp, transaction_content)
//  select :document_hash, 1658497983143, :transaction_content
//  where exists (select * from "transaction" where document_hash = :document_hash and exported = false)
//  """,
//  nativeQuery = true)
//  Transaction createTransaction(@Param("document_hash") String documentHash, @Param("transaction_content")JsonNode transactionContent);
}
