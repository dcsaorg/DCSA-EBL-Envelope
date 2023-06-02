package org.dcsa.endorsementchain.service;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
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
  private final EblEnvelopeService eblEnvelopeService;
  private final TransportDocumentService transportDocumentService;

  @Transactional
  public Optional<String> importEbl(TransferblockTO transferblock) {
    List<EblEnvelope> parsedEblEnvelopes = parseEblEnvelopes(transferblock);
    return Optional.ofNullable(eblEnvelopeService.saveEblEnvelopes(parsedEblEnvelopes));
  }

  private List<EblEnvelope> parseEblEnvelopes(TransferblockTO transferblock) {

    List<EblEnvelope> eblEnvelopeList = transferblock.endorsementChain().stream()
      .map(signedEblEnvelopeTO ->
        validate(transferblock, signedEblEnvelopeTO))
      .toList();

    verifyTransportDocument(transferblock.document(), eblEnvelopeList);

    return eblEnvelopeList;
  }

  private EblEnvelope validate(TransferblockTO transferblock, SignedEndorsementChainEntryTO signedEndorsementChainEntryTO) {
    EblEnvelopeTO parsedEblEnvelope = eblEnvelopeService.verifyEndorsementChainSignature(signedEndorsementChainEntryTO.signature());

    // Since the platformhost is the host of the originating transaction and all transactions within
    // an EBL envelope are from the same platform we can take any of the transactions to retrieve
    // the platformhost.
    String platformHost = parsedEblEnvelope.transactions().get(0).platformHost();

    return eblEnvelopeService.signedEblEnvelopeToEblEnvelope(signedEndorsementChainEntryTO, parsedEblEnvelope, transferblock.document(), platformHost);
  }

  private void verifyTransportDocument(String transferDocument, List<EblEnvelope> eblEnvelopeList) {
    List<String> documentHashes = eblEnvelopeList.stream().map(envelope -> envelope.getTransportDocument().getDocumentHash()).toList();
    transportDocumentService.verifyDocumentHash(transferDocument, documentHashes);
  }
}
