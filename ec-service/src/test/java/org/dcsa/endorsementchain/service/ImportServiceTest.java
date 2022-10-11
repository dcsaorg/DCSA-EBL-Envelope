package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeDataFactory;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeTODataFactory;
import org.dcsa.endorsementchain.datafactories.SignedEblEnvelopeTODataFactory;
import org.dcsa.endorsementchain.datafactories.TransferblockTODataFactory;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.TransferblockTO;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

  @Mock EblEnvelopeService eblEnvelopeService;
  @Mock TransportDocumentService transportDocumentService;

  @InjectMocks ImportService importService;

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  void testImportEbl() {
    List<EblEnvelope> eblEnvelopes = EblEnvelopeDataFactory.getEblEnvelopeList();
    List<String> rawEblEnvelopes = eblEnvelopes.stream().map(parsedEblEnvelope -> {
      try {
        return mapper.writeValueAsString(parsedEblEnvelope);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Can't serialize eblEnvelope");
      }
    }).toList();
    List<SignedEblEnvelopeTO> signedEblEnvelopes = SignedEblEnvelopeTODataFactory.signedEblEnvelopeTOList();

    TransferblockTO transferblock = TransferblockTODataFactory.transferblockTO(rawEblEnvelopes.get(0), rawEblEnvelopes.get(1));
    List<EblEnvelopeTO> eblEnvelopeTOs = EblEnvelopeTODataFactory.eblEnvelopeTOList();

    when(eblEnvelopeService.verifyEndorsementChainSignature(signedEblEnvelopes.get(0).signature()))
        .thenReturn(eblEnvelopeTOs.get(0));
    when(eblEnvelopeService.verifyEndorsementChainSignature(signedEblEnvelopes.get(1).signature()))
        .thenReturn(eblEnvelopeTOs.get(1));
    when(eblEnvelopeService.signedEblEnvelopeToEblEnvelope(signedEblEnvelopes.get(0), eblEnvelopeTOs.get(0), transferblock.document(), "localhost:8443")).thenReturn(eblEnvelopes.get(0));
    when(eblEnvelopeService.signedEblEnvelopeToEblEnvelope(signedEblEnvelopes.get(1), eblEnvelopeTOs.get(1), transferblock.document(), "localhost:8443")).thenReturn(eblEnvelopes.get(1));
    when((eblEnvelopeService.saveEblEnvelopes(eblEnvelopes))).thenReturn("dummyHash");

    Optional<String> signedResponse = importService.importEbl(transferblock);
    assertTrue(signedResponse.isPresent());
    assertEquals("dummyHash", signedResponse.get());
  }

  @Test
  void testImportEblWithoutEndorsementChain() {
    TransferblockTO transferblock = TransferblockTO.builder()
      .document("Test document")
      .endorsementChain(Collections.emptyList())
      .build();

    Optional<String> signedResponse = importService.importEbl(transferblock);
    assertTrue(signedResponse.isEmpty());
  }

}
