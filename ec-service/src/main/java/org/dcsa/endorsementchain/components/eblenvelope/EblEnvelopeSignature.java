package org.dcsa.endorsementchain.components.eblenvelope;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.components.jws.JWSSignerDetails;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EblEnvelopeSignature {

  private final JWSSignerDetails jwsSignerDetails;
  private final Map<String, JWSVerifier> jwsVerifiers;
  private final RestTemplate restTemplate;

  public SignedEblEnvelopeTO createSignedEblEnvelope(String rawEblEnvelope) {
    String signature = sign(rawEblEnvelope);
    String envelopeHash = DigestUtils.sha256Hex(rawEblEnvelope);

    return SignedEblEnvelopeTO.builder()
        .envelopeHash(envelopeHash)
        .signature(signature)
        .build();
  }

  public String sign(String payload) {
    JWSHeader header = new JWSHeader.Builder(jwsSignerDetails.algorithm()).build();
    JWSObject jwsObject = new JWSObject(header, new Payload(payload));
    try {
      jwsObject.sign(jwsSignerDetails.signer());
    } catch (JOSEException e) {
      throw ConcreteRequestErrorMessageException.internalServerError(
          "Unable to generate the JWS Object");
    }
    return jwsObject.serialize();
  }

  @SneakyThrows
  public boolean verifyEnvelopeHash(String cn, String signature, String eblEnvelopeHash) {
    JWSObject jwsObject = JWSObject.parse(signature);
    // the signed message does not contain the sent envelopeHash
    if (!jwsObject.getPayload().toString().equals(eblEnvelopeHash)) {
      return false;
    }

    JWSVerifier jwsVerifier = getJwsVerifierFromCN(cn);
    return jwsObject.verify(jwsVerifier);
  }

  @SneakyThrows
  public boolean verifySignature(String cn, JWSObject jwsObject) {
    JWSVerifier jwsVerifier = getJwsVerifierFromCN(cn);
    return jwsObject.verify(jwsVerifier);
  }

  private JWSVerifier getJwsVerifierFromCN(String cn) {
    return Optional.ofNullable(jwsVerifiers.get(cn))
      .or(() -> getJWSVerifierFromJWKS(cn))
      .orElseThrow(
        () ->
          ConcreteRequestErrorMessageException.internalServerError(
            "No public key available for sending platform"));
  }

  @SneakyThrows //ToDo this currently only works for the reference implementation. When decided the jwks.json URL becomes 'official' this needs to be refactored to take the context path into consideration
  private Optional<JWSVerifier> getJWSVerifierFromJWKS(String cn) {
    ResponseEntity<String> jwkSetResonseEntity = restTemplate.getForEntity(new URI("https://" + cn + "/v1/unofficial/.well-known/jwks.json"), String.class);
    JWKSet jwkSet = JWKSet.load( new ByteArrayInputStream(jwkSetResonseEntity.getBody().getBytes(StandardCharsets.UTF_8)));
    return Optional.of(new RSASSAVerifier(jwkSet.getKeys().get(0).toRSAKey().toRSAPublicKey()));
  }
}
