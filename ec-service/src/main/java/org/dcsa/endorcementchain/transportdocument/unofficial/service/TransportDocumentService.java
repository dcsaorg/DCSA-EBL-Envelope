package org.dcsa.endorcementchain.transportdocument.unofficial.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorcementchain.persistence.entity.TransportDocument;
import org.dcsa.endorcementchain.persistence.repository.TransportDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransportDocumentService {
  private final TransportDocumentRepository repository;

  public String saveTransportDocument(String transportDocument) {
    return Stream.of(transportDocument)
        .map(
            jsonNode ->
                TransportDocument.builder()
                    .transportDocumentJson(transportDocument)
                    .id(DigestUtils.sha256Hex(transportDocument))
                    .build())
        .map(repository::save)
        .map(TransportDocument::getId)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Saving of the transport document failed"));
  }

  public Optional<String> getTransportDocument(String transportDocumentHash) {
    return repository
        .findById(transportDocumentHash)
        .map(TransportDocument::getTransportDocumentJson);
  }
}
