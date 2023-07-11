package org.dcsa.endorsementchain.datafactories;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.components.jws.JWSSignerDetails;
import org.dcsa.endorsementchain.components.jws.PayloadSigner;
import org.dcsa.endorsementchain.components.jws.SignatureVerifier;
import org.dcsa.endorsementchain.components.jws.impl.DefaultPayloadSigner;

@UtilityClass
public class PayloadSignerFactory {

  // Generated with `openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 4 -subj "/C=US/ST=Delaware/L=Delaware/O=SELFSIGNED/CN=foo" -nodes`
  // Contents in the `key.pem`
  private static final String TEST_RSA_PRIVATE_KEY_PEM =
    "-----BEGIN PRIVATE KEY-----\n" +
    "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCXTD3XOeBMYVZS\n" +
    "Pd1LmImkCzAvCqTZ/YnMh0uhYW3HUOBOdRvE++BY5uny8EZvKI4onH10SI1Wm+oy\n" +
    "HPBPDFA0jP4SN/v83uPke67a6IuMcoQumZVHLY5+plg/w8YehGv0+sdwPY5UuO1O\n" +
    "IcnJoc7b7o7elJC3alJ+hXWvATE+uaw0dcNxbQf+6GaBRY1u7iw/XI0k0LuNo1sI\n" +
    "EuirnoFfDWMErFOhtRJo2DNQOAACbOfM+dTIxTACKdrz3K5GdLoClIT1SoJzq5S8\n" +
    "s/uc4e/CN4hJLcKFHUNQswvS7ba6SD4jf/qiGo/wUTgyaboygED9V33ZUsrfyTnY\n" +
    "wSoGfDvzAgMBAAECggEALI2sDEwjy/pB9Df5icBij+cnikLFJthtksgosl5BeJdN\n" +
    "Zm0//zL47tUZAYxWAXfc3QKwQuT2khGZ1qYE8hI7MC5wxyarUtzEGU1+wUIHjhVO\n" +
    "7XYWqn403wDXLffVyLjQHbUXs+q8liBa6U4z4OeARe2rLsprD0gFAPMGI8HjIYgR\n" +
    "bz+953x695vbOA9DCommw3fdJLiKckLj9i/o0TyNv1aLRyQjrjCSB5JYcfo/rNjJ\n" +
    "eTItoox2/oJYzZeJQ3SdMLf5iqEF8AONCg1+4td25B2KiJhnnbEuHkIp4na0wqeQ\n" +
    "mpqjuYreTiL86vRjB+8ujaa7X2xUorFuXA9Z4qEb4QKBgQDVWFUtDXEBfYyGUU1C\n" +
    "Hoe/SJsvwaQ++amI0Y04rZ6pgA271PfikV2l+W1KMhkxajD8LQ5Nx7WtvhaSuBeT\n" +
    "TwS+1MNL7JF7HhpuoMCy4VgfPivnErnZLUkv8o0HytDK+vtq5c+BwD33NuqPKas4\n" +
    "cAosNEyE4d09aYMs88Xmxbr8AwKBgQC1jCMsDkmy6JNg5e6R1UdvTAOWqFrHb0fU\n" +
    "S2cmyn/++kEUsUw4rvk58m4v1Ci72ZxdkhpDEJRIneoJjrYJzUUS5GuR9EYlk+tB\n" +
    "wjFTNRfzrGenO7F4pJUA3uGG6JMKy7LQ2d4Sm3GM8eR6PHkvO/O0+ZNSjlNDDwS7\n" +
    "rhTuW4zVUQKBgDBCqBnl5X9J0ET+FTT0xQ5fNUOrUSUxwskBZinBFJgRMIoh1eU5\n" +
    "ru6BqthS1uIXvHb/FjJAD/f6fQ65eBPJlzA33unI3Ov11lLaKF0OnqmKndHKqaHY\n" +
    "Hasr+f0eQvb3qXH4BGW8gAfxM0QpT+MXbSWsuvaARVTEDnlXt5fJeM/TAoGAB/sW\n" +
    "HLywDrZcrDjPaQfIMSNVUQ0rmHLS5IlACpuCTvIvZDp7EE7Y0+xNXbrk44Uoc5CV\n" +
    "qPcUnbCbdjoY1It6it8Rv4POhZ5gDC7+PhsqZ2Lf16EvJw+NIVGq9mRI+oOD49x/\n" +
    "/69nqXuEwL7h0OrAxublzA5HqL4DRkDb2LKbmVECgYB+XvRvR0GDE6Wo1zYI2sd/\n" +
    "omQJ0jJ3rT65rWPLPPHg8Pe9Z8VdK56EoJUCStlqDvF4dct+wNt4O5rUJRTpv2jh\n" +
    "ySiy2tAy9P+r91+PQW9Z8p9ecDi/BR2s9TLdceCGjvY518KD5HLLzP8LzG88VySr\n" +
    "s/bpdr+2hBUcSaTF5KXNGw==\n" +
    "-----END PRIVATE KEY-----";


  private static final KeyPair TEST_RSA_KEY_PAIR = parsePEMRSAKey(TEST_RSA_PRIVATE_KEY_PEM);
  private static final PayloadSigner TEST_KEY_PAYLOAD_SIGNER = rsaBasedPayloadSigner(TEST_RSA_KEY_PAIR);
  private static final SignatureVerifier TEST_KEY_SIGNATURE_VERIFICER = singleRSAKeySignatureVerificer(TEST_RSA_KEY_PAIR);

  public PayloadSigner testTestPayloadSigner() {
    return TEST_KEY_PAYLOAD_SIGNER;
  }

  public SignatureVerifier testKeySignatureVerifier() {
    return TEST_KEY_SIGNATURE_VERIFICER;
  }

  private PayloadSigner rsaBasedPayloadSigner(KeyPair keyPair) {
    return new DefaultPayloadSigner(
      new JWSSignerDetails(
        JWSAlgorithm.PS256,
        new RSASSASigner(keyPair.getPrivate())
      )
    );
  }

  private SignatureVerifier singleRSAKeySignatureVerificer(KeyPair keyPair) {
    return new SingleKeySignatureVerificer(new RSASSAVerifier((RSAPublicKey) keyPair.getPublic()));
  }

  @SneakyThrows
  private KeyPair parsePEMRSAKey(String pem) {
    String privKeyPEM = pem.replace("-----BEGIN PRIVATE KEY-----", "")
      .replace("-----END PRIVATE KEY-----", "")
      .replace("\n", "");

    byte [] encoded = Base64.getDecoder().decode(privKeyPEM);

    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    var privateKey = (RSAPrivateCrtKey)kf.generatePrivate(keySpec);
    var publicKey = kf.generatePublic(new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));
    return new KeyPair(publicKey, privateKey);
  }

  private record SingleKeySignatureVerificer(JWSVerifier jwsVerifier) implements SignatureVerifier {

    @SneakyThrows
    @Override
    public boolean verifySignature(String unused, JWSObject jwsObject) {
      return jwsObject.verify(jwsVerifier);
    }
  }
}
