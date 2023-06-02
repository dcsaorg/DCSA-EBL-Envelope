package org.dcsa.endorsementchain.persistence.repository;

import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EndorsementChainEntryRepository extends JpaRepository<EndorsementChainEntry, String> {
  Optional<List<EndorsementChainEntry>> findByTransportDocument_DocumentHash(String documentHash);
}
