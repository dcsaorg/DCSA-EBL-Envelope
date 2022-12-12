package org.dcsa.endorsementchain.unofficial.service;

import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.TransactionRepository;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.unofficial.datafactories.TransactionDataFactory;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

  private EndorsementChainTransactionTO endorsementChainTransactionTO;
  private Transaction transaction;
  private TransportDocument transportDocument;

  @BeforeEach
  public void init() {
    endorsementChainTransactionTO = TransactionDataFactory.endorsementChainTransaction();
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
            transportDocument.getDocumentHash(), endorsementChainTransactionTO);

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
                    documentHash, endorsementChainTransactionTO));

    assertEquals("TransportDocument not found", returnedException.getMessage());
  }

  @Test
  void testCreateLocalTransactionWithExportedDocument() {
    when(transportDocumentRepository.findById(transportDocument.getDocumentHash()))
      .thenReturn(Optional.of(TransportDocumentDataFactory.exportedTransportDocumentEntityWithoutTransactions()));
    verify(transactionRepository, times(0)).save(any());

    Optional<UUID> transactionId =
      transactionService.createLocalTransaction(
        transportDocument.getDocumentHash(), endorsementChainTransactionTO);
    assertTrue(transactionId.isEmpty());
  }

  @Test
  void testGetTransactionsForExport() {
    String documentHash = transportDocument.getDocumentHash();
    when(transactionRepository.findLocalNonExportedTransactions(any(), any())).thenReturn(Optional.of(List.of(TransactionDataFactory.transactionEntity())));

    List<Transaction> transactions = transactionService.getTransactionsForExport(documentHash);
    assertEquals(1, transactions.size());
    assertEquals(false, transactions.get(0).getTransportDocument().getIsExported());
  }

  @Test
  void testGetTransactionForExportNoneAvailable() {
    String documentHash = transportDocument.getDocumentHash();
    when(transactionRepository.findLocalNonExportedTransactions(any(), any())).thenReturn(Optional.empty());

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () -> transactionService.getTransactionsForExport(documentHash));

    assertEquals("No transactions available for export.", returnedException.getMessage());
  }

  @Test
  void testLocalToEndorsementChainTransactions() {
    List<Transaction> transactions = List.of(transaction);
    List<EndorsementChainTransactionTO> endorsementChainTransactionTOS = transactionService.localToEndorsementChainTransactions(transactions);
    assertEquals(1, endorsementChainTransactionTOS.size());
    assertEquals(transaction.getAction().name(), endorsementChainTransactionTOS.get(0).action().name());
    assertEquals(transaction.getTimestamp(), endorsementChainTransactionTOS.get(0).timestamp());
  }
}
