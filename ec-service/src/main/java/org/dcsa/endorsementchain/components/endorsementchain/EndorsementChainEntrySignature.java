package org.dcsa.endorsementchain.components.endorsementchain;

import com.nimbusds.jose.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.components.jws.JWSSignerDetails;
import org.dcsa.endorsementchain.components.jws.PayloadSigner;
import org.dcsa.endorsementchain.components.jws.SignatureVerifier;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EndorsementChainEntrySignature {

  private final SignatureVerifier signatureVerifier;
  private final PayloadSigner payloadSigner;

  public SignedEndorsementChainEntryTO createSignedEndorsementChainEntry(String endorsementChainEntry) {
    String signature = payloadSigner.sign(endorsementChainEntry);
    String envelopeHash = DigestUtils.sha256Hex(endorsementChainEntry);

    return SignedEndorsementChainEntryTO.builder()
        .envelopeHash(envelopeHash)
        .signature(signature)
        .build();
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
