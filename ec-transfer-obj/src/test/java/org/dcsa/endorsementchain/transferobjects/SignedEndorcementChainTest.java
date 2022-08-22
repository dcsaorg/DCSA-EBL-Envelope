package org.dcsa.endorsementchain.transferobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.transferobjects.enums.BlInstruction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignedEndorcementChainTest {

  @Test
  void testSignedEndorcementChainMessageStructure() throws JsonProcessingException {
    String expected =
        "[{\"envelopeHash\":\"d56a93a7e9f86a2d895df818e0440bdca6ffe03246e2fee14131f2e66c84c75a\",\"eblEnvelope\":{\"previousEblEnvelopeHash\":null,\"documentHash\":\"76a7d14c83d7268d643ae7345c448de60701f955d264a743e6928a0b8268b24f\",\"transactions\":[{\"instruction\":\"ISSU\",\"comments\":\"B/L has been issued\",\"timestamp\":1658403878461,\"isToOrder\":true,\"platformHost\":\"localhost:8443\",\"transferee\":\"foo\"},{\"instruction\":\"TRNS\",\"comments\":\"B/L has been transferred to bar\",\"timestamp\":1658403878472,\"isToOrder\":true,\"platformHost\":\"localhost:8443\",\"transferee\":\"bar\"}]},\"signature\":\"eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJIUzI1NiJ9..5rPBT_XW-x7mjc1ubf4WwW1iV2YJyc4CCFxORIEaAEk\"}]";

    EndorsementChainTransactionTO endorcementChainEntry1 =
        EndorsementChainTransactionTO.builder()
            .instruction(BlInstruction.ISSU)
            .comments("B/L has been issued")
            .isToOrder(true)
            .timestamp(1658403878461L)
            .transferee("foo")
            .platformHost("localhost:8443")
            .build();

    EndorsementChainTransactionTO endorcementChainEntry2 =
        EndorsementChainTransactionTO.builder()
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

    ObjectMapper mapper = new ObjectMapper();
    String rawEblEnvelope = mapper.writeValueAsString(eblEnvelope);

    SignedEblEnvelopeTO signedEblEnvelopeTO = SignedEblEnvelopeTO.builder()
      .eblEnvelope(rawEblEnvelope)
      .envelopeHash("d56a93a7e9f86a2d895df818e0440bdca6ffe03246e2fee14131f2e66c84c75a")
      .signature("eyJiNjQiOmZhbHNlLCJjcml0IjpbImI2NCJdLCJhbGciOiJIUzI1NiJ9..5rPBT_XW-x7mjc1ubf4WwW1iV2YJyc4CCFxORIEaAEk")
      .build();

    List<SignedEblEnvelopeTO> endorcementChain = List.of(signedEblEnvelopeTO);

    assertEquals(expected, mapper.writeValueAsString(endorcementChain));
  }
}
