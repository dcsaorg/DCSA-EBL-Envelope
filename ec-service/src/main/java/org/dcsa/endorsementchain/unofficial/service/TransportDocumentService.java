package org.dcsa.endorsementchain.unofficial.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.entity.enums.TransactionInstruction;
import org.dcsa.endorsementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorsementchain.service.ExportService;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransportDocumentService {
  private final TransportDocumentRepository repository;
  private final ExportService exportService;
  private final ObjectMapper mapper;

  @Value("${server.port}")
  private String port;

  @Value("${server.hostname}")
  private String hostname;

  @Transactional
  public Optional<String> saveTransportDocument(String transportDocument) {

    String canonizedTransportDocument = canonizeJson(transportDocument);
    String documentHash = DigestUtils.sha256Hex(canonizedTransportDocument);

    verifyDocumentHash(canonizedTransportDocument, List.of(documentHash));

    return Stream.of(canonizedTransportDocument)
        .map(
            jsonNode ->
                TransportDocument.builder()
                    .transportDocumentJson(canonizedTransportDocument)
                    .documentHash(documentHash)
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

  public void verifyDocumentHash(String transportDocument, List<String> documentHash) {

    boolean isVerified = documentHash.stream().allMatch(DigestUtils.sha256Hex(transportDocument)::equals);

    if (!isVerified) {
      throw ConcreteRequestErrorMessageException.invalidInput(
          "Transportdocument hash verification failed");
    }
  }

  @Transactional
  public Optional<String> export(String transferee, String documentHash) {

    TransportDocument transportDocument =
        repository
            .findById(documentHash)
            .orElseThrow(
                () -> ConcreteRequestErrorMessageException.notFound("TransportDocument not found"));

    return Optional.ofNullable(transportDocument)
        .map(includeTransferTransaction(transferee))
        .map(repository::save)
        .map(exportTransportDocument(transferee, documentHash))
        .map(TransportDocument::export)
        .map(repository::save)
        .map(transportDocument1 -> "TransportDocument exported");
  }

  @SneakyThrows
  private String canonizeJson(String rawDocument) {
    JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(rawDocument);
    return jsonCanonicalizer.getEncodedString();
  }

  private Function<TransportDocument, TransportDocument> includeTransferTransaction(
      String transferee) {
    return transportDocument -> {
      Set<Transaction> transactions = transportDocument.getTransactions();

      Transaction exportTransaction = createExportTransaction(transferee, transactions);
      exportTransaction.linkTransactionToTransportDocument(transportDocument);
      transactions.add(exportTransaction);
      return transportDocument;
    };
  }

  private Transaction createExportTransaction(String transferee, Set<Transaction> transactions) {
    return Transaction.builder()
        .transferee(transferee)
        .comments("The B/L exported to: " + transferee)
        .isToOrder(
            transactions.stream()
                .map(Transaction::getIsToOrder)
                .findAny()
                .orElse(
                    true)) // When no local transactions exist only the export transaction will be
        // created and isToOrder will be set to true
        .platformHost(hostname+":"+port)
        .timestamp(System.currentTimeMillis())
        .instruction(TransactionInstruction.TRNS)
        .build();
  }

  private Function<TransportDocument, TransportDocument> exportTransportDocument(
      String transferee, String documentHash) {
    return transportDocument -> {
      exportService.exportEbl(transferee, documentHash);
      return transportDocument;
    };
  }
}
