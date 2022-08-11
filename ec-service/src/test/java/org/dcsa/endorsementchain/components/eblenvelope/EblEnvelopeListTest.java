package org.dcsa.endorsementchain.components.eblenvelope;

import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EblEnvelopeListTest {

  List<EblEnvelope> eblEnvelopes;
  Map<String, EblEnvelope> eblEnvelopeMap;

  @BeforeEach
  void init() {
    EblEnvelope eblEnvelope1 =
        EblEnvelope.builder().envelopeHash("1").previousEnvelopeHash(null).build();
    EblEnvelope eblEnvelope2 =
        EblEnvelope.builder().envelopeHash("2").previousEnvelopeHash("1").build();
    EblEnvelope eblEnvelope3 =
        EblEnvelope.builder().envelopeHash("3").previousEnvelopeHash("2").build();
    EblEnvelope eblEnvelope4 =
        EblEnvelope.builder().envelopeHash("4").previousEnvelopeHash("3").build();

    eblEnvelopes = List.of(eblEnvelope3, eblEnvelope1, eblEnvelope4, eblEnvelope2);
    eblEnvelopeMap =
        eblEnvelopes.stream()
            .collect(Collectors.toMap(EblEnvelope::getPreviousEnvelopeHash, envelope -> envelope));
  }

  @Test
  void testReOrderEblEnvelopeList() {
    List<EblEnvelope> orderedEblEnvelopes = EblEnvelopeList.order(eblEnvelopes);

    assertEquals("1", orderedEblEnvelopes.get(0).getEnvelopeHash());
    assertEquals("3", orderedEblEnvelopes.get(2).getEnvelopeHash());
    assertEquals(eblEnvelopes.size(), orderedEblEnvelopes.size());
  }

  @Test
  void testGetFirstEblEnvelope() {
    Optional<EblEnvelope> firstEblEnvelope = EblEnvelopeList.first(eblEnvelopeMap);

    assertTrue(firstEblEnvelope.isPresent());
    assertEquals("1", firstEblEnvelope.get().getEnvelopeHash());
    assertNull(firstEblEnvelope.get().getPreviousEnvelopeHash());
  }

  @Test
  void testGetLastEblEnvelope() {
    Optional<EblEnvelope> lastEblEnvelope = EblEnvelopeList.last(eblEnvelopes);

    assertTrue(lastEblEnvelope.isPresent());
    assertEquals("4", lastEblEnvelope.get().getEnvelopeHash());
    assertEquals("3", lastEblEnvelope.get().getPreviousEnvelopeHash());
  }

  @Test
  void testGetLastNoEblEnvelopesPresent() {
    Optional<EblEnvelope> lastEblEnvelope = EblEnvelopeList.last(Collections.emptyList());
    assertTrue(lastEblEnvelope.isEmpty());
  }
}
