package org.dcsa.endorsementchain.components.eblenvelope;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@UtilityClass
public class EblEnvelopeList {
  public List<EblEnvelope> order(List<EblEnvelope> envelopeList) {
    EblEnvelope firstEnvelope = first(envelopeList).orElse(null);

    List<EblEnvelope> sortedList = new ArrayList<>();
    EblEnvelope envelope = firstEnvelope;
    while (envelope != null) {
      sortedList.add(envelope);
      envelope = next(envelopeList, envelope.getEnvelopeHash()).orElse(null);
    }

    return sortedList;
  }

  public Optional<EblEnvelope> last(List<EblEnvelope> eblEnvelopes) {
    return Optional.ofNullable(eblEnvelopes)
        .filter(envelopes -> !envelopes.isEmpty())
        .map(envelopes -> order(eblEnvelopes).get(envelopes.size() - 1));
  }

  public Optional<EblEnvelope> first(List<EblEnvelope> eblEnvelopes) {
    return eblEnvelopes.stream()
        .filter(eblEnvelope -> Objects.isNull(eblEnvelope.getPreviousEnvelopeHash()))
        .findFirst();
  }

  private Optional<EblEnvelope> next(List<EblEnvelope> eblEnvelopes, String envelopeHash) {
    return eblEnvelopes.stream()
        .filter(eblEnvelope -> envelopeHash.equals(eblEnvelope.getPreviousEnvelopeHash()))
        .findFirst();
  }
}
