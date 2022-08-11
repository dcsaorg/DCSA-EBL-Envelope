package org.dcsa.endorsementchain.components.jws;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
public class JWKManager {

  @Bean("signing-jwk")
  public JWKSet jwkSigningSet(@Value("${spring.security.keystoreLocation}") String keystoreLocation, @Value("${spring.security.keystorePassword}") char[] keystorePassword) {
    return getJwkSet(keystoreLocation, keystorePassword);
  }

  @Bean("verifying-jwk")
  public JWKSet jwkVerificationSet(@Value("${spring.security.verification.keystoreLocation}") String keystoreLocation, @Value("${spring.security.verification.keystorePassword}") char[] keystorePassword) {
    return getJwkSet(keystoreLocation, keystorePassword);
  }

  @SneakyThrows
  @Bean
  public JWSVerifier getJWSVerifier(@Qualifier("verifying-jwk") JWKSet jwkSet, @Value("${spring.security.verification.jws.key-id}") String keyId) {
    JWK jwk = jwkSet.getKeyByKeyId(keyId);

    KeyType keyType = getValidKeyType(jwk);
    return switch (keyType.getValue()) {
      case "RSA" -> new RSASSAVerifier(jwk.toRSAKey().toRSAPublicKey());
      case "EC" -> new ECDSAVerifier(jwk.toECKey().toECPublicKey());
      default -> throw new IllegalStateException("Cannot create JWS Verifier due to unknown key type: " + keyType);
    };
  }

  @Bean
  public JWSSignerDetails getJWSSignerDetails(@Qualifier("signing-jwk") JWKSet jwkSet, @Value("${spring.security.jws.key-id}") String keyId) throws JOSEException {
    JWK jwk = jwkSet.getKeyByKeyId(keyId);

    KeyType keyType = getValidKeyType(jwk);

    return switch (keyType.getValue()) {
      case "RSA" -> new JWSSignerDetails(JWSAlgorithm.RS256, new RSASSASigner(jwk.toRSAKey()));
      case "EC" -> {
        ECKey ecKey = jwk.toECKey();
        yield new JWSSignerDetails(getESAlgorithm(ecKey.getCurve()), new ECDSASigner(ecKey));
      }
      default -> throw new IllegalStateException("Cannot create JWS Signer due to unknown key type: " + keyType);
    };

  }

  private JWKSet getJwkSet(String keystoreLocation, char[] keystorePassword) {
    JWKSet jwkSet = null;
    try {
      KeyStore keyStore = KeyStore.getInstance("PKCS12");

      keyStore.load(
        JWKManager.class.getClassLoader().getResourceAsStream(keystoreLocation),
        keystorePassword);

      jwkSet = JWKSet.load(keyStore, name -> keystorePassword);
    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to create the JWKSet from the JKS.");
    }
    return jwkSet;
  }

  private KeyType getValidKeyType(JWK jwk) {
    KeyType keyType = jwk.getKeyType();
    if (keyType == null) {
      keyType = getKeyType(jwk);
    }
    return keyType;
  }

  private KeyType getKeyType(JWK jwk) {
    KeyType keyType;
    if (jwk instanceof RSAKey) {
      keyType = KeyType.RSA;
    } else if (jwk instanceof ECKey) {
      keyType = KeyType.EC;
    } else {
      throw new IllegalStateException("Unknown key type: " + jwk.getClass().getName());
    }
    return keyType;
  }

  private JWSAlgorithm getESAlgorithm(Curve curve) {
    return switch (curve.getStdName()) {
      case "secp256r1" -> JWSAlgorithm.ES256;
      case "secp256k1" -> JWSAlgorithm.ES256K;
      case "secp384r1" -> JWSAlgorithm.ES384;
      case "secp521r1" -> JWSAlgorithm.ES512;
      default -> throw new IllegalStateException("Unknown EC Curve.");
    };
  }

}
