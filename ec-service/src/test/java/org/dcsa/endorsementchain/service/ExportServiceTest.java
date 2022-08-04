package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.components.eblenvelope.EblEnvelopeSignature;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeDataFactory;
import org.dcsa.endorsementchain.datafactories.EblEnvelopeTODataFactory;
import org.dcsa.endorsementchain.datafactories.EndorsementChainTransactionTODataFactory;
import org.dcsa.endorsementchain.datafactories.SignedEblEnvelopeTODataFactory;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.unofficial.datafactories.TransactionDataFactory;
import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
import org.dcsa.endorsementchain.unofficial.service.TransactionService;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {
  @Mock EblEnvelopeService eblEnvelopeService;
  @Mock TransactionService transactionService;
  @Mock EblEnvelopeSignature signature;
  @Mock RestTemplate restTemplate;

  @InjectMocks ExportService exportService;

  private ObjectMapper mapper = new ObjectMapper();
  private List<Transaction> transactionList;
  private List<EndorsementChainTransactionTO> endorsementChainTransactionTOs;
  private List<EblEnvelope> previousEblEnvelopes;
  private String previousEblEnvelopeHash;
  private EblEnvelopeTO exportingEblEnvelopeTO;
  private List<SignedEblEnvelopeTO> previousSignedEblEnvelopes;
  private SignedEblEnvelopeTO signedEblEnvelopeTO;
  private JsonNode jsonResponse;

  @BeforeEach
  void init() throws JsonProcessingException {
    transactionList = TransactionDataFactory.transactionEntityList();
    previousEblEnvelopes = EblEnvelopeDataFactory.getEblEnvelopeList();
    previousEblEnvelopeHash = previousEblEnvelopes.get(0).getEnvelopeHash();
    exportingEblEnvelopeTO = EblEnvelopeTODataFactory.eblEnvelopeTO();
    previousSignedEblEnvelopes = SignedEblEnvelopeTODataFactory.signedEblEnvelopeTOList();
    signedEblEnvelopeTO = SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO();
    endorsementChainTransactionTOs = EndorsementChainTransactionTODataFactory.endorsementChainTransactionTOList();
    jsonResponse = mapper.readTree("\"dummyResponse\"");
  }

  @Test
  void testExportSuccessful() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(eblEnvelopeService.findPreviousEblEnvelopes(documentHash)).thenReturn(previousEblEnvelopes);
    when(eblEnvelopeService.findPreviousEblEnvelopeHash(previousEblEnvelopes)).thenReturn(previousEblEnvelopeHash);
    when(eblEnvelopeService.convertExistingEblEnvelopesToSignedEnvelopes(previousEblEnvelopes)).thenReturn(previousSignedEblEnvelopes);
    when(eblEnvelopeService.createEblEnvelope(eq(documentHash), eq(endorsementChainTransactionTOs), eq(previousEblEnvelopeHash))).thenReturn(exportingEblEnvelopeTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(signature.sign(exportingEblEnvelopeTO)).thenReturn(signedEblEnvelopeTO);
    when(signature.verify(eq("dummyResponse"), eq(signedEblEnvelopeTO.eblEnvelopeHash()))).thenReturn(true);
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

    String responseSignature = exportService.exportEbl("test", TransportDocumentDataFactory.transportDocumentHash());
    assertEquals("dummyResponse", responseSignature);

  }

  @Test
  void testExportVerificationFailed() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(eblEnvelopeService.findPreviousEblEnvelopes(documentHash)).thenReturn(previousEblEnvelopes);
    when(eblEnvelopeService.findPreviousEblEnvelopeHash(previousEblEnvelopes)).thenReturn(previousEblEnvelopeHash);
    when(eblEnvelopeService.convertExistingEblEnvelopesToSignedEnvelopes(previousEblEnvelopes)).thenReturn(previousSignedEblEnvelopes);
    when(eblEnvelopeService.createEblEnvelope(eq(documentHash), eq(endorsementChainTransactionTOs), eq(previousEblEnvelopeHash))).thenReturn(exportingEblEnvelopeTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(signature.sign(exportingEblEnvelopeTO)).thenReturn(signedEblEnvelopeTO);
    when(signature.verify(eq("dummyResponse"), eq(signedEblEnvelopeTO.eblEnvelopeHash()))).thenReturn(false);
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () ->
          exportService.exportEbl("test", TransportDocumentDataFactory.transportDocumentHash()));

    assertEquals("Signature not valid", returnedException.getMessage());

  }

  @Test
  void testExportHttpCallFailed() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(eblEnvelopeService.findPreviousEblEnvelopes(documentHash)).thenReturn(previousEblEnvelopes);
    when(eblEnvelopeService.findPreviousEblEnvelopeHash(previousEblEnvelopes)).thenReturn(previousEblEnvelopeHash);
    when(eblEnvelopeService.convertExistingEblEnvelopesToSignedEnvelopes(previousEblEnvelopes)).thenReturn(previousSignedEblEnvelopes);
    when(eblEnvelopeService.createEblEnvelope(eq(documentHash), eq(endorsementChainTransactionTOs), eq(previousEblEnvelopeHash))).thenReturn(exportingEblEnvelopeTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(signature.sign(exportingEblEnvelopeTO)).thenReturn(signedEblEnvelopeTO);
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.BAD_REQUEST));

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () ->
          exportService.exportEbl("test", TransportDocumentDataFactory.transportDocumentHash()));

    assertEquals("Transfer failed.", returnedException.getMessage());

  }

  @Test
  void testExportSigningFailed() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(eblEnvelopeService.findPreviousEblEnvelopes(documentHash)).thenReturn(previousEblEnvelopes);
    when(eblEnvelopeService.findPreviousEblEnvelopeHash(previousEblEnvelopes)).thenReturn(previousEblEnvelopeHash);
    when(eblEnvelopeService.convertExistingEblEnvelopesToSignedEnvelopes(previousEblEnvelopes)).thenReturn(previousSignedEblEnvelopes);
    when(eblEnvelopeService.createEblEnvelope(eq(documentHash), eq(endorsementChainTransactionTOs), eq(previousEblEnvelopeHash))).thenReturn(exportingEblEnvelopeTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(signature.sign(exportingEblEnvelopeTO)).thenThrow(ConcreteRequestErrorMessageException.internalServerError("Unable to generate the JWS Object"));

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () ->
          exportService.exportEbl("test", TransportDocumentDataFactory.transportDocumentHash()));

    assertEquals("Unable to generate the JWS Object", returnedException.getMessage());

  }

  @Test
  void testExportCreatingEndorsementChainTransactionsFailed() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(eblEnvelopeService.findPreviousEblEnvelopes(documentHash)).thenReturn(previousEblEnvelopes);
    when(eblEnvelopeService.findPreviousEblEnvelopeHash(previousEblEnvelopes)).thenReturn(previousEblEnvelopeHash);
    when(eblEnvelopeService.convertExistingEblEnvelopesToSignedEnvelopes(previousEblEnvelopes)).thenReturn(previousSignedEblEnvelopes);
    when(eblEnvelopeService.createEblEnvelope(eq(documentHash), eq(endorsementChainTransactionTOs), eq(previousEblEnvelopeHash))).thenReturn(exportingEblEnvelopeTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(signature.sign(exportingEblEnvelopeTO)).thenReturn(signedEblEnvelopeTO);
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () ->
          exportService.exportEbl("test", TransportDocumentDataFactory.transportDocumentHash()));

    assertEquals("No signature response received from recipient platform", returnedException.getMessage());

  }

}
