package org.dcsa.endorsementchain.security;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.net.URIBuilder;
import com.nimbusds.jose.jwk.JWKSet;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeSignature;
import org.dcsa.endorsementchain.service.EblEnvelopeService;
import org.dcsa.endorsementchain.service.ExportService;
import org.dcsa.endorsementchain.service.ImportService;
import org.dcsa.endorsementchain.unofficial.controller.JwkSetRestController;
import org.dcsa.endorsementchain.unofficial.service.TransactionService;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ActiveProfiles("prod")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
      "spring.security.keystoreLocation=../test-certificates/dcsa-jwk.jks",
      "server.ssl.key-store=../test-certificates/springboot-https.p12",
    })
@ContextConfiguration(initializers = {SecurityFlowIT.Initializer.class})
class SecurityFlowIT {

  @Autowired
  RestTemplateBuilder restTemplateBuilder;

  @Autowired
  private ServletWebServerApplicationContext webServerAppCtxt;

  private RestTemplate restTemplate;

  @MockBean EblEnvelopeSignature envelopeSignature;

  @MockBean
  @Qualifier("verifying-jwk")
  JWKSet jwkSetVerifying;

  @MockBean ImportService importService;

  @MockBean ExportService exportService;

  @MockBean EblEnvelopeService eblEnvelopeService;

  @MockBean TransactionService transactionService;

  @MockBean TransportDocumentService transportDocumentService;

  @MockBean
  JwkSetRestController jwkSetRestController;

  @PostConstruct
  @SneakyThrows
  void init() {
    SSLContext sslContext = SSLContextBuilder.create()
      .loadTrustMaterial(null, (x509Certificates, s) -> true)
      .build();

    HttpClient httpClient = HttpClientBuilder.create()
      .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()))
      .build();

    ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

    this.restTemplate = restTemplateBuilder.requestFactory(() -> requestFactory).rootUri("https://localhost:"+ webServerAppCtxt.getWebServer().getPort()).build();
  }

  static final KeycloakContainer keycloak =
      new KeycloakContainer().withRealmImportFile("keycloak/realm-export.json");

  static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      keycloak.start();
      TestPropertyValues.of(
              "spring.security.oauth2.resourceserver.jwt.issuer-uri="
                  + keycloak.getAuthServerUrl()
                  + "realms/dcsa",
              "dcsa.securityConfig.jwt.claim.shape=LIST_OF_STRINGS",
              "dcsa.securityConfig.jwt.audience=account")
          .applyTo(configurableApplicationContext.getEnvironment());
    }
  }

  protected String getBearerToken() {

    try {
      String token;
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      URI authorizationURI =
          new URIBuilder(keycloak.getAuthServerUrl() + "/realms/dcsa/protocol/openid-connect/token")
              .build();
      RestTemplate client = new RestTemplateBuilder().build();
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      formData.put("grant_type", Collections.singletonList("client_credentials"));
      formData.put("client_id", Collections.singletonList("dcsa-api"));
      formData.put("client_secret", Collections.singletonList("X32D3a5FoiDRLzXzJMCFW5Q7pMcbZh6o"));

      String result =
          client
              .exchange(
                  authorizationURI,
                  HttpMethod.POST,
                  new HttpEntity<>(formData, headers),
                  String.class)
              .getBody();

      JacksonJsonParser jsonParser = new JacksonJsonParser();

      token = "Bearer " + jsonParser.parseMap(result).get("access_token").toString();

      log.info("TOKEN :: {}", token);
      return token;
    } catch (URISyntaxException e) {
      log.error("Can't obtain an access token from Keycloak!", e);
      throw new RuntimeException("Can't obtain an access token from Keycloak!");
    }
  }

  @RestController
  static class DummyController {
    @GetMapping("/dummy")
    public ResponseEntity<String> test() {
      return ResponseEntity.ok().body("It works!");
    }
  }

  @Test
  void validTokenShouldReturnSuccess() {

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", getBearerToken());

    ResponseEntity<String> exchange =
      restTemplate.exchange(
            "/v1/dummy", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertEquals(HttpStatus.OK, exchange.getStatusCode());
    assertEquals("It works!", exchange.getBody());
  }

  @Test
  void invalidTokenShouldReturnUnauthorized() {

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer xxxxxx.yyyyyy.zzzzzz");

    HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
      ResponseEntity<String> exchange =
        restTemplate.exchange(
          "/v1/dummy", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }
}
