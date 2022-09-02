package org.dcsa.endorsementchain.unofficial.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.unofficial.service.TransactionService;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionController {

  public static final String API_PATH = "/unofficial/transactions";

  private final TransactionService service;

  @PostMapping(
      value = API_PATH + "/local/{transportDocumentHash}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> createLocalTransaction(
      @PathVariable("transportDocumentHash") String transportDocumentHash,
      @RequestBody EndorsementChainTransactionTO transactionRequest,
      UriComponentsBuilder builder) {
    return service
        .createLocalTransaction(transportDocumentHash, transactionRequest)
        .map(UUID::toString)
        .map(
            transactionID ->
                ResponseEntity.created(
                        builder
                            .path(API_PATH + "/local/{transportDocumentHash}")
                            .buildAndExpand(transactionID)
                            .toUri())
                    .body(transactionID))
        .orElseThrow(() -> ConcreteRequestErrorMessageException.invalidParameter("Cannot create a transaction on an exported TransportDocument."));
  }
}
