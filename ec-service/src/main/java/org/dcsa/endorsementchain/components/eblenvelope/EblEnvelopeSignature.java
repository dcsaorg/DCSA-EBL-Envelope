package org.dcsa.endorsementchain.components.eblenvelope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.components.jws.JWSSignerDetails;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class EblEnvelopeSignature {

  private final JWSSignerDetails jwsSignerDetails;
  private final JWSVerifier jwsVerifier;
  private final ObjectMapper mapper;


  public SignedEblEnvelopeTO sign(EblEnvelopeTO eblEnvelope) {
    JWSHeader header =
      new JWSHeader.Builder(jwsSignerDetails.algorithm())
        .base64URLEncodePayload(false)
        .criticalParams(Collections.singleton("b64"))
        .build();
    try {
      String jsonPayload = mapper.writeValueAsString(eblEnvelope);
      JWSObject jwsObject =
        new JWSObject(header, new Payload(mapper.writeValueAsString(jsonPayload)));
      jwsObject.sign(jwsSignerDetails.signer());
      String signature = jwsObject.serialize(true);
      String envelopeHash = DigestUtils.sha256Hex(jsonPayload);

      return SignedEblEnvelopeTO.builder()
        .eblEnvelopeHash(envelopeHash)
        .signature(signature)
        .eblEnvelope(eblEnvelope)
        .build();

    } catch (JOSEException | JsonProcessingException e) {
      throw ConcreteRequestErrorMessageException.internalServerError(
        "Unable to generate the JWS Object");
    }
  }

  @SneakyThrows
  public Boolean verify(String signature, String eblEnvelopeHash) {
    JWSObject jwsObject = JWSObject.parse(signature);
    //the signed message does not contain the sent eblEnvelopeHash
    if(!jwsObject.getPayload().toString().equals(eblEnvelopeHash)){
      return false;
    }
    return jwsObject.verify(jwsVerifier);
  }
}
