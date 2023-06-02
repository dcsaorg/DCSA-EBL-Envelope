package org.dcsa.endorsementchain.service;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.exceptions.BadEnvelopeException;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.EBLEnvelopeTO;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportService {
  private final EndorsementChainEntryService endorsementChainEntryService;
  private final TransportDocumentService transportDocumentService;

  @Transactional
  public Optional<String> importEbl(EBLEnvelopeTO eblEnvelope) {
    List<EndorsementChainEntry> parsedEndorsementChainEntries = parseEndorsementChainEntries(eblEnvelope);
    return Optional.ofNullable(endorsementChainEntryService.saveEndorsementEntries(parsedEndorsementChainEntries));
  }

  private void verifyEndorsementChainOrderConsistency(List<EndorsementChainEntry> endorsementChainEntryList) {
    String actualPreviousHash = null;
    int index = 0;
    for (EndorsementChainEntry endorsementChainEntry : endorsementChainEntryList) {
      String expectedPreviousHash = endorsementChainEntry.getPreviousEnvelopeHash();
      String currentHash = endorsementChainEntry.getEnvelopeHash();

      if (Objects.equals(actualPreviousHash, expectedPreviousHash)) {
        ++index;
        actualPreviousHash = currentHash;
        continue;
      }

      // The reference implementation enforces strict ordering. Implementors *may* choose to be more lenient
      // when receiving. At this point, we could just reject the envelope with a generic message but let us
      // provide a bit more context just for the sake of it.
      List<String> allHashes = endorsementChainEntryList.stream()
        .map(EndorsementChainEntry::getEnvelopeHash)
        .toList();
      int actualIndexOfHash = allHashes.indexOf(expectedPreviousHash);
      if (actualIndexOfHash != -1) {
        throw new BadEnvelopeException("Endorsement Chain Entries are out of order. Entry at index " + index
          + " expected the previous hash to be " + expectedPreviousHash + ", but it was found at index "
          + actualIndexOfHash);
      }
      throw new BadEnvelopeException("The Endorsement Chain Entry at index " + index + " references to a non-existent"
        + " hash " + expectedPreviousHash + " as the previous hash.  The previous entry instead had the hash "
        + actualPreviousHash);
    }
  }

  private List<EndorsementChainEntry> parseEndorsementChainEntries(EBLEnvelopeTO eblEnvelope) {

    List<EndorsementChainEntry> endorsementChainEntryList = eblEnvelope.endorsementChain().stream()
      .map(signedEndorsementChainEntryTO ->
        validate(eblEnvelope, signedEndorsementChainEntryTO))
      .toList();

    verifyEndorsementChainOrderConsistency(endorsementChainEntryList);

    verifyTransportDocument(eblEnvelope.document(), endorsementChainEntryList);

    return endorsementChainEntryList;
  }

  private EndorsementChainEntry validate(EBLEnvelopeTO eblEnvelope, SignedEndorsementChainEntryTO signedEndorsementChainEntryTO) {
    EndorsementChainEntryTO parsedEndorsementChainEntry = endorsementChainEntryService.verifyEndorsementChainSignature(signedEndorsementChainEntryTO.signature());

    // Since the platformhost is the host of the originating transaction and all transactions within
    // an EBL envelope are from the same platform we can take any of the transactions to retrieve
    // the platformhost.
    String platformHost = parsedEndorsementChainEntry.transactions().get(0).platformHost();

    return endorsementChainEntryService.signedEndorsementEntryToEndorsementChainEntry(signedEndorsementChainEntryTO, parsedEndorsementChainEntry, eblEnvelope.document(), platformHost);
  }

  private void verifyTransportDocument(String transferDocument, List<EndorsementChainEntry> endorsementChainEntryList) {
    List<String> documentHashes = endorsementChainEntryList.stream().map(envelope -> envelope.getTransportDocument().getDocumentHash()).toList();
    transportDocumentService.verifyDocumentHash(transferDocument, documentHashes);
  }
}
