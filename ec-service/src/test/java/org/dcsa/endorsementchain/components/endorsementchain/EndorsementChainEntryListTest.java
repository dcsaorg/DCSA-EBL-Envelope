package org.dcsa.endorsementchain.components.endorsementchain;

import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EndorsementChainEntryListTest {

  List<EndorsementChainEntry> endorsementChainEntries;
  Map<String, EndorsementChainEntry> endorsementChainEntryMap;

  @BeforeEach
  void init() {
    EndorsementChainEntry endorsementChainEntry1 =
        EndorsementChainEntry.builder().envelopeHash("1").previousEnvelopeHash(null).build();
    EndorsementChainEntry endorsementChainEntry2 =
        EndorsementChainEntry.builder().envelopeHash("2").previousEnvelopeHash("1").build();
    EndorsementChainEntry endorsementChainEntry3 =
        EndorsementChainEntry.builder().envelopeHash("3").previousEnvelopeHash("2").build();
    EndorsementChainEntry endorsementChainEntry4 =
        EndorsementChainEntry.builder().envelopeHash("4").previousEnvelopeHash("3").build();

    endorsementChainEntries = List.of(endorsementChainEntry3, endorsementChainEntry1, endorsementChainEntry4, endorsementChainEntry2);
    endorsementChainEntryMap =
        endorsementChainEntries.stream()
            .collect(Collectors.toMap(EndorsementChainEntry::getPreviousEnvelopeHash, envelope -> envelope));
  }

  @Test
  void testReEndorsementChainEntryList() {
    List<EndorsementChainEntry> orderedEndorsementChainEntries = EndorsementChainEntryList.order(endorsementChainEntries);

    assertEquals("1", orderedEndorsementChainEntries.get(0).getEnvelopeHash());
    assertEquals("3", orderedEndorsementChainEntries.get(2).getEnvelopeHash());
    assertEquals(endorsementChainEntries.size(), orderedEndorsementChainEntries.size());
  }

  @Test
  void testGetFirstEndorsementChainEntry() {
    Optional<EndorsementChainEntry> firstEndorsementChainEntry = EndorsementChainEntryList.first(endorsementChainEntryMap);

    assertTrue(firstEndorsementChainEntry.isPresent());
    assertEquals("1", firstEndorsementChainEntry.get().getEnvelopeHash());
    assertNull(firstEndorsementChainEntry.get().getPreviousEnvelopeHash());
  }

  @Test
  void testGetEndorsementChainEntry() {
    Optional<EndorsementChainEntry> lastEndorsementChainEntry = EndorsementChainEntryList.last(endorsementChainEntries);

    assertTrue(lastEndorsementChainEntry.isPresent());
    assertEquals("4", lastEndorsementChainEntry.get().getEnvelopeHash());
    assertEquals("3", lastEndorsementChainEntry.get().getPreviousEnvelopeHash());
  }

  @Test
  void testGetLastNoEndorsementChainEntry() {
    Optional<EndorsementChainEntry> lastEndorsementChainEntry = EndorsementChainEntryList.last(Collections.emptyList());
    assertTrue(lastEndorsementChainEntry.isEmpty());
  }

  @Test
  void testGetLastEndorsementChainEntryWithNoEndorsementChainEntryHash() {
    Optional<EndorsementChainEntry> lastEndorsementChainEntry = EndorsementChainEntryList.last(List.of(EndorsementChainEntry.builder().build()));
    assertTrue(lastEndorsementChainEntry.isEmpty());
  }
}
