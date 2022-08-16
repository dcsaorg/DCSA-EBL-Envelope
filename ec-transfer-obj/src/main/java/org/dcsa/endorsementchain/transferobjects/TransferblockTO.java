package org.dcsa.endorsementchain.transferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.List;

public record TransferblockTO(
  @JsonRawValue
  String document,
  List<SignedEblEnvelopeTO> endorsementChain
) {

  @Builder(toBuilder = true)
  public TransferblockTO {
  }

  @JsonCreator
  @Builder(toBuilder = true)
  public TransferblockTO(@JsonProperty("document") JsonNode document, List<SignedEblEnvelopeTO> endorsementChain) {
    this(document.toString(), endorsementChain);
  }
}
