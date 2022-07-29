package org.dcsa.endorcementchain.transportdocument.unofficial.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorcementchain.transportdocument.unofficial.service.TransportDocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.stream.Stream;

@RestController
@RequestMapping("${spring.application.api-path}")
@RequiredArgsConstructor
public class TransportDocumentController {

  public static final String API_PATH = "/unofficial/transport-documents";

  @Value("${spring.application.api-path}")
  private String contextPath;

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

    return Stream.of(httpEntity)
        .map(transportDocumentService::saveTransportDocument)
        .map(
            transportDocumentHash ->
                ResponseEntity.created(
                        builder
                            .path(contextPath + API_PATH + "/{id}")
                            .buildAndExpand(transportDocumentHash)
                            .toUri())
                    .body(transportDocumentHash))
        .findFirst()
        .orElse(ResponseEntity.internalServerError().build());
  }
}
