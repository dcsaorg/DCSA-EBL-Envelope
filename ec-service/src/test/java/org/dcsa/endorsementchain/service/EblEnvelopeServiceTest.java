package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeSignature;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeDataFactory;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeTODataFactory;
import org.dcsa.endorsementchain.datafactories.SignedEblEnvelopeTODataFactory;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.EblEnvelopeRepository;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
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
class EblEnvelopeServiceTest {

  @Mock EblEnvelopeRepository repository;
  @Mock EblEnvelopeSignature signature;
  @Spy ObjectMapper objectMapper;

  @Spy TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

  @InjectMocks EblEnvelopeService service;

  private List<EblEnvelope> eblEnvelopeList;
  private EblEnvelope eblEnvelope;
  private TransportDocument transportDocument;
  private EblEnvelopeTO eblEnvelopeTO;

  @BeforeEach
  void init() {
    eblEnvelope = EblEnvelopeDataFactory.getEblEnvelope();
    eblEnvelopeList = EblEnvelopeDataFactory.getEblEnvelopeList();
    transportDocument = TransportDocumentDataFactory.transportDocumentEntityWithTransactions();
    eblEnvelopeTO = EblEnvelopeTODataFactory.eblEnvelopeTO();
  }

  @Test
  void testConvertExistingEblEnvelopesToSignedEnvelopes() {
    List<SignedEndorsementChainEntryTO> signedEnvelopes =
        service.convertExistingEblEnvelopesToSignedEnvelopes(eblEnvelopeList);
    assertEquals(2, signedEnvelopes.size());
    assertNotNull(signedEnvelopes.get(0).signature());
  }

  @Test
  void testConvertExistingEblEnvelopesToSignedEnvelopesNull() {
    List<SignedEndorsementChainEntryTO> signedEnvelopes =
        service.convertExistingEblEnvelopesToSignedEnvelopes(Collections.emptyList());
    assertEquals(0, signedEnvelopes.size());
  }

  @Test
  void testFindPreviousEnvelopeHash() {
    String previousEnvelopeHash = service.findPreviousEblEnvelopeHash(eblEnvelopeList);
    assertEquals(
        "a25286672be331c6770fa590f8eb7ab7cf105fd76f0db4b7cabd258a5953482e", previousEnvelopeHash);
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
    when(repository.findByTransportDocument_DocumentHash(any()))
        .thenReturn(Optional.of(eblEnvelopeList));
    List<EblEnvelope> foundPreviousEblEnvelopes =
        service.findPreviousEblEnvelopes("testDocumentHash");
    assertEquals(2, foundPreviousEblEnvelopes.size());
  }

  @Test
  void testFindPreviousEblEnvelopesNoneFound() {
    when(repository.findByTransportDocument_DocumentHash(any())).thenReturn(Optional.empty());
    List<EblEnvelope> foundPreviousEblEnvelopes =
        service.findPreviousEblEnvelopes("testDocumentHash");
    assertEquals(0, foundPreviousEblEnvelopes.size());
  }

  @Test
  void testCreateEblEnvelope() {
    String documentHash = transportDocument.getDocumentHash();
    List<EndorsementChainTransactionTO> transactions =
        List.of(TransactionDataFactory.endorsementChainTransaction());
    EblEnvelopeTO envelope =
        service.createEblEnvelope(
            documentHash, transactions, eblEnvelope.getPreviousEnvelopeHash());
    assertEquals(documentHash, envelope.documentHash());
    assertEquals(transactions.size(), envelope.transactions().size());
    assertEquals(eblEnvelope.getPreviousEnvelopeHash(), envelope.previousEnvelopeHash());
  }

  @Test
  void testCreateEblEnvelopeWithNullTransactions() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    List<EndorsementChainTransactionTO> transactions =
        List.of(TransactionDataFactory.endorsementChainTransaction());
    EblEnvelopeTO envelope =
        service.createEblEnvelope(documentHash, null, eblEnvelope.getPreviousEnvelopeHash());
    assertEquals(documentHash, envelope.documentHash());
    assertNull(envelope.transactions());
    assertEquals(eblEnvelope.getPreviousEnvelopeHash(), envelope.previousEnvelopeHash());
  }

  @Test
  void testExportEnvelope() {
    when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    when(signature.createSignedEblEnvelope(any())).thenReturn(SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO());

    SignedEndorsementChainEntryTO response = service.exportEblEnvelope(transportDocument, eblEnvelopeTO);

    assertEquals(SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().envelopeHash(), response.envelopeHash());
    assertEquals(SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().signature(), response.signature());
  }

  @Test
  void testExportEnvelopeSignFailed() {
    when(signature.createSignedEblEnvelope(any()))
        .thenThrow(
            ConcreteRequestErrorMessageException.internalServerError(
                "Unable to generate the JWS Object"));

    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.exportEblEnvelope(transportDocument, eblEnvelopeTO));

    assertEquals("Unable to generate the JWS Object", returnedException.getMessage());
  }

  @Test
  void testVerifyResponseSignatureInvalid() {
    when(signature.verifyEnvelopeHash(any(), any(), any())).thenReturn(false);

    String envelopeHash = SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().envelopeHash();
    String signature = SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().signature();

    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.verifyEblEnvelopeResponseSignature("localhost:8443", envelopeHash, signature));

    assertEquals("Signature not valid", returnedException.getMessage());
  }

  @Test
  void testVerifyResponseSignatureValid() {
    when(signature.verifyEnvelopeHash(any(), any(), any())).thenReturn(true);

    String response =
        service.verifyEblEnvelopeResponseSignature(
            "localhost:8443",
          SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().envelopeHash(),
          SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().signature());
    assertEquals(SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().signature(), response);
  }

  @Test
  void testParseEnvelopeInvalidEblEnvelope() {
    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.verifyEndorsementChainSignature("invalid EBL envelope"));

    assertEquals("Provided EBL envelope is not valid", returnedException.getMessage());
  }

  @Test
  void testParseEnvelopeValidEblEnvelope() {
    when(signature.verifySignature(any(), any())).thenReturn(true);
    EblEnvelopeTO eblEnvelopeTO = service.verifyEndorsementChainSignature(SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().signature());

    assertNotNull(eblEnvelopeTO);
    assertEquals(1, eblEnvelopeTO.transactions().size());
    assertEquals("fd868c82e99777b472a1677390d954dbb0131cb3b0f55c8ef51969856410d38e", eblEnvelopeTO.documentHash());
    assertNull(eblEnvelopeTO.previousEnvelopeHash());
  }

  @Test
  void testVerifyEnvelopeSignatureInvalid() {
    SignedEndorsementChainEntryTO invalidSignedEblEnvelope =
      SignedEndorsementChainEntryTO.builder()
        .envelopeHash(SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().envelopeHash())
        .signature(SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO().signature())
        .build();

    when(signature.verifySignature(any(), any())).thenReturn(false);
    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.verifyEndorsementChainSignature(SignedEblEnvelopeTODataFactory.signedEblEnvelopeTOWithInvalidSignature().signature()));

    assertEquals("Signature could not be validated", returnedException.getMessage());
  }

  @Test
  void testSaveEblEnvelope() {
    when(repository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
    when(signature.sign(any())).thenReturn("dummySignature");

    String signature = service.saveEblEnvelopes(eblEnvelopeList);
    assertNotNull(signature);
  }

  @Test
  void testSaveEblEnvelopeWithoutEnvelopeHash() {
    List<EblEnvelope> listOfInvalidEblEnvelope =
        List.of(
            EblEnvelope.builder()
                .transactions(EblEnvelopeDataFactory.getEblEnvelope().getTransactions())
                .transportDocument(EblEnvelopeDataFactory.getEblEnvelope().getTransportDocument())
                .build());

    when(repository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);

    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.saveEblEnvelopes(listOfInvalidEblEnvelope));
    assertEquals("Could not find a Envelope Hash on the EblEnvelope", returnedException.getMessage());
  }
}
