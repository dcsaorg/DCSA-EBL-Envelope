package org.dcsa.endorsementchain.components.eblenvelope;

import com.nimbusds.jose.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.components.jws.JWSSignerDetails;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EblEnvelopeSignature {

  private final JWSSignerDetails jwsSignerDetails;
  private final Map<String, JWSVerifier> jwsVerifiers;

  public SignedEblEnvelopeTO signEnvelope(String rawEblEnvelope) {
    JWSHeader header =
        new JWSHeader.Builder(jwsSignerDetails.algorithm())
            .base64URLEncodePayload(false)
            .criticalParams(Collections.singleton("b64"))
            .build();
    try {
      JWSObject jwsObject = new JWSObject(header, new Payload(rawEblEnvelope));
      jwsObject.sign(jwsSignerDetails.signer());
      String signature = jwsObject.serialize(true);
      String envelopeHash = DigestUtils.sha256Hex(rawEblEnvelope);

      return SignedEblEnvelopeTO.builder()
          .eblEnvelopeHash(envelopeHash)
          .signature(signature)
          .eblEnvelope(rawEblEnvelope)
          .build();

    } catch (JOSEException e) {
      throw ConcreteRequestErrorMessageException.internalServerError(
          "Unable to generate the JWS Object");
    }
  }

  public String signEnvelopeHash(String envelopeHash) {
    JWSHeader header = new JWSHeader.Builder(jwsSignerDetails.algorithm()).build();
    JWSObject jwsObject = new JWSObject(header, new Payload(envelopeHash));
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
    // the signed message does not contain the sent eblEnvelopeHash
    if (!jwsObject.getPayload().toString().equals(eblEnvelopeHash)) {
      return false;
    }
    return jwsObject.verify(jwsVerifiers.get(cn));
  }

  @SneakyThrows
  public boolean verifyEnvelope(String cn, String signature, String payload) {
    JWSObject jwsObject = JWSObject.parse(signature, new Payload(payload));

    return jwsObject.verify(jwsVerifiers.get(cn));
  }
}
