package org.dcsa.endorsementchain.unofficial.service;

import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.TransactionRepository;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransaction;
import org.dcsa.endorsementchain.unofficial.datafactories.TransactionDataFactory;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
import org.dcsa.endorsementchain.unofficial.mappers.TransactionMapper;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock TransactionRepository transactionRepository;

  @Mock TransportDocumentRepository transportDocumentRepository;

  @Spy TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

  @InjectMocks TransactionService transactionService;

  private EndorsementChainTransaction endorsementChainTransaction;
  private Transaction transaction;
  private TransportDocument transportDocument;

  @BeforeEach
  public void init() {
    endorsementChainTransaction = TransactionDataFactory.endorsementChainTransaction();
    transaction = TransactionDataFactory.transactionEntity();
    transportDocument = transaction.getTransportDocument();
  }

  @Test
  void testCreateLocalTransactionSuccess() {
    when(transportDocumentRepository.findById(transportDocument.getDocumentHash()))
        .thenReturn(Optional.of(transportDocument));
    when(transactionRepository.save(any())).thenReturn(transaction);

    Optional<UUID> transactionId =
        transactionService.createLocalTransaction(
            transportDocument.getDocumentHash(), endorsementChainTransaction);

    assertTrue(transactionId.isPresent());
    assertEquals(UUID.fromString("326137d8-bd60-4dea-88cc-52687fcb303a"), transactionId.get());
    assertNotNull(transaction.getTransportDocument());
    assertNotNull(transaction.getTimestamp());
    assertEquals("localhost:8443", transaction.getPlatformHost());
  }

  @Test
  void testCreateLocalTransactionNoLinkedTransportDocument() {
    String documentHash = transportDocument.getDocumentHash();
    when(transportDocumentRepository.findById(documentHash)).thenReturn(Optional.empty());

    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () ->
                transactionService.createLocalTransaction(
                    documentHash, endorsementChainTransaction));

    assertEquals("TransportDocument not found", returnedException.getMessage());
  }

  @Test
  void testCreateLocalTransactionWithExportedDocument() {
    when(transportDocumentRepository.findById(transportDocument.getDocumentHash()))
      .thenReturn(Optional.of(TransportDocumentDataFactory.exportedTransportDocumentEntityWithoutTransactions()));
    verify(transactionRepository, times(0)).save(any());

    Optional<UUID> transactionId =
      transactionService.createLocalTransaction(
        transportDocument.getDocumentHash(), endorsementChainTransaction);
    assertTrue(transactionId.isEmpty());
  }
}
