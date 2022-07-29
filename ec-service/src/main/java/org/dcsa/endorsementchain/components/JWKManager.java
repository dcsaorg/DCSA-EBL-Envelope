package org.dcsa.endorsementchain.components;

import com.nimbusds.jose.jwk.JWKSet;
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

  @Value("${spring.security.keystorePassword}")
  private char[] keystorePassword;

  @Value("${spring.security.keystoreLocation}")
  private String keystoreLocation;

  @Bean
  public JWKSet jwkSet() {
    JWKSet jwkSet = null;
    try {
      KeyStore keyStore = KeyStore.getInstance("PKCS12");

      keyStore.load(
          JWKManager.class.getClassLoader().getResourceAsStream(keystoreLocation),
          keystorePassword);

      jwkSet = JWKSet.load(keyStore, name -> keystorePassword);
    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
      throw new RuntimeException("Unable to create the JWKSet from the JKS.");
    }
    return jwkSet;
  }
}
