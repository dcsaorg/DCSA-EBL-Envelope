package org.dcsa.endorsementchain.service;

import org.checkerframework.checker.nullness.qual.AssertNonNullIfNonNull;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeDataFactory;
import org.dcsa.endorsementchain.datafactories.TransferblockTODataFactory;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.TransferblockTO;
import org.dcsa.endorsementchain.unofficial.datafactories.TransactionDataFactory;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
import org.dcsa.endorsementchain.unofficial.service.TransactionService;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

  @Mock EblEnvelopeService eblEnvelopeService;
  @Mock TransactionService transactionService;
  @Mock TransportDocumentService transportDocumentService;

  @InjectMocks ImportService importService;

  @Test
  void testImportEbl() {
    TransferblockTO transferblock = TransferblockTODataFactory.transferblockTO();
    List<SignedEblEnvelopeTO> signedEblEnvelopes = transferblock.endorsementChain();
    List<EblEnvelope> eblEnvelopes = EblEnvelopeDataFactory.getEblEnvelopeList();

    when(eblEnvelopeService.verifyEnvelopeSignature(signedEblEnvelopes.get(0)))
        .thenReturn(eblEnvelopes.get(0));
    when(eblEnvelopeService.verifyEnvelopeSignature(signedEblEnvelopes.get(1)))
        .thenReturn(eblEnvelopes.get(1));
    when((eblEnvelopeService.saveEblEnvelopes(eblEnvelopes))).thenReturn("dummyHash");
    when(transportDocumentService.saveTransportDocument(transferblock.document()))
        .thenReturn(Optional.of(TransportDocumentDataFactory.transportDocumentHash()));
    when(transactionService.saveImportedTransactions(any()))
        .thenReturn(TransactionDataFactory.transactionEntityList());

    String signedResponse = importService.importEbl(transferblock);
    assertNotNull(signedResponse);
    assertEquals("dummyHash", signedResponse);
  }

  @Test
  void testImportEblWithoutEndorsementChain() {
    TransferblockTO transferblock = TransferblockTO.builder()
      .document("Test document")
      .endorsementChain(Collections.emptyList())
      .build();
    List<SignedEblEnvelopeTO> signedEblEnvelopes = transferblock.endorsementChain();
    List<EblEnvelope> eblEnvelopes = EblEnvelopeDataFactory.getEblEnvelopeList();

    when(transportDocumentService.saveTransportDocument(transferblock.document()))
      .thenReturn(Optional.of(TransportDocumentDataFactory.transportDocumentHash()));

    String signedResponse = importService.importEbl(transferblock);
    assertNull(signedResponse);
  }

}
