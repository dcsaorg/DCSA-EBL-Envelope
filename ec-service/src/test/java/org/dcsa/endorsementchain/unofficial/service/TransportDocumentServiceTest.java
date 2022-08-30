package org.dcsa.endorsementchain.unofficial.service;

import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorsementchain.service.ExportService;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportDocumentServiceTest {

  @Mock TransportDocumentRepository repository;
  @Mock ExportService exportService;

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
        service.saveTransportDocument(transportDocument.getTransportDocumentJson(), TransportDocumentDataFactory.transportDocumentHash());
    assertTrue(documentHash.isPresent());
    assertEquals(transportDocument.getDocumentHash(), documentHash.get());
  }

  @Test
  void testSaveTransportDocumentFailed() {
    when(repository.save(any()))
        .thenReturn(TransportDocumentDataFactory.transportDocumentEntityWithoutDocumentHash());

    Optional<String> documentHash =
        service.saveTransportDocument(transportDocument.getTransportDocumentJson(), TransportDocumentDataFactory.transportDocumentHash());
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
    String documentHash = transportDocument.getDocumentHash();
    when(repository.findById(documentHash)).thenReturn(Optional.empty());

    Optional<String> transportDocumentResponse = service.getTransportDocument(documentHash);
    assertTrue(transportDocumentResponse.isEmpty());
  }

  @Test
  void testExportTransportDocument() {
    TransportDocument transportDocumentWithTransactions = TransportDocumentDataFactory.transportDocumentEntityWithTransactions();
    String documentHash = transportDocumentWithTransactions.getDocumentHash();
    String transferee = "test";
    when(repository.findById(documentHash)).thenReturn(Optional.of(transportDocumentWithTransactions));
    when(exportService.exportEbl(transferee, documentHash)).thenReturn("test signature");
    when(repository.save(transportDocumentWithTransactions)).thenReturn(transportDocumentWithTransactions);

    Optional<String> exportResponse = service.export(transferee, documentHash);

    assertTrue(exportResponse.isPresent());
    assertEquals("TransportDocument exported", exportResponse.get());
  }

  @Test
  void testExportTransportDocumentFailed() {
    TransportDocument transportDocumentWithTransactions = TransportDocumentDataFactory.transportDocumentEntityWithTransactions();
    String documentHash = transportDocumentWithTransactions.getDocumentHash();
    String transferee = "test";
    when(repository.findById(documentHash)).thenReturn(Optional.empty());

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () ->
          service.export(transferee, documentHash));

    assertEquals("TransportDocument not found", returnedException.getMessage());
  }

  @Test
  void testExportTransportDocumentCantBeSaved() {
    TransportDocument transportDocumentWithTransactions = TransportDocumentDataFactory.transportDocumentEntityWithTransactions();
    String documentHash = transportDocumentWithTransactions.getDocumentHash();
    String transferee = "test";
    when(repository.findById(documentHash)).thenReturn(Optional.of(transportDocumentWithTransactions));
    when(repository.save(transportDocumentWithTransactions)).thenReturn(null);

    Optional<String> exportResponse = service.export(transferee, documentHash);

    assertFalse(exportResponse.isPresent());
  }
}
