package org.dcsa.endorsementchain.transferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.List;

public record EBLEnvelopeTO(
  @JsonRawValue
  String document,
  List<SignedEndorsementChainEntryTO> endorsementChain
) {

  @Builder(toBuilder = true)
  public EBLEnvelopeTO {
  }

  @JsonCreator
  @Builder(toBuilder = true)
  public EBLEnvelopeTO(@JsonProperty("document") JsonNode document,
                       List<SignedEndorsementChainEntryTO> endorsementChain) {
    this(document.toString(), endorsementChain);
  }
}
