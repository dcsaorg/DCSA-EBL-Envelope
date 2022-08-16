package org.dcsa.endorsementchain.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.dcsa.endorsementchain.persistence.entity.EblEnvelope;
import org.dcsa.endorsementchain.persistence.entity.Transaction;
import org.dcsa.endorsementchain.persistence.entity.TransportDocument;
import org.dcsa.endorsementchain.transferobjects.EblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.SignedEblEnvelopeTO;
import org.dcsa.endorsementchain.transferobjects.TransferblockTO;
import org.dcsa.endorsementchain.unofficial.service.TransactionService;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
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
            .endorcementChain(toBeExportedEblEnvelopes)
            .transferDocument(transportDocument.getTransportDocumentJson())
            .build();

    URI platformURL = transfereeToPlatformHost(transferee);

    String signatureResponse = sendTransferBlock(platformURL, transferblock);

    return eblEnvelopeService.verifyResponse(
        platformURL.getHost() + ":" + platformURL.getPort(),
        signedEblEnvelopeTO.eblEnvelopeHash(),
        signatureResponse);
  }

  private String sendTransferBlock(URI platformUrl, TransferblockTO transferblock) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    ResponseEntity<JsonNode> transferblockResponse =
        restTemplate.exchange(
            platformUrl, HttpMethod.PUT, new HttpEntity<>(transferblock, headers), JsonNode.class);

    if (transferblockResponse.getStatusCode().isError()) {
      throw ConcreteRequestErrorMessageException.internalServerError("Transfer failed.");
    }

    return Optional.ofNullable(transferblockResponse.getBody())
        .map(JsonNode::asText)
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
}
