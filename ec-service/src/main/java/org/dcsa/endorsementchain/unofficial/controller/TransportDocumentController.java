package org.dcsa.endorsementchain.unofficial.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TransportDocumentController {

  public static final String API_PATH = "/unofficial/transport-documents";

  private final TransportDocumentService transportDocumentService;
  private final ObjectMapper mapper;

  @GetMapping(
      value = API_PATH + "/{transportDocumentId}",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseBody
  public ResponseEntity<String> getTransportDocument(@PathVariable String transportDocumentId) {
    return transportDocumentService
        .getTransportDocument(transportDocumentId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping(
      value = API_PATH,
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.TEXT_PLAIN_VALUE})
  @ResponseBody
  public ResponseEntity<String> addTransportDocument(
      @RequestBody JsonNode document, UriComponentsBuilder builder) {

    return Optional.of(document)
        .map(this::marshalDocument)
        .flatMap(transportDocumentService::saveTransportDocument)
        .map(
            transportDocumentHash ->
                ResponseEntity.created(
                        builder
                            .path(API_PATH + "/{id}")
                            .buildAndExpand(transportDocumentHash)
                            .toUri())
                    .body(transportDocumentHash))
        .orElseThrow(
            () ->
                ConcreteRequestErrorMessageException.internalServerError(
                    "Saving of the transport document failed"));
  }

  @PostMapping(
      value = API_PATH + "/{transportDocumentId}/export",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseBody
  public ResponseEntity<String> exportTransportDocument(
      @PathVariable String transportDocumentId,
      @RequestBody JsonNode transferee,
      UriComponentsBuilder builder) {
    return transportDocumentService
        .export(transferee.asText(), transportDocumentId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @SneakyThrows
  private String marshalDocument(JsonNode document) {
    return mapper.writeValueAsString(document);
  }
}
