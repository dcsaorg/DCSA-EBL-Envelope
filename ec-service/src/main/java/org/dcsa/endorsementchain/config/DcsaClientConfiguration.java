package org.dcsa.endorsementchain.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "dcsa.client")
public class DcsaClientConfiguration {
  private boolean tlsVerification = true;
}
