package org.dcsa.endorsementchain.unofficial.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransportDocumentService {
  private final TransportDocumentRepository repository;

  public Optional<String> saveTransportDocument(String transportDocument) {
    return Stream.of(transportDocument)
        .map(
            jsonNode ->
                TransportDocument.builder()
                    .transportDocumentJson(transportDocument)
                    .documentHash(DigestUtils.sha256Hex(transportDocument))
                    .isExported(false)
                    .build())

        .map(repository::save)
        .map(TransportDocument::getDocumentHash)
        .filter(Objects::nonNull)
        .findAny();
  }

  public Optional<String> getTransportDocument(String transportDocumentHash) {
    return repository
        .findById(transportDocumentHash)
        .map(TransportDocument::getTransportDocumentJson);
  }
}
