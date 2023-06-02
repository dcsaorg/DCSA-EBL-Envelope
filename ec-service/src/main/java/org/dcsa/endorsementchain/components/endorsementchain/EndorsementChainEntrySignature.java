package org.dcsa.endorsementchain.components.endorsementchain;

import com.nimbusds.jose.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.components.jws.JWSSignerDetails;
import org.dcsa.endorsementchain.components.jws.SignatureVerifier;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EndorsementChainEntrySignature {

  private final JWSSignerDetails jwsSignerDetails;
  private final SignatureVerifier signatureVerifier;

  public SignedEndorsementChainEntryTO createSignedEndorsementChainEntry(String endorsementChainEntry) {
    String signature = sign(endorsementChainEntry);
    String envelopeHash = DigestUtils.sha256Hex(endorsementChainEntry);

    return SignedEndorsementChainEntryTO.builder()
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
  public boolean verifyEndorsementChainHash(String cn, String signature, String endorsementChainEntryHash) {
    JWSObject jwsObject = JWSObject.parse(signature);
    // the signed message does not contain the sent envelopeHash
    if (!jwsObject.getPayload().toString().equals(endorsementChainEntryHash)) {
      return false;
    }
    return signatureVerifier.verifySignature(cn, jwsObject);
  }

}
