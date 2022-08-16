package org.dcsa.endorsementchain.service;

import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.TransferblockTO;
import org.dcsa.endorsementchain.unofficial.service.TransactionService;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {
  private final EblEnvelopeService eblEnvelopeService;
  private final TransactionService transactionService;
  private final TransportDocumentService transportDocumentService;

  @Transactional
  public String importEbl(TransferblockTO transferblock) {
    List<EblEnvelope> parsedEblEnvelopes = parseEblEnvelopes(transferblock.endorsementChain());
    return saveTransferBlock(transferblock, parsedEblEnvelopes);
  }

  private String saveTransferBlock(TransferblockTO transferblock, List<EblEnvelope> eblEnvelopes) {
    transportDocumentService.saveTransportDocument(transferblock.document());
    String signedResponse = eblEnvelopeService.saveEblEnvelopes(eblEnvelopes);


    eblEnvelopes.forEach(
        eblEnvelope -> transactionService.saveImportedTransactions(eblEnvelope.getTransactions()));
    return signedResponse;
  }

  private List<EblEnvelope> parseEblEnvelopes(List<SignedEblEnvelopeTO> endorsementChain) {
    return endorsementChain.stream().map(eblEnvelopeService::verifyEnvelopeSignature).toList();
  }
}
