package org.dcsa.endorsementchain.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.TransferblockTO;
import org.dcsa.endorsementchain.unofficial.service.TransactionService;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExportService {

  private final EblEnvelopeService eblEnvelopeService;
  private final TransactionService transactionService;
  private final RestTemplate restTemplate;

  public String exportEbl(String transferee, String documentHash) {

    List<Transaction> exportedTransactions =
        transactionService.getTransactionsForExport(documentHash);

    TransportDocument transportDocument = exportedTransactions.get(0).getTransportDocument();

    List<EblEnvelope> previousEblEnvelopes =
        eblEnvelopeService.findPreviousEblEnvelopes(documentHash);

    checkOutGoingDocumentHash(transportDocument.getTransportDocumentJson(), documentHash);

    String previousEblEnvelopeHash =
        eblEnvelopeService.findPreviousEblEnvelopeHash(previousEblEnvelopes);
    List<SignedEblEnvelopeTO> previousSignedEblEnvelopes =
        eblEnvelopeService.convertExistingEblEnvelopesToSignedEnvelopes(previousEblEnvelopes);

    EblEnvelopeTO exportingEblEnvelopeTO =
        eblEnvelopeService.createEblEnvelope(
            documentHash,
            transactionService.localToEndorsementChainTransactions(exportedTransactions),
            previousEblEnvelopeHash);

    SignedEblEnvelopeTO signedEblEnvelopeTO =
        eblEnvelopeService.exportEblEnvelope(transportDocument, exportingEblEnvelopeTO);

    List<SignedEblEnvelopeTO> toBeExportedEblEnvelopes =
        Stream.concat(previousSignedEblEnvelopes.stream(), Stream.of(signedEblEnvelopeTO)).toList();

    TransferblockTO transferblock =
        TransferblockTO.builder()
            .endorsementChain(toBeExportedEblEnvelopes)
            .document(transportDocument.getTransportDocumentJson())
            .build();

    URI platformURL = transfereeToPlatformHost(transferee);

    String signatureResponse = sendTransferBlock(platformURL, transferblock);

    return eblEnvelopeService.verifyEblEnvelopeResponseSignature(
        platformURL.getHost() + ":" + platformURL.getPort(),
        signedEblEnvelopeTO.envelopeHash(),
        signatureResponse);
  }

  private String sendTransferBlock(URI platformUrl, TransferblockTO transferblock) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    ResponseEntity<String> transferblockResponse =
        restTemplate.exchange(
            platformUrl, HttpMethod.PUT, new HttpEntity<>(transferblock, headers), String.class);

    if (transferblockResponse.getStatusCode().isError()) {
      throw ConcreteRequestErrorMessageException.internalServerError("Transfer failed.");
    }

    return Optional.ofNullable(transferblockResponse.getBody())
        .orElseThrow(
            () ->
                ConcreteRequestErrorMessageException.internalServerError(
                    "No signature response received from recipient platform"));
  }

  private URI transfereeToPlatformHost(String transferee) {
    String host = transferee.substring(transferee.indexOf("@") + 1);
    return UriComponentsBuilder.fromHttpUrl("https://" + host)
        .scheme("https")
        .path("/v1/transferblocks")
        .build()
        .toUri();
  }

  //The reference implementation already ensures the transportdocument is formatted according to RFC 8785 in TransportDocumentController#addTransportDocument
  //If the transport document was received from another EBL provider and the hash is provided it cannot be changed when it differs. So an exception is raised.
  @SneakyThrows
  private void checkOutGoingDocumentHash(String transportDocumentJson, String documentHash) {
    JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(transportDocumentJson);
    String canonizedTransportDocument = jsonCanonicalizer.getEncodedString();
    if(!documentHash.equals(DigestUtils.sha256Hex(canonizedTransportDocument))) {
      throw ConcreteRequestErrorMessageException.internalServerError("The documentHash does not match the hash calculated on the canonized transport document");
    }
  }
}
