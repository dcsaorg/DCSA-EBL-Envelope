package org.dcsa.endorsementchain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.datafactories.TransferblockTODataFactory;
import org.dcsa.endorsementchain.service.ImportService;
import org.dcsa.endorsementchain.transferobjects.TransferblockTO;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ImportController.class})
@Import({SpringExceptionHandler.class, JakartaValidationExceptionHandler.class, FallbackExceptionHandler.class, ConcreteRequestErrorMessageExceptionHandler.class})
class ImportControllerTest {

  @MockBean ImportService service;

  @Autowired ObjectMapper mapper;
  @Autowired MockMvc mockMvc;

  @Test
  void testImportController() throws Exception {
    TransferblockTO transferblock = TransferblockTODataFactory.transferblockTO();
    String rawTransferBlock = mapper.writeValueAsString(transferblock);

    when(service.importEbl(isNotNull())).thenReturn(Optional.of("Dummy signature"));

    String response = mockMvc.perform(put("/transferblocks").contentType(MediaType.APPLICATION_JSON).content(rawTransferBlock))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertEquals("Dummy signature", response);
  }

  @Test
  void testImportControllerError() throws Exception {
    TransferblockTO transferblock = TransferblockTODataFactory.transferblockTO();
    String rawTransferBlock = mapper.writeValueAsString(transferblock);

    when(service.importEbl(isNotNull())).thenReturn(Optional.empty());

    mockMvc.perform(put("/transferblocks").contentType(MediaType.APPLICATION_JSON).content(rawTransferBlock))
      .andDo(print())
      .andExpect(status().isBadRequest());

  }

  @Test
  void testImportControllerInvalidInput() throws Exception {
    mockMvc.perform(put("/transferblocks").contentType(MediaType.APPLICATION_JSON).content("invalid request"))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.httpMethod").value("PUT"))
      .andExpect(jsonPath("$.requestUri").value("/transferblocks"))
      .andExpect(jsonPath("$.errors[0].reason").value("invalidInput"))
      .andExpect(
        jsonPath("$.errors[0].message")
          .value(containsString("Unrecognized token 'invalid'")));
  }
}
