package org.dcsa.endorsementchain.components.jws.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.components.jws.JWSSignerDetails;
import org.dcsa.endorsementchain.components.jws.PayloadSigner;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultPayloadSigner implements PayloadSigner {

  private final JWSSignerDetails jwsSignerDetails;

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
}
