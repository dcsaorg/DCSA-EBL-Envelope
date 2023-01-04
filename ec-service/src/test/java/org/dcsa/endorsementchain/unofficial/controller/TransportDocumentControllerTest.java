package org.dcsa.endorsementchain.unofficial.controller;

import org.dcsa.endorsementchain.unofficial.datafactories.TransportDocumentDataFactory;
import org.dcsa.endorsementchain.unofficial.service.TransportDocumentService;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TransportDocumentController.class})
@Import({SpringExceptionHandler.class, JakartaValidationExceptionHandler.class, FallbackExceptionHandler.class, ConcreteRequestErrorMessageExceptionHandler.class})
class TransportDocumentControllerTest {

  @MockBean TransportDocumentService service;

  @Autowired MockMvc mockMvc;

  @Test
  void testSuccessfulCreateTransportDocument() throws Exception {
    when(service.saveTransportDocument(any()))
        .thenReturn(Optional.of(TransportDocumentDataFactory.transportDocumentHash()));

    mockMvc
        .perform(
            post("/unofficial/transport-documents")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                    TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions()
                        .getTransportDocumentJson()))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString()
        .equals("535acfd2e11364f9d35155e0160bbff6ec782fe03f6305162bf18f3de73930e3");
  }

  @Test
  void testErrorCreateTransportDocument() throws Exception {
    when(service.saveTransportDocument(any())).thenReturn(Optional.empty());

    mockMvc
        .perform(
            post("/unofficial/transport-documents")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                    TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions()
                        .getTransportDocumentJson()))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.httpMethod").value("POST"))
        .andExpect(jsonPath("$.requestUri").value("/unofficial/transport-documents"))
        .andExpect(jsonPath("$.errors[0].reason").value("internalError"))
        .andExpect(
            jsonPath("$.errors[0].message")
                .value(containsString("Saving of the transport document failed")));
  }

  @Test
  void testGetTransportDocument() throws Exception {
    when(service.getTransportDocument(any()))
        .thenReturn(
            Optional.of(
                TransportDocumentDataFactory.transportDocumentEntityWithoutTransactions()
                    .getTransportDocumentJson()));

    mockMvc
        .perform(
            get("/unofficial/transport-documents/535acfd2e11364f9d35155e0160bbff6ec782fe03f6305162bf18f3de73930e3")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  void testGetTransportDocumentNotFound() throws Exception {
    when(service.getTransportDocument(any())).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/unofficial/transport-documents/535acfd2e11364f9d35155e0160bbff6ec782fe03f6305162bf18f3de73930e3")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isNotFound());
  }
}
