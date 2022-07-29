package org.dcsa.endorsementchain.unofficial.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TransportDocumentController {

  public static final String API_PATH = "/unofficial/transport-documents";

  private final TransportDocumentService transportDocumentService;

  @GetMapping(
      value = API_PATH + "/{transportDocumentId}",
      produces = {"application/json"})
  @ResponseBody
  @CrossOrigin(origins = "*")
  public ResponseEntity<String> getTransportDocument(@PathVariable String transportDocumentId) {
    return transportDocumentService
        .getTransportDocument(transportDocumentId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping(
      value = API_PATH,
      consumes = {"application/json"},
      produces = {"application/json"})
  @ResponseBody
  public ResponseEntity<String> addTransportDocument(
      @RequestBody String httpEntity, UriComponentsBuilder builder) {

    return transportDocumentService
        .saveTransportDocument(httpEntity)
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
}
