package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransactionByTimestampComparator;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.unofficial.datafactories.TransactionDataFactory;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@UtilityClass
public class EblEnvelopeDataFactory {

  private static SortedSet<Transaction> setOf(Transaction t) {
    var s = new TreeSet<>(TransactionByTimestampComparator.INSTANCE);
    s.add(t);
    return s;
  }

  public EblEnvelope getEblEnvelope() {
    return EblEnvelope.builder()
        .transportDocument(
            TransportDocument.builder()
                .transportDocumentJson("{\"test\":\"testValue\"}")
                .isExported(false)
                .build())
        .envelopeHash("a25286672be331c6770fa590f8eb7ab7cf105fd76f0db4b7cabd258a5953482e")
        .previousEnvelopeHash(null)
        .signature(
            "nI85d4jseqXOR3hXkyLT2uajlXCsW88nAoEwec13I1tUyt6xwbgQz4jVQ2tdKbSlLcwrVmetlbz9Moy-DRno3X3cJmW9Y9TtyVMuygP0p_BQy1TVd766O1VRpiTp_tB1Eb93Fnj0RvpUZ_ddp9YKQjdxpAYi23Rbnz33A-x3uWp-Nk6y7sy3THlw5ZwbhuBWtp7yeaNGwVL9RUegyHwojGQp3YIVzFVXomUkj-JAg8UvOE6xdd7XSZaM_Sf7ZJlyTQQxyqZ1ui-zCwVHu0zdNEbEzAm_SfgxlPUaO8nEWSmAGo29aGO8HiU2_mbVMU1lCL00LyAEoAaDFnmbnEOoWw")
        .transactions(setOf(TransactionDataFactory.transactionEntity()))
        .build();
  }

  public List<EblEnvelope> getEblEnvelopeList() {
    EblEnvelope initialEblEnvelope =
        EblEnvelope.builder()
            .transportDocument(
                TransportDocument.builder()
                    .transportDocumentJson("{\"test\":\"testValue\"}")
                    .isExported(false)
                    .build())
            .envelopeHash("a25286672be331c6770fa590f8eb7ab7cf105fd76f0db4b7cabd258a5953482e")
            .previousEnvelopeHash(null)
            .signature(
                "nI85d4jseqXOR3hXkyLT2uajlXCsW88nAoEwec13I1tUyt6xwbgQz4jVQ2tdKbSlLcwrVmetlbz9Moy-DRno3X3cJmW9Y9TtyVMuygP0p_BQy1TVd766O1VRpiTp_tB1Eb93Fnj0RvpUZ_ddp9YKQjdxpAYi23Rbnz33A-x3uWp-Nk6y7sy3THlw5ZwbhuBWtp7yeaNGwVL9RUegyHwojGQp3YIVzFVXomUkj-JAg8UvOE6xdd7XSZaM_Sf7ZJlyTQQxyqZ1ui-zCwVHu0zdNEbEzAm_SfgxlPUaO8nEWSmAGo29aGO8HiU2_mbVMU1lCL00LyAEoAaDFnmbnEOoWw")
            .transactions(setOf(TransactionDataFactory.transactionEntity()))
            .build();

    EblEnvelope subsequentEblEnvelope =
        EblEnvelope.builder()
            .transportDocument(
                TransportDocument.builder()
                    .transportDocumentJson("{\"test\":\"testValue\"}")
                    .isExported(false)
                    .build())
            .envelopeHash("e1e4a40aaa508bc275898fb28474a4c808739f00aeb4334360008698d411bed9")
            .previousEnvelopeHash(
                "a25286672be331c6770fa590f8eb7ab7cf105fd76f0db4b7cabd258a5953482e")
            .signature(
                "nI85d4jseqXOR3hXkyLT2uajlXCsW88nAoEwec13I1tUyt6xwbgQz4jVQ2tdKbSlLcwrVmetlbz9Moy-DRno3X3cJmW9Y9TtyVMuygP0p_BQy1TVd766O1VRpiTp_tB1Eb93Fnj0RvpUZ_ddp9YKQjdxpAYi23Rbnz33A-x3uWp-Nk6y7sy3THlw5ZwbhuBWtp7yeaNGwVL9RUegyHwojGQp3YIVzFVXomUkj-JAg8UvOE6xdd7XSZaM_Sf7ZJlyTQQxyqZ1ui-zCwVHu0zdNEbEzAm_SfgxlPUaO8nEWSmAGo29aGO8HiU2_mbVMU1lCL00LyAEoAaDFnmbnEOoWw")
            .transactions(setOf(TransactionDataFactory.transactionEntity()))
            .build();

    return List.of(initialEblEnvelope, subsequentEblEnvelope);
  }
}
