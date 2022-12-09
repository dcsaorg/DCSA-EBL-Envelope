package org.dcsa.endorsementchain.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.dcsa.endorsementchain.mapping.PartyMapper;
import org.dcsa.endorsementchain.persistence.repository.PartyRepository;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartyService {
  private final PartyRepository partyRepository;
  private final PartyMapper partyMapper;

  @Setter // for testing
  @Value("@${server.hostname}:${server.port}")
  private String platformId;

  @Transactional
  public Optional<PartyTO> findPartyByUserId(String userId) {
    return partyRepository.findById(userId + platformId).map(partyMapper::toDTO);
  }
}
