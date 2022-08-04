package org.dcsa.endorsementchain.persistence.repository;

import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

  @Query("""
    select t
    from Transaction t
    left outer join t.transportDocument td
    where td.documentHash = :documentHash and t.platformHost = :platformHost and td.isExported = false
    """)
  Optional<List<Transaction>> findLocalNonExportedTransactions(
    @Param("documentHash") String documentHash, @Param("platformHost") String platformHost);
}
