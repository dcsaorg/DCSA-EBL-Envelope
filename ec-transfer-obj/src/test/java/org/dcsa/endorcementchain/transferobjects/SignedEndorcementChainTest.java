package org.dcsa.endorcementchain.transferobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorcementchain.transferobjects.enums.BlInstruction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignedEndorcementChainTest {

  @Test
  void testSignedEndorcementChainMessageStructure() throws JsonProcessingException {
    String expected = "[{\"eblEnvelopeHash\":\"d56a93a7e9f86a2d895df818e0440bdca6ffe03246e2fee14131f2e66c84c75a\",\"signature\":\"eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJIUzI1NiJ9..5rPBT_XW-x7mjc1ubf4WwW1iV2YJyc4CCFxORIEaAEk\",\"eblEnvelope\":{\"previousEblEnvelopeHash\":null,\"documentHash\":\"76a7d14c83d7268d643ae7345c448de60701f955d264a743e6928a0b8268b24f\",\"transactions\":[{\"instruction\":\"ISSU\",\"comments\":\"B/L has been issued\",\"transferee\":\"foo\",\"timestamp\":1658403878461,\"isToOrder\":true,\"platformHost\":\"localhost:8443\"},{\"instruction\":\"TRNS\",\"comments\":\"B/L has been transferred to bar\",\"transferee\":\"bar\",\"timestamp\":1658403878472,\"isToOrder\":true,\"platformHost\":\"localhost:8443\"}]}}]";

    EndorcementChainTransaction endorcementChainEntry1 =
        EndorcementChainTransaction.builder()
            .instruction(BlInstruction.ISSU)
            .comments("B/L has been issued")
            .isToOrder(true)
            .timestamp(1658403878461L)
            .transferee("foo")
            .platformHost("localhost:8443")
            .build();

    EndorcementChainTransaction endorcementChainEntry2 =
        EndorcementChainTransaction.builder()
            .instruction(BlInstruction.TRNS)
            .comments("B/L has been transferred to bar")
            .isToOrder(true)
            .timestamp(1658403878472L)
            .transferee("bar")
            .platformHost("localhost:8443")
            .build();

    EblEnvelopeTO eblEnvelope =
        EblEnvelopeTO.builder()
            .previousEblEnvelopeHash(null)
            .transactions(List.of(endorcementChainEntry1, endorcementChainEntry2))
            .documentHash("76a7d14c83d7268d643ae7345c448de60701f955d264a743e6928a0b8268b24f")
            .build();

    SignedEblEnvelopeTO signedEblEnvelopeTO = SignedEblEnvelopeTO.builder()
      .eblEnvelope(eblEnvelope)
      .eblEnvelopeHash("d56a93a7e9f86a2d895df818e0440bdca6ffe03246e2fee14131f2e66c84c75a")
      .signature("eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJIUzI1NiJ9..5rPBT_XW-x7mjc1ubf4WwW1iV2YJyc4CCFxORIEaAEk")
      .build();

    List<SignedEblEnvelopeTO> endorcementChain = List.of(signedEblEnvelopeTO);

    ObjectMapper mapper = new ObjectMapper();
    assertEquals(expected, mapper.writeValueAsString(endorcementChain));
  }
}
