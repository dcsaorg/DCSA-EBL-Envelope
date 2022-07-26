package org.dcsa.endorsementchain.controller;

import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class JwkSetRestController {

  public static final String API_PATH = "/.well-known";

  @Qualifier("signing-jwk")
  private final JWKSet jwkSet;

  @GetMapping(value = API_PATH + "/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> keys() {
    return jwkSet.toJSONObject();
  }
}
