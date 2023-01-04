package org.dcsa.endorsementchain.unofficial.controller;

import org.dcsa.endorsementchain.unofficial.datafactories.TransactionDataFactory;
import org.dcsa.endorsementchain.unofficial.service.TransactionService;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.errors.infrastructure.ConcreteRequestErrorMessageExceptionHandler;
import org.dcsa.skernel.errors.infrastructure.FallbackExceptionHandler;
import org.dcsa.skernel.errors.infrastructure.JakartaValidationExceptionHandler;
import org.dcsa.skernel.errors.infrastructure.SpringExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TransactionController.class})
@Import({SpringExceptionHandler.class, JakartaValidationExceptionHandler.class, FallbackExceptionHandler.class, ConcreteRequestErrorMessageExceptionHandler.class})
class TransactionControllerTest {

  @MockBean TransactionService service;

  @Autowired MockMvc mockMvc;

  @Test
  void testSuccessfulLocalTransactionCreation() throws Exception {
    when(service.createLocalTransaction(any(), any()))
        .thenReturn(Optional.of(UUID.fromString("b1b2a261-f6b7-4afa-9cca-6de4f11191fd")));

    mockMvc
        .perform(
            post("/unofficial/transactions/local/2d2b87eb4182a68031b595fbaa3af9b649944c54a6d075add7e77045b7c8ee5b")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(TransactionDataFactory.endorsementChainTransactionJson()))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString()
        .equals("b1b2a261-f6b7-4afa-9cca-6de4f11191fd");
  }

  @Test
  void testLocalTransactionCreationOnExportedEBL() throws Exception {
    when(service.createLocalTransaction(any(), any())).thenReturn(Optional.empty());

    mockMvc
      .perform(
        post("/unofficial/transactions/local/2d2b87eb4182a68031b595fbaa3af9b649944c54a6d075add7e77045b7c8ee5b")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(TransactionDataFactory.endorsementChainTransactionJson()))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.httpMethod").value("POST"))
      .andExpect(jsonPath("$.requestUri").value("/unofficial/transactions/local/2d2b87eb4182a68031b595fbaa3af9b649944c54a6d075add7e77045b7c8ee5b"))
      .andExpect(jsonPath("$.errors[0].reason").value("invalidParameter"))
      .andExpect(
        jsonPath("$.errors[0].message")
          .value(containsString("Cannot create a transaction on an exported TransportDocument.")));
  }

  @Test
  void testLocalTransactionOnNonExistentTransportDocument() throws Exception {
    when(service.createLocalTransaction(any(), any())).thenThrow(ConcreteRequestErrorMessageException.notFound("TransportDocument not found"));

    mockMvc
      .perform(
        post("/unofficial/transactions/local/2d2b87eb4182a68031b595fbaa3af9b649944c54a6d075add7e77045b7c8ee5b")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(TransactionDataFactory.endorsementChainTransactionJson()))
      .andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.httpMethod").value("POST"))
      .andExpect(jsonPath("$.requestUri").value("/unofficial/transactions/local/2d2b87eb4182a68031b595fbaa3af9b649944c54a6d075add7e77045b7c8ee5b"))
      .andExpect(jsonPath("$.errors[0].reason").value("notFound"))
      .andExpect(
        jsonPath("$.errors[0].message")
          .value(containsString("TransportDocument not found")));
  }
}
