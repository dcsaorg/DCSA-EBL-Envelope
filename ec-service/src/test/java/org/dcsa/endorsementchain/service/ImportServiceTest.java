package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.datafactories.EndorsementChainEntryDataFactory;
import org.dcsa.endorsementchain.datafactories.EndorsementChainEntryTODataFactory;
import org.dcsa.endorsementchain.datafactories.SignedEndorsementChainEntryTODataFactory;
import org.dcsa.endorsementchain.datafactories.TransferblockTODataFactory;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
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

    TransferblockTO transferblock = TransferblockTODataFactory.transferblockTO(rawEndorsementChainEntries.get(0), rawEndorsementChainEntries.get(1));
    List<EndorsementChainEntryTO> endorsementChainEntryTOs = EndorsementChainEntryTODataFactory.endorsementChainEntryTOList();

    when(endorsementChainEntryService.verifyEndorsementChainSignature(signedEndorsementChainEntries.get(0).signature()))
        .thenReturn(endorsementChainEntryTOs.get(0));
    when(endorsementChainEntryService.verifyEndorsementChainSignature(signedEndorsementChainEntries.get(1).signature()))
        .thenReturn(endorsementChainEntryTOs.get(1));
    when(endorsementChainEntryService.signedEndorsementEntryToEndorsementChainEntry(signedEndorsementChainEntries.get(0), endorsementChainEntryTOs.get(0), transferblock.document(), "localhost:8443")).thenReturn(endorsementChainEntries.get(0));
    when(endorsementChainEntryService.signedEndorsementEntryToEndorsementChainEntry(signedEndorsementChainEntries.get(1), endorsementChainEntryTOs.get(1), transferblock.document(), "localhost:8443")).thenReturn(endorsementChainEntries.get(1));
    when((endorsementChainEntryService.saveEndorsementEntries(endorsementChainEntries))).thenReturn("dummyHash");

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
