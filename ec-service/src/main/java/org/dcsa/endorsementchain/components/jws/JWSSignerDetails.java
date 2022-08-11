package org.dcsa.endorsementchain.components.jws;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;

public record JWSSignerDetails(JWSAlgorithm algorithm, JWSSigner signer) {}
