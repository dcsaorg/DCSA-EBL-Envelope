package org.dcsa.endorsementchain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dcsa.endorsementchain.datafactories.PartyDataFactory;
import org.dcsa.endorsementchain.service.PartyService;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.dcsa.skernel.errors.infrastructure.ConcreteRequestErrorMessageExceptionHandler;
import org.dcsa.skernel.errors.infrastructure.FallbackExceptionHandler;
import org.dcsa.skernel.errors.infrastructure.JavaxValidationExceptionHandler;
import org.dcsa.skernel.errors.infrastructure.SpringExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class})
@Import({SpringExceptionHandler.class, JavaxValidationExceptionHandler.class, FallbackExceptionHandler.class, ConcreteRequestErrorMessageExceptionHandler.class})
public class UserControllerTest {
  @MockBean PartyService partyService;

  @Autowired MockMvc mockMvc;

  @Test
  public void testGetUser() throws Exception {
    // Setup
    PartyTO expected = PartyDataFactory.partyTO();
    when(partyService.findPartyByUserId(any())).thenReturn(Optional.of(expected));

    // Execute
    String json = mockMvc.perform(MockMvcRequestBuilders.get("/user/test"))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    // Verify
    PartyTO actual = new ObjectMapper().readValue(json, PartyTO.class);
    assertEquals(expected, actual);
    verify(partyService).findPartyByUserId("test");
  }

  @Test
  public void testGetUser_NotFound() throws Exception {
    when(partyService.findPartyByUserId(any())).thenReturn(Optional.empty());

    mockMvc.perform(MockMvcRequestBuilders.get("/user/test"))
      .andExpect(status().isNotFound());

    verify(partyService).findPartyByUserId("test");
  }
}
