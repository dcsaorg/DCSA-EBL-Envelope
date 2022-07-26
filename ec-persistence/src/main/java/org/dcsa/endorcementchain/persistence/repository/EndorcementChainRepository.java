package org.dcsa.endorcementchain.persistence.repository;

import org.dcsa.endorcementchain.persistence.entity.EndorcementChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EndorcementChainRepository extends JpaRepository<EndorcementChain, UUID> {
}
