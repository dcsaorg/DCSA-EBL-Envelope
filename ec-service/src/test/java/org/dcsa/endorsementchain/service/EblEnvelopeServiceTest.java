package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeSignature;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeDataFactory;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeTODataFactory;
import org.dcsa.endorsementchain.datafactories.SignedEblEnvelopeTODataFactory;
import org.dcsa.endorsementchain.mapping.EblEnvelopeMapper;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.repository.EblEnvelopeRepository;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.unofficial.datafactories.TransactionDataFactory;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
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

  @Spy EblEnvelopeMapper mapper = Mappers.getMapper(EblEnvelopeMapper.class);

  @InjectMocks EblEnvelopeService service;

  private List<EblEnvelope> eblEnvelopeList;
  private EblEnvelope eblEnvelope;
  private TransportDocument transportDocument;
  private EblEnvelopeTO eblEnvelopeTO;
  private SignedEblEnvelopeTO signedEblEnvelopeTO;
  private String rawEnvelope;

  @BeforeEach
  void init() throws JsonProcessingException {
    eblEnvelope = EblEnvelopeDataFactory.getEblEnvelope();
    eblEnvelopeList = EblEnvelopeDataFactory.getEblEnvelopeList();
    transportDocument = TransportDocumentDataFactory.transportDocumentEntityWithTransactions();
    eblEnvelopeTO = EblEnvelopeTODataFactory.eblEnvelopeTO();
    rawEnvelope = objectMapper.writeValueAsString(eblEnvelopeTO);
    signedEblEnvelopeTO = SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO(rawEnvelope);
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
    when(signature.signEblEnvelope(any())).thenReturn(signedEblEnvelopeTO);

    SignedEblEnvelopeTO response = service.exportEblEnvelope(transportDocument, eblEnvelopeTO);

    assertEquals(signedEblEnvelopeTO.eblEnvelopeHash(), response.eblEnvelopeHash());
    assertEquals(signedEblEnvelopeTO.signature(), response.signature());
  }

  @Test
  void testExportEnvelopeSignFailed() {
    when(signature.signEblEnvelope(any()))
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
    when(signature.verifyEblEnvelopeHash(any(), any(), any())).thenReturn(false);

    String envelopeHash = signedEblEnvelopeTO.eblEnvelopeHash();
    String signature = signedEblEnvelopeTO.signature();

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () -> service.verifyResponse("localhost:8443", envelopeHash, signature));

    assertEquals("Signature not valid", returnedException.getMessage());
  }

  @Test
  void testVerifyResponseSignatureValid() {
    when(signature.verifyEblEnvelopeHash(any(), any(),any())).thenReturn(true);

    String response = service.verifyResponse("localhost:8443", signedEblEnvelopeTO.eblEnvelopeHash(), signedEblEnvelopeTO.signature());
    assertEquals(signedEblEnvelopeTO.signature(), response);
  }

  @Test
  void testVerifyEnvelopeSignatureInvalidEblEnvelope() {
    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () -> service.verifyEnvelopeSignature("signature", "dummyPayload"));

    assertEquals("Provided EBL envelope is not valid", returnedException.getMessage());
  }

  @Test
  void testVerifyEnvelopeSignatureInvalid() {
    when(signature.verifyDetachedPayload(any(), any(), any())).thenReturn(false);
    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () -> service.verifyEnvelopeSignature("InvalidSignature", rawEnvelope));

    assertEquals("Signature could not be validated", returnedException.getMessage());
  }

  @Test
  void testVerifyEnvelopeSignatureValid() {
    when(signature.verifyDetachedPayload(any(), any(), any())).thenReturn(true);

    EblEnvelopeTO parsedEnvelope = service.verifyEnvelopeSignature("validSignature", rawEnvelope);
    assertEquals(eblEnvelopeTO.documentHash(), parsedEnvelope.documentHash());
    assertEquals(eblEnvelopeTO.previousEblEnvelopeHash(), parsedEnvelope.previousEblEnvelopeHash());
    assertEquals(eblEnvelopeTO.transactions().size(), parsedEnvelope.transactions().size());
  }
}
