package org.dcsa.endorsementchain.service;

import org.dcsa.endorsementchain.datafactories.PartyDataFactory;
import org.dcsa.endorsementchain.mapping.PartyMapper;
import org.dcsa.endorsementchain.persistence.repository.PartyRepository;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartyServiceTest {
  @Spy private PartyMapper partyMapper = Mappers.getMapper(PartyMapper.class);
  @Mock private PartyRepository partyRepository;

  @InjectMocks private PartyService partyService;

  @BeforeEach
  public void init() {
    partyService.setPlatformId("@dcsa-testing.org.invalid:8443");
  }

  @Test
  public void testFindPartyByUserId() {
    // Setup
    when(partyRepository.findById(any())).thenReturn(Optional.of(PartyDataFactory.party()));

    // Execute
    Optional<PartyTO> actual = partyService.findLocalPartyByUserId("test");

    // Verify
    assertTrue(actual.isPresent());
    assertEquals(PartyDataFactory.partyTO(), actual.get());
    verify(partyRepository).findById("test@dcsa-testing.org.invalid:8443");
  }

  @Test
  public void testFindPartyByUserId_NotFound() {
    // Setup
    when(partyRepository.findById(any())).thenReturn(Optional.empty());

    // Execute
    Optional<PartyTO> actual = partyService.findLocalPartyByUserId("test");

    // Verify
    assertFalse(actual.isPresent());
    verify(partyRepository).findById("test@dcsa-testing.org.invalid:8443");
  }
}
