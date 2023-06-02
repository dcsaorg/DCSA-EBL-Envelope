package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.datafactories.EndorsementChainEntryDataFactory;
import org.dcsa.endorsementchain.datafactories.EndorsementChainEntryTODataFactory;
import org.dcsa.endorsementchain.datafactories.EndorsementChainTransactionTODataFactory;
import org.dcsa.endorsementchain.datafactories.SignedEndorsementChainEntryTODataFactory;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainTransactionTO;
import org.dcsa.endorsementchain.transferobjects.SignedEndorsementChainEntryTO;
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
  @Mock
  EndorsementChainEntryService endorsementChainEntryService;
  @Mock TransactionService transactionService;
  @Mock RestTemplate restTemplate;

  @InjectMocks ExportService exportService;

  private ObjectMapper mapper = new ObjectMapper();
  private List<Transaction> transactionList;
  private List<EndorsementChainTransactionTO> endorsementChainTransactionTOs;
  private List<EndorsementChainEntry> previousEndorsementChainEntries;
  private String previousEndorsementChainEntryHash;
  private EndorsementChainEntryTO exportingEndorsementChainEntryTO;
  private List<SignedEndorsementChainEntryTO> previousSignedEndorsementChainEntries;
  private SignedEndorsementChainEntryTO signedEndorsementChainEntryTO;

  @BeforeEach
  void init() {
    transactionList = TransactionDataFactory.transactionEntityList();
    previousEndorsementChainEntries = EndorsementChainEntryDataFactory.getEndorsementChainEntryList();
    previousEndorsementChainEntryHash = previousEndorsementChainEntries.get(0).getEnvelopeHash();
    exportingEndorsementChainEntryTO = EndorsementChainEntryTODataFactory.endorsementChainEntryTO();
    previousSignedEndorsementChainEntries = SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTOList();
    signedEndorsementChainEntryTO = SignedEndorsementChainEntryTODataFactory.signedEndorsementChainEntryTO();
    endorsementChainTransactionTOs = EndorsementChainTransactionTODataFactory.endorsementChainTransactionTOList();
  }

  @Test
  void testExportSuccessful() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(endorsementChainEntryService.findPreviousEndorsementChainEntries(documentHash)).thenReturn(previousEndorsementChainEntries);
    when(endorsementChainEntryService.findPreviousEndorsementChainEntryHash(previousEndorsementChainEntries)).thenReturn(previousEndorsementChainEntryHash);
    when(endorsementChainEntryService.convertExistingEndorsementChainEntriesToSignedEntries(previousEndorsementChainEntries)).thenReturn(previousSignedEndorsementChainEntries);
    when(endorsementChainEntryService.createEndorsementChainEntry(documentHash, endorsementChainTransactionTOs, previousEndorsementChainEntryHash)).thenReturn(exportingEndorsementChainEntryTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(endorsementChainEntryService.exportEndorsementChainEntry(transactionList.get(0).getTransportDocument(), exportingEndorsementChainEntryTO)).thenReturn(signedEndorsementChainEntryTO);
    when(endorsementChainEntryService.verifyEndorsementChainEntryResponseSignature("localhost:8443", signedEndorsementChainEntryTO.envelopeHash(), "dummyResponse")).thenReturn("dummyResponse");
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>("dummyResponse", HttpStatus.OK));

    String responseSignature = exportService.exportEbl("test@localhost:8443", TransportDocumentDataFactory.transportDocumentHash());
    assertEquals("dummyResponse", responseSignature);

  }

  @Test
  void testExportHttpCallFailed() {
    String documentHash = TransportDocumentDataFactory.transportDocumentHash();
    when(transactionService.getTransactionsForExport(documentHash)).thenReturn(transactionList);
    when(endorsementChainEntryService.findPreviousEndorsementChainEntries(documentHash)).thenReturn(previousEndorsementChainEntries);
    when(endorsementChainEntryService.findPreviousEndorsementChainEntryHash(previousEndorsementChainEntries)).thenReturn(previousEndorsementChainEntryHash);
    when(endorsementChainEntryService.convertExistingEndorsementChainEntriesToSignedEntries(previousEndorsementChainEntries)).thenReturn(previousSignedEndorsementChainEntries);
    when(endorsementChainEntryService.createEndorsementChainEntry(documentHash, endorsementChainTransactionTOs, previousEndorsementChainEntryHash)).thenReturn(exportingEndorsementChainEntryTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(endorsementChainEntryService.exportEndorsementChainEntry(transactionList.get(0).getTransportDocument(), exportingEndorsementChainEntryTO)).thenReturn(signedEndorsementChainEntryTO);
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>("dummyResponse", HttpStatus.BAD_REQUEST));

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
    when(endorsementChainEntryService.findPreviousEndorsementChainEntries(documentHash)).thenReturn(previousEndorsementChainEntries);
    when(endorsementChainEntryService.findPreviousEndorsementChainEntryHash(previousEndorsementChainEntries)).thenReturn(previousEndorsementChainEntryHash);
    when(endorsementChainEntryService.convertExistingEndorsementChainEntriesToSignedEntries(previousEndorsementChainEntries)).thenReturn(previousSignedEndorsementChainEntries);
    when(endorsementChainEntryService.createEndorsementChainEntry(documentHash, endorsementChainTransactionTOs, previousEndorsementChainEntryHash)).thenReturn(exportingEndorsementChainEntryTO);
    when(transactionService.localToEndorsementChainTransactions(transactionList)).thenReturn(endorsementChainTransactionTOs);
    when(endorsementChainEntryService.exportEndorsementChainEntry(transactionList.get(0).getTransportDocument(), exportingEndorsementChainEntryTO)).thenReturn(signedEndorsementChainEntryTO);
    when(restTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

    Exception returnedException =
      assertThrows(
        ConcreteRequestErrorMessageException.class,
        () ->
          exportService.exportEbl("test", documentHash));

    assertEquals("No signature response received from recipient platform", returnedException.getMessage());

  }

}
