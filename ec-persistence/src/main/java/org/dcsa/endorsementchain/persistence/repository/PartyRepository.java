package org.dcsa.endorsementchain.persistence.repository;

import org.dcsa.endorsementchain.persistence.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, String> { }
