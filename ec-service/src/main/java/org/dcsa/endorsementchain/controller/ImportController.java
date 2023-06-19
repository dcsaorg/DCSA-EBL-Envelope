package org.dcsa.endorsementchain.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.service.ImportService;
import org.dcsa.endorsementchain.transferobjects.EBLEnvelopeTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ImportController {

  private final ImportService importService;

  @PutMapping(value = "/transfer-transactions",
  produces = MediaType.APPLICATION_JSON_VALUE,
  consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> importEBLEnvelope(@RequestBody @Valid EBLEnvelopeTO eblEnvelopeTO) {
    return importService.importEbl(eblEnvelopeTO)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.badRequest().build());
  }
}
