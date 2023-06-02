package org.dcsa.endorsementchain.components.jws;

import com.nimbusds.jose.JWSObject;

/**
 * Method for verifying signatures
 *
 * <p>
 * Abstracts away logic of trust store management behind this interface as those are generally
 * implementation details.
 * </p>
 */
public interface SignatureVerifier {

  /**
   * @param entityProvidingTheJWSObject Identifier of the entity that provided this JWS.  The reference
   *                                    implementation uses hostnames in order to resolve the jwks.json.
   * @param jwsObject The JWS object that has a signature to be validated
   * @return true if and only the implementation deemed the JWSObject provided to have a valid
   *   signature from the provided platform.
   */
  boolean verifySignature(String entityProvidingTheJWSObject, JWSObject jwsObject);

}
