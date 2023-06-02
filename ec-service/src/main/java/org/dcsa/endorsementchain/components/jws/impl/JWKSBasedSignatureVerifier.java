package org.dcsa.endorsementchain.components.jws.impl;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.dcsa.endorsementchain.components.jws.SignatureVerifier;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JWKSBasedSignatureVerifier implements SignatureVerifier {

  private final Map<String, JWSVerifier> jwsVerifiers;
  private final RestTemplate restTemplate;

  @SneakyThrows
  public boolean verifySignature(String entityProvidingTheJWSObject, JWSObject jwsObject) {
    JWSVerifier jwsVerifier = getJwsVerifierFromCN(entityProvidingTheJWSObject);
    return jwsObject.verify(jwsVerifier);
  }

  @SneakyThrows //ToDo this currently only works for the reference implementation. When decided the jwks.json URL becomes 'official' this needs to be refactored to take the context path into consideration
  private Optional<JWSVerifier> getJWSVerifierFromJWKS(String cn) {
    ResponseEntity<String> jwkSetResponseEntity = restTemplate.getForEntity(new URI("https://" + cn + "/v1/.well-known/jwks.json"), String.class);
    var body = jwkSetResponseEntity.getBody();
    if (body == null) {
      // We could also throw here as an empty body would be invalid.
      // - However, the only caller does throw for us when we return empty here anyway, so meh.
      return Optional.empty();
    }

    JWKSet jwkSet = JWKSet.load(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    return Optional.of(new RSASSAVerifier(jwkSet.getKeys().get(0).toRSAKey().toRSAPublicKey()));
  }


  private JWSVerifier getJwsVerifierFromCN(String cn) {
    return Optional.ofNullable(jwsVerifiers.get(cn))
      .or(() -> getJWSVerifierFromJWKS(cn))
      .orElseThrow(
        () ->
          ConcreteRequestErrorMessageException.internalServerError(
            "No public key available for sending platform"));
  }

}
