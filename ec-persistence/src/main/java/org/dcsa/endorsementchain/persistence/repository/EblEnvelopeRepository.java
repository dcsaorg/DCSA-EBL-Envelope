package org.dcsa.endorsementchain.persistence.repository;

import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EblEnvelopeRepository extends JpaRepository<EblEnvelope, String> {
  Optional<List<EblEnvelope>> findByTransportDocument_DocumentHash(String documentHash);
}
