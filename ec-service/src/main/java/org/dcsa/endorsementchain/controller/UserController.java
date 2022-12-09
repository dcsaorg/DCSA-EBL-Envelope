package org.dcsa.endorsementchain.controller;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.service.PartyService;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
@RestController
@RequiredArgsConstructor
public class UserController {
  private final PartyService partyService;

  @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PartyTO> getUser(@NotBlank @Size(max = 255) @PathVariable("userId") String userId) {
    return ResponseEntity.of(partyService.findPartyByUserId(userId));
  }
}
