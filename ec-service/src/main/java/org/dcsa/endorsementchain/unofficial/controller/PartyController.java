package org.dcsa.endorsementchain.unofficial.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.service.PartyService;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Validated
@RestController
@RequiredArgsConstructor
public class PartyController {
  private final PartyService partyService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/unofficial/party/local/", consumes = MediaType.APPLICATION_JSON_VALUE)
  public void createParty(@Valid @NotNull @RequestBody PartyTO partyTO) {
    partyService.createParty(partyTO);
  }
}
