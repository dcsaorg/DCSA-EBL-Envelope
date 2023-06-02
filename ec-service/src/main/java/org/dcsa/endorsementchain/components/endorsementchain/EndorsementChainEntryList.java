package org.dcsa.endorsementchain.components.endorsementchain;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class EndorsementChainEntryList {
  public List<EndorsementChainEntry> order(List<EndorsementChainEntry> envelopeList) {

    Map<String, EndorsementChainEntry> endorsementChainEntryMap =
        envelopeList.stream()
            .collect(Collectors.toMap(EndorsementChainEntry::getPreviousEnvelopeHash, envelope -> envelope));
    EndorsementChainEntry firstEnvelope = first(endorsementChainEntryMap).orElse(null);

    List<EndorsementChainEntry> sortedList = new ArrayList<>();
    EndorsementChainEntry envelope = firstEnvelope;
    while (envelope != null && envelope.getEnvelopeHash() != null) {
      sortedList.add(envelope);
      envelope = next(endorsementChainEntryMap, envelope.getEnvelopeHash()).orElse(null);
    }

    return sortedList;
  }

  public Optional<EndorsementChainEntry> last(List<EndorsementChainEntry> endorsementChainEntries) {
    return Optional.ofNullable(endorsementChainEntries)
        .filter(envelopes -> !envelopes.isEmpty())
        .map(envelopes -> order(endorsementChainEntries))
        .filter(envelopes -> !envelopes.isEmpty())
        .map(envelopes -> envelopes.get(envelopes.size() - 1));
  }

  Optional<EndorsementChainEntry> first(Map<String, EndorsementChainEntry> endorsementChainEntries) {
    return Optional.ofNullable(endorsementChainEntries.get(null));
  }

  private Optional<EndorsementChainEntry> next(Map<String, EndorsementChainEntry> endorsementChainEntries, String envelopeHash) {
    return Optional.ofNullable(endorsementChainEntries.get(envelopeHash));
  }
}
