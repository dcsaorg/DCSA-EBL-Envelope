package org.dcsa.endorsementchain.unofficial.service;

import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportDocumentServiceTest {

  @Mock TransportDocumentRepository repository;

  @InjectMocks TransportDocumentService service;

  private TransportDocument transportDocument;

  @BeforeEach
  public void init() {
    transportDocument = TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions();
  }

  @Test
  void testSaveTransportDocument() {
    when(repository.save(any())).thenReturn(transportDocument);

    Optional<String> documentHash =
        service.saveTransportDocument(transportDocument.getTransportDocumentJson());
    assertTrue(documentHash.isPresent());
    assertEquals(transportDocument.getDocumentHash(), documentHash.get());
  }

  @Test
  void testSaveTransportDocumentFailed() {
    when(repository.save(any()))
        .thenReturn(TransportDocumentDataFactory.transportDocumentEntityWithoutDocumentHash());

    Optional<String> documentHash =
        service.saveTransportDocument(transportDocument.getTransportDocumentJson());
    assertTrue(documentHash.isEmpty());
  }

  @Test
  void testGetTransportDocument() {
    String documentHash = transportDocument.getDocumentHash();
    when(repository.findById(documentHash)).thenReturn(Optional.of(transportDocument));

    Optional<String> transportDocumentResponse = service.getTransportDocument(documentHash);
    assertTrue(transportDocumentResponse.isPresent());
    assertEquals(transportDocument.getTransportDocumentJson(), transportDocumentResponse.get());
  }

  @Test
  void testGetTransportDocumentNotFound() {
    String documentHash = transportDocument.getDocumentHash();;
    when(repository.findById(documentHash)).thenReturn(Optional.empty());

    Optional<String> transportDocumentResponse = service.getTransportDocument(documentHash);
    assertTrue(transportDocumentResponse.isEmpty());
  }
}
