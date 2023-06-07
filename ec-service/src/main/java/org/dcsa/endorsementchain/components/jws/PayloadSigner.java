package org.dcsa.endorsementchain.components.jws;

public interface PayloadSigner {
  String sign(String payload);
}
