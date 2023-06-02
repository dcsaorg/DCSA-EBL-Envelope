package org.dcsa.endorsementchain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.dcsa.endorsementchain.datafactories.EBLEnvelopeTODataFactory;
import org.dcsa.endorsementchain.datafactories.EndorsementChainEntryDataFactory;
import org.dcsa.endorsementchain.datafactories.EndorsementChainEntryTODataFactory;
import org.dcsa.endorsementchain.datafactories.SignedEndorsementChainEntryTODataFactory;
import org.dcsa.endorsementchain.exceptions.BadEnvelopeException;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.transferobjects.EBLEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

  @Mock
  EndorsementChainEntryService endorsementChainEntryService;
  @Mock TransportDocumentService transportDocumentService;

  @InjectMocks ImportService importService;

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  void testImportEbl() {
    List<EndorsementChainEntry> endorsementChainEntries = EndorsementChainEntryDataFactory.getEndorsementChainEntryList();
    List<String> rawEndorsementChainEntries = endorsementChainEntries.stream().map(parsedEndorsementChainEntry -> {
      try {
        return mapper.writeValueAsString(parsedEndorsementChainEntry);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Can't serialize EndorsementChainEntry");
      }
    }).toList();
    List<SignedEndorsementChainEntryTO> signedEndorsementChainEntries = SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTOList();

    EBLEnvelopeTO eblEnvelope = EBLEnvelopeTODataFactory.eblEnvelopeTO(rawEndorsementChainEntries.get(0), rawEndorsementChainEntries.get(1));
    List<EndorsementChainEntryTO> endorsementChainEntryTOs = EndorsementChainEntryTODataFactory.endorsementChainEntryTOList();

    when(endorsementChainEntryService.verifyEndorsementChainSignature(signedEndorsementChainEntries.get(0).signature()))
        .thenReturn(endorsementChainEntryTOs.get(0));
    when(endorsementChainEntryService.verifyEndorsementChainSignature(signedEndorsementChainEntries.get(1).signature()))
        .thenReturn(endorsementChainEntryTOs.get(1));
    when(endorsementChainEntryService.signedEndorsementEntryToEndorsementChainEntry(signedEndorsementChainEntries.get(0), endorsementChainEntryTOs.get(0), eblEnvelope.document(), "localhost:8443")).thenReturn(endorsementChainEntries.get(0));
    when(endorsementChainEntryService.signedEndorsementEntryToEndorsementChainEntry(signedEndorsementChainEntries.get(1), endorsementChainEntryTOs.get(1), eblEnvelope.document(), "localhost:8443")).thenReturn(endorsementChainEntries.get(1));
    when((endorsementChainEntryService.saveEndorsementEntries(endorsementChainEntries))).thenReturn("dummyHash");

    Optional<String> signedResponse = importService.importEbl(eblEnvelope);
    assertTrue(signedResponse.isPresent());
    assertEquals("dummyHash", signedResponse.get());
  }


  @Test
  void testImportEblReorderedEndorsementChainEntries() {
    List<EndorsementChainEntry> endorsementChainEntries = new ArrayList<>(EndorsementChainEntryDataFactory.getEndorsementChainEntryList());
    assertTrue(endorsementChainEntries.size() > 1);
    Collections.reverse(endorsementChainEntries);
    List<String> rawEndorsementChainEntries = endorsementChainEntries.stream().map(parsedEndorsementChainEntry -> {
      try {
        return mapper.writeValueAsString(parsedEndorsementChainEntry);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Can't serialize EndorsementChainEntry");
      }
    }).toList();
    List<SignedEndorsementChainEntryTO> signedEndorsementChainEntries = SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTOList();

    EBLEnvelopeTO eblEnvelope = EBLEnvelopeTODataFactory.eblEnvelopeTO(rawEndorsementChainEntries.get(0), rawEndorsementChainEntries.get(1));
    List<EndorsementChainEntryTO> endorsementChainEntryTOs = EndorsementChainEntryTODataFactory.endorsementChainEntryTOList();

    when(endorsementChainEntryService.verifyEndorsementChainSignature(signedEndorsementChainEntries.get(0).signature()))
      .thenReturn(endorsementChainEntryTOs.get(0));
    when(endorsementChainEntryService.verifyEndorsementChainSignature(signedEndorsementChainEntries.get(1).signature()))
      .thenReturn(endorsementChainEntryTOs.get(1));
    when(endorsementChainEntryService.signedEndorsementEntryToEndorsementChainEntry(signedEndorsementChainEntries.get(0), endorsementChainEntryTOs.get(0), eblEnvelope.document(), "localhost:8443")).thenReturn(endorsementChainEntries.get(0));
    when(endorsementChainEntryService.signedEndorsementEntryToEndorsementChainEntry(signedEndorsementChainEntries.get(1), endorsementChainEntryTOs.get(1), eblEnvelope.document(), "localhost:8443")).thenReturn(endorsementChainEntries.get(1));

    assertThrows(BadEnvelopeException.class, () -> importService.importEbl(eblEnvelope));
  }

  @Test
  void testImportEblWithoutEndorsementChain() {
    EBLEnvelopeTO eblEnvelope = EBLEnvelopeTO.builder()
      .document("Test document")
      .endorsementChain(Collections.emptyList())
      .build();

    Optional<String> signedResponse = importService.importEbl(eblEnvelope);
    assertTrue(signedResponse.isEmpty());
  }

}
