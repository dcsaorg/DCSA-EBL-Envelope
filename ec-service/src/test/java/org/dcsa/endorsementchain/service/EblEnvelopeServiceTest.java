package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
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
  private SignedEblEnvelopeTO signedEblEnvelopeTO;
  private List<SignedEblEnvelopeTO> signedEblEnvelopeTOs;

  @BeforeEach
  void init() throws JsonProcessingException {
    eblEnvelope = EblEnvelopeDataFactory.getEblEnvelope();
    eblEnvelopeList = EblEnvelopeDataFactory.getEblEnvelopeList();
    transportDocument = TransportDocumentDataFactory.transportDocumentEntityWithTransactions();
    eblEnvelopeTO = EblEnvelopeTODataFactory.eblEnvelopeTO();
    List<EblEnvelopeTO> eblEnvelopeTOs = EblEnvelopeTODataFactory.eblEnvelopeTOList();
    List<String> rawEblEnvelopes =
        eblEnvelopeTOs.stream()
            .map(
                parsedEblEnvelope -> {
                  try {
                    return objectMapper.writeValueAsString(parsedEblEnvelope);
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException("Can't serialize eblEnvelope");
                  }
                })
            .toList();
    signedEblEnvelopeTO =
        SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO(rawEblEnvelopes.get(1));
    signedEblEnvelopeTOs =
        SignedEblEnvelopeTODataFactory.signedEblEnvelopeTOList(
            rawEblEnvelopes.get(0), rawEblEnvelopes.get(1));
  }

  @Test
  void testConvertExistingEblEnvelopesToSignedEnvelopes() {
    List<SignedEblEnvelopeTO> signedEnvelopes =
        service.convertExistingEblEnvelopesToSignedEnvelopes(eblEnvelopeList);
    assertEquals(2, signedEnvelopes.size());
    assertNotNull(signedEnvelopes.get(0).signature());
  }

  @Test
  void testConvertExistingEblEnvelopesToSignedEnvelopesNull() {
    List<SignedEblEnvelopeTO> signedEnvelopes =
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
    assertEquals(eblEnvelope.getPreviousEnvelopeHash(), envelope.previousEblEnvelopeHash());
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
    assertEquals(eblEnvelope.getPreviousEnvelopeHash(), envelope.previousEblEnvelopeHash());
  }

  @Test
  void testExportEnvelope() {
    when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    when(signature.signEnvelope(any())).thenReturn(signedEblEnvelopeTO);

    SignedEblEnvelopeTO response = service.exportEblEnvelope(transportDocument, eblEnvelopeTO);

    assertEquals(signedEblEnvelopeTO.envelopeHash(), response.envelopeHash());
    assertEquals(signedEblEnvelopeTO.signature(), response.signature());
  }

  @Test
  void testExportEnvelopeSignFailed() {
    when(signature.signEnvelope(any()))
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

    String envelopeHash = signedEblEnvelopeTO.envelopeHash();
    String signature = signedEblEnvelopeTO.signature();

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
            signedEblEnvelopeTO.envelopeHash(),
            signedEblEnvelopeTO.signature());
    assertEquals(signedEblEnvelopeTO.signature(), response);
  }

  @Test
  void testParseEnvelopeInvalidEblEnvelope() {
    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.parseEblEnvelope("invalid EBL envelope"));

    assertEquals("Provided EBL envelope is not valid", returnedException.getMessage());
  }

  @Test
  void testParseEnvelopeValidEblEnvelope() {
    EblEnvelopeTO eblEnvelopeTO = service.parseEblEnvelope(signedEblEnvelopeTO.eblEnvelope());

    assertNotNull(eblEnvelopeTO);
    assertEquals(2, eblEnvelopeTO.transactions().size());
    assertEquals("21a9c5f49bfc7248fecbeba49e0b3414376e0c77a84ce1dae383dccd66f03000", eblEnvelopeTO.documentHash());
    assertEquals("a25286672be331c6770fa590f8eb7ab7cf105fd76f0db4b7cabd258a5953482e", eblEnvelopeTO.previousEblEnvelopeHash());
  }

  @Test
  void testVerifyEnvelopeSignatureInvalid() {
    SignedEblEnvelopeTO invalidSignedEblEnvelope =
      SignedEblEnvelopeTO.builder()
        .envelopeHash(signedEblEnvelopeTO.envelopeHash())
        .signature(signedEblEnvelopeTO.signature())
        .eblEnvelope("InvalidEblEnvelope")
        .build();

    when(signature.verifyEnvelope(any(), any(), any())).thenReturn(false);
    Exception returnedException =
        assertThrows(
            ConcreteRequestErrorMessageException.class,
            () -> service.verifyEblEnvelopeSignature(eblEnvelopeTO, invalidSignedEblEnvelope));

    assertEquals("Signature could not be validated", returnedException.getMessage());
  }

  @Test
  void testVerifyEnvelopeSignatureValid() {
    when(signature.verifyEnvelope(any(), any(), any())).thenReturn(true);

    service.verifyEblEnvelopeSignature(eblEnvelopeTO, signedEblEnvelopeTOs.get(1));
  }

  @Test
  void testSaveEblEnvelope() {
    when(repository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
    when(signature.signEnvelopeHash(any())).thenReturn("dummySignature");

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
