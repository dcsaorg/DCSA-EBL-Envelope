package org.dcsa.endorcementchain;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Collections;

@SpringBootApplication
@ComponentScan("org.dcsa")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Component
  public class CommandLineAppStartupRunner implements CommandLineRunner {
    private static final Logger LOG =
      LoggerFactory.getLogger(CommandLineAppStartupRunner.class);

    @Override
    public void run(String...args) throws Exception {
//      detachedUnencodedJWSPayload();
    }

    private void detachedUnencodedJWSPayload() throws JOSEException, ParseException {
      // Some HMAC key for JWS with HS256
      OctetSequenceKey hmacJWK = OctetSequenceKey.parse("{"+
        "\"kty\":\"oct\"," +
        "\"k\":\"AyM1SysPpbyDfgZld3umj1qzKObwVMkoqQ-EstJQLr_T-1qS0gZH75aKtMN3Yj0iPS4hcgUuTwjAzZr1Z9CAow\"" +
        "}");

// The payload which will not be encoded and must be passed to
// the JWS consumer in a detached manner
      Payload detachedPayload = new Payload("Hello, world!");

// Create and sign JWS
      JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
        .base64URLEncodePayload(false)
        .criticalParams(Collections.singleton("b64"))
        .build();

      JWSObject jwsObject = new JWSObject(header, detachedPayload);
      jwsObject.sign(new MACSigner(hmacJWK));

      boolean isDetached = true;

      String jws = jwsObject.serialize(isDetached);
      LOG.info("Detached JWS: " + jws);
      LOG.info("Payload: \n" + detachedPayload);


// Parse JWS with detached payload
      JWSObject parsedJWSObject = JWSObject.parse(jws, detachedPayload);

// Verify the HMAC
      if (parsedJWSObject.verify(new MACVerifier(hmacJWK))) {
        System.out.println("Valid HMAC");
      } else {
        System.out.println("Invalid HMAC");
      }
    }
  }
}
