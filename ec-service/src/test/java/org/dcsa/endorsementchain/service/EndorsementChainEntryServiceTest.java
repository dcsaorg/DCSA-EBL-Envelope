package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.components.endorsementchain.EndorsementChainEntrySignature;
import org.dcsa.endorsementchain.datafactories.EndorsementChainEntryDataFactory;
import org.dcsa.endorsementchain.datafactories.EndorsementChainEntryTODataFactory;
import org.dcsa.endorsementchain.datafactories.SignedEndorsementChainEntryTODataFactory;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.EndorsementChainEntryRepository;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
import org.dcsa.endorsementchain.unofficial.datafactories.TransactionDataFactory;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
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
class EndorsementChainEntryServiceTest {

  @Mock
  EndorsementChainEntryRepository repository;
  @Mock
  EndorsementChainEntrySignature signature;
  @Spy ObjectMapper objectMapper;

  @Spy TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

  @InjectMocks
  EndorsementChainEntryService service;

  private List<EndorsementChainEntry> endorsementChainEntryList;
  private EndorsementChainEntry endorsementChainEntry;
  private TransportDocument transportDocument;
  private EndorsementChainEntryTO endorsementChainEntryTO;

  @BeforeEach
  void init() {
    endorsementChainEntry = EndorsementChainEntryDataFactory.getEndorsementChainEntry();
    endorsementChainEntryList = EndorsementChainEntryDataFactory.getEndorsementChainEntryList();
    transportDocument = TransportDocumentDataFactory.transportDocumentEntityWithTransactions();
    endorsementChainEntryTO = EndorsementChainEntryTODataFactory.endorsementChainEntryTO();
  }

  @Test
  void testConvertExistingEndorsementChainEntriesToSignedEntries() {
    List<SignedEndorsementChainEntryTO> signedEntries =
        service.convertExistingEndorsementChainEntriesToSignedEntries(endorsementChainEntryList);
    assertEquals(2, signedEntries.size());
    assertNotNull(signedEntries.get(0).signature());
  }

  @Test
  void testConvertExistingEndorsementChainEntriesToSignedEntriesNull() {
    List<SignedEndorsementChainEntryTO> signedEnvelopes =
        service.convertExistingEndorsementChainEntriesToSignedEntries(Collections.emptyList());
    assertEquals(0, signedEnvelopes.size());
  }

  @Test
  void testFindPreviousEndorsementChainEntryHash() {
    String previousEndorsementChainEntryHash = service.findPreviousEndorsementChainEntryHash(endorsementChainEntryList);
    assertEquals(
        "a25286672be331c6770fa590f8eb7ab7cf105fd76f0db4b7cabd258a5953482e", previousEndorsementChainEntryHash);
  }

  @Test
  void testFindPreviousEnvelopeHashNotExists() {
    String previousEnvelopeHash = service.findPreviousEndorsementChainEntryHash(List.of(endorsementChainEntry));
    assertNull(previousEnvelopeHash);
  }

  @Test
  void testFindPreviousEnvelopeHashNotExistsNull() {
    String previousEnvelopeHash = service.findPreviousEndorsementChainEntryHash(null);
    assertNull(previousEnvelopeHash);
  }

  @Test
  void testFindPreviousEndorsementChainEntries() {
    when(repository.findByTransportDocument_DocumentHash(any()))
        .thenReturn(Optional.of(endorsementChainEntryList));
    List<EndorsementChainEntry> foundPreviousEndorsementChainEntries =
        service.findPreviousEndorsementChainEntries("testDocumentHash");
    assertEquals(2, foundPreviousEndorsementChainEntries.size());
  }

  @Test
  void testFindPreviousEndorsementChainEntriesNoneFound() {
    when(repository.findByTransportDocument_DocumentHash(any())).thenReturn(Optional.empty());
    List<EndorsementChainEntry> foundPreviousEndorsementChainEntries =
        service.findPreviousEndorsementChainEntries("testDocumentHash");
    assertEquals(0, foundPreviousEndorsementChainEntries.size());
  }

  @Test
  void testCreateEndorsementChainEntry() {
    String documentHash = transportDocument.getDocumentHash();
    List<EndorsementChainTransactionTO> transactions =
        List.of(TransactionDataFactory.endorsementChainTransaction());
    EndorsementChainEntryTO envelope =
        service.createEndorsementChainEntry(
            documentHash, transactions, endorsementChainEntry.getPreviousEnvelopeHash());
    assertEquals(documentHash, envelope.documentHash());
    assertEquals(transactions.size(), envelope.transactions().size());
    assertEquals(endorsementChainEntry.getPreviousEnvelopeHash(), envelope.previousEnvelopeHash());
  }

  @Test
  void testCreateEndorsementChainEntryWithNullTransactions() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    List<EndorsementChainTransactionTO> transactions =
        List.of(TransactionDataFactory.endorsementChainTransaction());
    EndorsementChainEntryTO envelope =
        service.createEndorsementChainEntry(documentHash, null, endorsementChainEntry.getPreviousEnvelopeHash());
    assertEquals(documentHash, envelope.documentHash());
    assertNull(envelope.transactions());
    assertEquals(endorsementChainEntry.getPreviousEnvelopeHash(), envelope.previousEnvelopeHash());
  }

  @Test
  void testExportEndorsementChainEntry() {
    when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    when(signature.createSignedEndorsementChainEntry(any())).thenReturn(SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO());

    SignedEndorsementChainEntryTO response = service.exportEndorsementChainEntry(transportDocument, endorsementChainEntryTO);

    assertEquals(SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().envelopeHash(), response.envelopeHash());
    assertEquals(SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().signature(), response.signature());
  }

  @Test
  void testExportEnvelopeSignFailed() {
    when(signature.createSignedEndorsementChainEntry(any()))
        .thenThrow(
            ConcreteRequestErrorMessageException.internalServerError(
                "Unable to generate the JWS Object"));

    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.exportEndorsementChainEntry(transportDocument, endorsementChainEntryTO));

    assertEquals("Unable to generate the JWS Object", returnedException.getMessage());
  }

  @Test
  void testVerifyResponseSignatureInvalid() {
    when(signature.verifyEndorsementChainHash(any(), any(), any())).thenReturn(false);

    String envelopeHash = SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().envelopeHash();
    String signature = SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().signature();

    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.verifyEndorsementChainEntryResponseSignature("localhost:8443", envelopeHash, signature));

    assertEquals("Signature not valid", returnedException.getMessage());
  }

  @Test
  void testVerifyResponseSignatureValid() {
    when(signature.verifyEndorsementChainHash(any(), any(), any())).thenReturn(true);

    String response =
        service.verifyEndorsementChainEntryResponseSignature(
            "localhost:8443",
          SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().envelopeHash(),
          SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().signature());
    assertEquals(SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().signature(), response);
  }

  @Test
  void testParseEndorsementChainEntryInvalidEndorsementChainEntry() {
    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.verifyEndorsementChainSignature("invalid EBL envelope"));

    assertEquals("Provided EBL envelope is not valid", returnedException.getMessage());
  }

  @Test
  void testParseEndorsementChainEntryValidEndorsementChainEntry() {
    when(signature.verifySignature(any(), any())).thenReturn(true);
    EndorsementChainEntryTO endorsementChainEntryTO = service.verifyEndorsementChainSignature(SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().signature());

    assertNotNull(endorsementChainEntryTO);
    assertEquals(1, endorsementChainEntryTO.transactions().size());
    assertEquals("fd868c82e99777b472a1677390d954dbb0131cb3b0f55c8ef51969856410d38e", endorsementChainEntryTO.documentHash());
    assertNull(endorsementChainEntryTO.previousEnvelopeHash());
  }

  @Test
  void testVerifyEnvelopeSignatureInvalid() {
    // TODO: Double check why IntelliJ thinks this is unused and remove it if unnecessary
    SignedEndorsementChainEntryTO invalidSignedEndorsementChainEntry =
      SignedEndorsementChainEntryTO.builder()
        .envelopeHash(SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().envelopeHash())
        .signature(SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO().signature())
        .build();

    when(signature.verifySignature(any(), any())).thenReturn(false);
    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.verifyEndorsementChainSignature(SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTOWithInvalidSignature().signature()));

    assertEquals("Signature could not be validated", returnedException.getMessage());
  }

  @Test
  void testSaveEndorsementChainEntry() {
    when(repository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
    when(signature.sign(any())).thenReturn("dummySignature");

    String signature = service.saveEndorsementEntries(endorsementChainEntryList);
    assertNotNull(signature);
  }

  @Test
  void testSaveEndorsementChainEntryWithoutEndorsementChainEntryHash() {
    List<EndorsementChainEntry> listOfInvalidEndorsementChainEntry =
        List.of(
            EndorsementChainEntry.builder()
                .transactions(EndorsementChainEntryDataFactory.getEndorsementChainEntry().getTransactions())
                .transportDocument(EndorsementChainEntryDataFactory.getEndorsementChainEntry().getTransportDocument())
                .build());

    when(repository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);

    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.saveEndorsementEntries(listOfInvalidEndorsementChainEntry));
    assertEquals("Could not find a Envelope Hash on the EndorsementChainEntry", returnedException.getMessage());
  }
}
