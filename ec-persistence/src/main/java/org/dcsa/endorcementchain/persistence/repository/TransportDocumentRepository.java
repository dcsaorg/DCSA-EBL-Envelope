package org.dcsa.endorcementchain.persistence.repository;

import org.dcsa.endorcementchain.persistence.entity.TransportDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportDocumentRepository extends JpaRepository<TransportDocument, String> {
}
