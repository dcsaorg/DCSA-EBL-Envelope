package org.dcsa.endorsementchain.components.client;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WebClient {

  @Bean
  public RestTemplate configureRestTemplate() {
    //ToDo add mTLS configuration
    return new RestTemplate();
  }
}
