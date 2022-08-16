package org.dcsa.endorsementchain.components.eblenvelope;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class EblEnvelopeList {
  public List<EblEnvelope> order(List<EblEnvelope> envelopeList) {

    Map<String, EblEnvelope> eblEnvelopeMap =
        envelopeList.stream()
            .collect(Collectors.toMap(EblEnvelope::getPreviousEnvelopeHash, envelope -> envelope));
    EblEnvelope firstEnvelope = first(eblEnvelopeMap).orElse(null);

    List<EblEnvelope> sortedList = new ArrayList<>();
    EblEnvelope envelope = firstEnvelope;
    while (envelope != null && envelope.getEnvelopeHash() != null) {
      sortedList.add(envelope);
      envelope = next(eblEnvelopeMap, envelope.getEnvelopeHash()).orElse(null);
    }

    return sortedList;
  }

  public Optional<EblEnvelope> last(List<EblEnvelope> eblEnvelopes) {
    return Optional.ofNullable(eblEnvelopes)
        .filter(envelopes -> !envelopes.isEmpty())
        .map(envelopes -> order(eblEnvelopes))
        .filter(envelopes -> !envelopes.isEmpty())
        .map(envelopes -> envelopes.get(envelopes.size() - 1));
  }

  Optional<EblEnvelope> first(Map<String, EblEnvelope> eblEnvelopes) {
    return Optional.ofNullable(eblEnvelopes.get(null));
  }

  private Optional<EblEnvelope> next(Map<String, EblEnvelope> eblEnvelopes, String envelopeHash) {
    return Optional.ofNullable(eblEnvelopes.get(envelopeHash));
  }
}
