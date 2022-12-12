package org.dcsa.endorsementchain.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.dcsa.endorsementchain.mapping.PartyMapper;
import org.dcsa.endorsementchain.persistence.entity.Party;
import org.dcsa.endorsementchain.persistence.repository.PartyRepository;
import org.dcsa.endorsementchain.transferobjects.PartyTO;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
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
  public Optional<PartyTO> findLocalPartyByUserId(String userId) {
    return partyRepository.findById(userId + platformId).map(partyMapper::toDTO);
  }

  @Transactional
  public Party getPartyByTransferee(String transferee) {
    return transferee == null ? null : partyRepository.findById(idWithPlatformId(transferee))
      .orElseThrow(() -> ConcreteRequestErrorMessageException.notFound("Party for transferee '" + transferee + "' not found"));
  }

  @Transactional
  public void createParty(PartyTO partyTO) {
    partyRepository.save(partyMapper.toDAO(partyTO, idWithPlatformId(partyTO.id())));
  }

  private String idWithPlatformId(String id) {
    return id.indexOf('@') == -1 ? id + platformId : id;
  }
}
