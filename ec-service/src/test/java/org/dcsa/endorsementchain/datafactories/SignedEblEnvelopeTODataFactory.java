package org.dcsa.endorsementchain.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;

import java.util.List;

@UtilityClass
public class SignedEblEnvelopeTODataFactory {

  public SignedEblEnvelopeTO signedEblEnvelopeTO() {
    return SignedEblEnvelopeTO.builder()
        .eblEnvelope(EblEnvelopeTODataFactory.eblEnvelopeTO())
        .signature(
            "nI85d4jseqXOR3hXkyLT2uajlXCsW88nAoEwec13I1tUyt6xwbgQz4jVQ2tdKbSlLcwrVmetlbz9Moy-DRno3X3cJmW9Y9TtyVMuygP0p_BQy1TVd766O1VRpiTp_tB1Eb93Fnj0RvpUZ_ddp9YKQjdxpAYi23Rbnz33A-x3uWp-Nk6y7sy3THlw5ZwbhuBWtp7yeaNGwVL9RUegyHwojGQp3YIVzFVXomUkj-JAg8UvOE6xdd7XSZaM_Sf7ZJlyTQQxyqZ1ui-zCwVHu0zdNEbEzAm_SfgxlPUaO8nEWSmAGo29aGO8HiU2_mbVMU1lCL00LyAEoAaDFnmbnEOoWw")
        .eblEnvelopeHash("5d52a570c5df23371da789956922870eea5efe50cb4d571517ab18cadabb611e")
        .build();
  }

  public List<SignedEblEnvelopeTO> signedEblEnvelopeTOList() {
    SignedEblEnvelopeTO initialSignedEblEnvelopeTO =
        SignedEblEnvelopeTO.builder()
            .eblEnvelope(EblEnvelopeTODataFactory.eblEnvelopeTO())
            .signature(
                "gb_mxYs4slLrOnFqmlFHnv0YC5-HajF2Bxs39OBe4okm0teciZJ0USUYc1AGFqAaL2oYogn6shsWY2kkPnpU85-hq2qldv9M-WoT5vh1xqAxcXYsEUSOQFV-2ATJAuMk5ceIQ8DlksVJ14CPTTlB59bHz6UWhwpbuzDVEROjQ79qS9AGlgH6rU06q8phGGn09G1rq2twZL6qxXHP9y4ZrTo_P088JCWBtvkGQ1ApHfcGO75wp-BmqAt26BZHuUioC7kvo9hPXJQGz8RY5KqNwul3lXKmjnVF2Ac-MU-IRf0AzFARk25UUaVnpHXN6hIeHR3hAjwNjGnY44VXTbDbwA")
            .eblEnvelopeHash("2071af20009cdd6348a727b33c005767afe83db2a9e67e2dde6367fcd045fd0a")
            .build();

    return List.of(initialSignedEblEnvelopeTO, SignedEblEnvelopeTODataFactory.signedEblEnvelopeTO());
  }
}
