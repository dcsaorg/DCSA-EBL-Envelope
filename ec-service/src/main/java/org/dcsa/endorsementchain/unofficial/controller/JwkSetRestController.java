package org.dcsa.endorsementchain.unofficial.controller;

import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class JwkSetRestController {

  public static final String API_PATH = "/unofficial/.well-known";

  private final JWKSet jwkSet;

  @GetMapping(value = API_PATH + "/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> keys() {
    return jwkSet.toJSONObject();
  }
}
