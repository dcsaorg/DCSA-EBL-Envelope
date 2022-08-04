package org.dcsa.endorsementchain.unofficial.service;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.TransactionRepository;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
  private final TransactionRepository repository;
  private final TransportDocumentRepository transportDocumentRepository;
  private final TransactionMapper mapper;

  @Value("${server.port}")
  private String port;

  @Transactional
  public Optional<UUID> createLocalTransaction(
      String documentHash, EndorsementChainTransactionTO transactionRequest) {

    TransportDocument transportDocumentOptional =
        transportDocumentRepository
            .findById(documentHash)
            .orElseThrow(
                () -> ConcreteRequestErrorMessageException.notFound("TransportDocument not found"));

    return Optional.of(transportDocumentOptional)
        .filter(transportDocument -> Boolean.FALSE.equals(transportDocument.getIsExported()))
        .map(transportDocument -> createLinkedTransaction(transactionRequest, transportDocument))
        .map(repository::save)
        .map(Transaction::getId);
  }

  private Transaction createLinkedTransaction(
      EndorsementChainTransactionTO transactionRequest, TransportDocument transportDocument) {
    Transaction transaction =
        mapper.endorsementChainTransactionToTransaction(transactionRequest, "localhost:" + port);
    transaction.linkTransactionToTransportDocument(transportDocument);
    return transaction;
  }

  public List<Transaction> getTransactionsForExport(String documentHash) {
    return repository
        .findLocalNonExportedTransactions(documentHash, "localhost:8443")
        .orElseThrow(
            () ->
                ConcreteRequestErrorMessageException.internalServerError(
                    "No transactions available for export."));
  }

  public List<EndorsementChainTransactionTO> localToEndorsementChainTransactions(List<Transaction> transactions) {
    return transactions.stream().map(mapper::transactionToEndorcementChainTransaction).toList();
  }
}
