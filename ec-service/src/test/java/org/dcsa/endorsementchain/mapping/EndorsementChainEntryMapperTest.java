package org.dcsa.endorsementchain.mapping;

import org.dcsa.endorsementchain.datafactories.PartyDataFactory;
import org.dcsa.endorsementchain.persistence.entity.EndorsementChainEntry;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.persistence.entity.enums.TransactionAction;
import org.dcsa.endorsementchain.transferobjects.EndorsementChainEntryTO;
import org.dcsa.endorsementchain.transferobjects.enums.BlAction;
import org.dcsa.endorsementchain.unofficial.mapping.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EndorsementChainEntryMapperTest {

  private EndorsementChainEntryMapper mapper = Mappers.getMapper(EndorsementChainEntryMapper.class);
  private TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(mapper, "transactionMapper", transactionMapper);
  }

  @Test
  void testEndorsementChainEntryMapper() {
    EndorsementChainEntry envelope =
        EndorsementChainEntry.builder()
            .previousEnvelopeHash(null)
            .envelopeHash("78cbf8e9600067b40ef9ea7d5dbf6e06213c72e5cc40b91b764680bc1f7aa31d")
            .signature("this is a dummy signature")
            .transportDocument(
                TransportDocument.builder()
                    .documentHash(
                        "5f7e1e721202f7319f2b5b75f042c4d4a122225033b57972ac821b7a014c6dd2")
                    .isExported(false)
                    .transportDocumentJson("dummy transportdocument")
                    .transactions(
                        Set.of(
                            Transaction.builder()
                                .comments("this is a comment")
                                .isToOrder(true)
                                .action(TransactionAction.ISSU)
                                .platformHost("localhost:443")
                                .party(PartyDataFactory.party())
                                .build()))
                    .build())
            .build();

    EndorsementChainEntryTO endorsementChainEntryTO = mapper.endorsementChainEntryToTO(envelope);
    assertEquals(envelope.getTransportDocument().getDocumentHash(), endorsementChainEntryTO.documentHash());
    assertEquals(envelope.getTransportDocument().getTransactions().size(), endorsementChainEntryTO.transactions().size());
    assertEquals(BlAction.ISSU, endorsementChainEntryTO.transactions().get(0).action());
    assertNull(endorsementChainEntryTO.previousEnvelopeHash());
  }
}
