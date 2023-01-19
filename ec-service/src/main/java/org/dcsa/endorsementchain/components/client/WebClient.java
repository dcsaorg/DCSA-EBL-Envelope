package org.dcsa.endorsementchain.components.client;

import lombok.SneakyThrows;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.dcsa.endorsementchain.config.DcsaClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Component
public class WebClient {

  @Bean
  @SneakyThrows
  public RestTemplate configureRestTemplate(DcsaClientConfiguration dcsaClientConfiguration) {
    if (!dcsaClientConfiguration.isTlsVerification()) {
      SSLContext sslContext = SSLContextBuilder.create()
        .loadTrustMaterial(null, (x509Certificates, s) -> true)
        .build();
      var sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
      var socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create()
          .register("https", sslsf)
          .register("http", new PlainConnectionSocketFactory())
          .build();
      var connectionManager =
        new BasicHttpClientConnectionManager(socketFactoryRegistry);
      var httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager).build();
      var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

      return new RestTemplate(requestFactory);
    }
    //ToDo add mTLS configuration
    return new RestTemplate();
  }
}
