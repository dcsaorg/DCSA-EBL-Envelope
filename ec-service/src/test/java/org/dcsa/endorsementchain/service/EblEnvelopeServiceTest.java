package org.dcsa.endorsementchain.service;

import org.dcsa.endorsementchain.datafactories.EblEnvelopeDataFactory;
import org.dcsa.endorsementchain.mapping.EblEnvelopeMapper;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.repository.EblEnvelopeRepository;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EblEnvelopeServiceTest {

  @Mock EblEnvelopeRepository repository;

  @Spy EblEnvelopeMapper mapper = Mappers.getMapper(EblEnvelopeMapper.class);

  @InjectMocks EblEnvelopeService service;

  private List<EblEnvelope> eblEnvelopeList;
  private EblEnvelope eblEnvelope;

  @BeforeEach
  void init() {
    eblEnvelope = EblEnvelopeDataFactory.getEblEnvelope();
    eblEnvelopeList = EblEnvelopeDataFactory.getEblEnvelopeList();
  }

  @Test
  void testConvertExistingEblEnvelopesToSignedEnvelopes() {
    List<SignedEblEnvelopeTO> signedEnvelopes = service.convertExistingEblEnvelopesToSignedEnvelopes(eblEnvelopeList);
    assertEquals(2, signedEnvelopes.size());
    assertNotNull(signedEnvelopes.get(0).signature());
  }

  @Test
  void testConvertExistingEblEnvelopesToSignedEnvelopesNull() {
    List<SignedEblEnvelopeTO> signedEnvelopes = service.convertExistingEblEnvelopesToSignedEnvelopes(Collections.emptyList());
    assertEquals(0, signedEnvelopes.size());
  }

  @Test
  void testFindPreviousEnvelopeHash() {
    String previousEnvelopeHash = service.findPreviousEblEnvelopeHash(eblEnvelopeList);
    assertEquals("a25286672be331c6770fa590f8eb7ab7cf105fd76f0db4b7cabd258a5953482e", previousEnvelopeHash);
  }

  @Test
  void testFindPreviousEnvelopeHashNotExists() {
    String previousEnvelopeHash = service.findPreviousEblEnvelopeHash(List.of(eblEnvelope));
    assertNull(previousEnvelopeHash);
  }

  @Test
  void testFindPreviousEnvelopeHashNotExistsNull() {
    String previousEnvelopeHash = service.findPreviousEblEnvelopeHash(null);
    assertNull(previousEnvelopeHash);
  }

  @Test
  void testFindPreviousEblEnvelopes() {
    when(repository.findByTransportDocument_DocumentHash(any())).thenReturn(Optional.of(eblEnvelopeList));
    List<EblEnvelope> foundPreviousEblEnvelopes = service.findPreviousEblEnvelopes("testDocumentHash");
    assertEquals(2, foundPreviousEblEnvelopes.size());
  }

  @Test
  void testFindPreviousEblEnvelopesNoneFound() {
    when(repository.findByTransportDocument_DocumentHash(any())).thenReturn(Optional.empty());
    List<EblEnvelope> foundPreviousEblEnvelopes = service.findPreviousEblEnvelopes("testDocumentHash");
    assertEquals(0, foundPreviousEblEnvelopes.size());
  }
}
