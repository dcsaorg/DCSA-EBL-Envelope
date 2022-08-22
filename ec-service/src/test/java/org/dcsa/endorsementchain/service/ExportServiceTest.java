package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {
  @Mock EblEnvelopeService eblEnvelopeService;
  @Mock TransactionService transactionService;
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
    String rawEnvelope = mapper.writeValueAsString(exportingEblEnvelopeTO);
    List<String> rawEblEnvelopes = previousEblEnvelopes.stream().map(parsedEblEnvelope -> {
      try {
        return mapper.writeValueAsString(parsedEblEnvelope);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Can't serialize eblEnvelope");
      }
    }).toList();
    previousSignedEblEnvelopes = SignedEblEnvelopeTODataFactory.signedEblEnvelopeTOList(rawEblEnvelopes.get(0), rawEblEnvelopes.get(1));
    signedEblEnvelopeTO = SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO(rawEnvelope);
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
    when(eblEnvelopeService.createEblEnvelope(documentHash, endorsementChainTransactionTOs, previousEblEnvelopeHash)).thenReturn(exportingEblEnvelopeTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(eblEnvelopeService.exportEblEnvelope(transactionList.get(0).getTransportDocument(), exportingEblEnvelopeTO)).thenReturn(signedEblEnvelopeTO);
    when(eblEnvelopeService.verifyResponse("localhost:8443", signedEblEnvelopeTO.eblEnvelopeHash(), "dummyResponse")).thenReturn("dummyResponse");
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

    String responseSignature = exportService.exportEbl("test@localhost:8443", TransportDocumentDataFactory.transportDocumentHash());
    assertEquals("dummyResponse", responseSignature);

  }

  @Test
  void testExportHttpCallFailed() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(eblEnvelopeService.findPreviousEblEnvelopes(documentHash)).thenReturn(previousEblEnvelopes);
    when(eblEnvelopeService.findPreviousEblEnvelopeHash(previousEblEnvelopes)).thenReturn(previousEblEnvelopeHash);
    when(eblEnvelopeService.convertExistingEblEnvelopesToSignedEnvelopes(previousEblEnvelopes)).thenReturn(previousSignedEblEnvelopes);
    when(eblEnvelopeService.createEblEnvelope(documentHash, endorsementChainTransactionTOs, previousEblEnvelopeHash)).thenReturn(exportingEblEnvelopeTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(eblEnvelopeService.exportEblEnvelope(transactionList.get(0).getTransportDocument(), exportingEblEnvelopeTO)).thenReturn(signedEblEnvelopeTO);
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.BAD_REQUEST));

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () ->
          exportService.exportEbl("test", documentHash));

    assertEquals("Transfer failed.", returnedException.getMessage());

  }

  @Test
  void testExportCreatingEndorsementChainTransactionsFailed() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(eblEnvelopeService.findPreviousEblEnvelopes(documentHash)).thenReturn(previousEblEnvelopes);
    when(eblEnvelopeService.findPreviousEblEnvelopeHash(previousEblEnvelopes)).thenReturn(previousEblEnvelopeHash);
    when(eblEnvelopeService.convertExistingEblEnvelopesToSignedEnvelopes(previousEblEnvelopes)).thenReturn(previousSignedEblEnvelopes);
    when(eblEnvelopeService.createEblEnvelope(documentHash, endorsementChainTransactionTOs, previousEblEnvelopeHash)).thenReturn(exportingEblEnvelopeTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(eblEnvelopeService.exportEblEnvelope(transactionList.get(0).getTransportDocument(), exportingEblEnvelopeTO)).thenReturn(signedEblEnvelopeTO);
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () ->
          exportService.exportEbl("test", documentHash));

    assertEquals("No signature response received from recipient platform", returnedException.getMessage());

  }

}
