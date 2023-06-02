package org.dcsa.endorsementchain.service;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.TransferblockTO;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImportService {
  private final EndorsementChainEntryService endorsementChainEntryService;
  private final TransportDocumentService transportDocumentService;

  @Transactional
  public Optional<String> importEbl(TransferblockTO transferblock) {
    List<EndorsementChainEntry> parsedEndorsementChainEntries = parseEndorsementChainEntries(transferblock);
    return Optional.ofNullable(endorsementChainEntryService.saveEndorsementEntries(parsedEndorsementChainEntries));
  }

  private List<EndorsementChainEntry> parseEndorsementChainEntries(TransferblockTO transferblock) {

    List<EndorsementChainEntry> endorsementChainEntryList = transferblock.endorsementChain().stream()
      .map(signedEndorsementChainEntryTO ->
        validate(transferblock, signedEndorsementChainEntryTO))
      .toList();

    verifyTransportDocument(transferblock.document(), endorsementChainEntryList);

    return endorsementChainEntryList;
  }

  private EndorsementChainEntry validate(TransferblockTO transferblock, SignedEndorsementChainEntryTO signedEndorsementChainEntryTO) {
    EndorsementChainEntryTO parsedEndorsementChainEntry = endorsementChainEntryService.verifyEndorsementChainSignature(signedEndorsementChainEntryTO.signature());

    // Since the platformhost is the host of the originating transaction and all transactions within
    // an EBL envelope are from the same platform we can take any of the transactions to retrieve
    // the platformhost.
    String platformHost = parsedEndorsementChainEntry.transactions().get(0).platformHost();

    return endorsementChainEntryService.signedEndorsementEntryToEndorsementChainEntry(signedEndorsementChainEntryTO, parsedEndorsementChainEntry, transferblock.document(), platformHost);
  }

  private void verifyTransportDocument(String transferDocument, List<EndorsementChainEntry> endorsementChainEntryList) {
    List<String> documentHashes = endorsementChainEntryList.stream().map(envelope -> envelope.getTransportDocument().getDocumentHash()).toList();
    transportDocumentService.verifyDocumentHash(transferDocument, documentHashes);
  }
}
