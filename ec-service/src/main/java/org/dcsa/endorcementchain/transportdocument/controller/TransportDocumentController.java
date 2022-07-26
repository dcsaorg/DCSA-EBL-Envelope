package org.dcsa.endorcementchain.transportdocument.controller;

import org.dcsa.endorcementchain.persistence.entity.TransportDocument;
import org.dcsa.endorcementchain.persistence.repository.TransportDocumentRepository;
import org.dcsa.endorcementchain.transferobjects.pdf.BillOfLading;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping("${spring.application.api-path}")
public class TransportDocumentController {

  public static final String API_PATH = "/transport-documents";

  @Value("${spring.application.api-path}")
  private String contextPath;

  @Autowired
  TransportDocumentRepository transportDocumentRepository;

  @GetMapping(value = API_PATH + "/{transportDocumentId}", produces = {"application/json"})
  @ResponseBody
  @CrossOrigin(origins = "*")
  public ResponseEntity<TransportDocument> getTransportDocument(@PathVariable String transportDocumentId){
    Optional<TransportDocument> transportDocument = transportDocumentRepository.findById(transportDocumentId);
    return new ResponseEntity<TransportDocument>(transportDocument.get(), HttpStatus.OK);
  }

  @GetMapping(value = API_PATH + "/{transportDocumentId}/pdf", produces = {"application/pdf"})
  @ResponseBody
  public byte[] getTransportDocumentAsPdf(@PathVariable String transportDocumentId) throws java.io.IOException, java.lang.IllegalAccessException {
    Optional<TransportDocument> transportDocument = transportDocumentRepository.findById(transportDocumentId);
    BillOfLading bol = new BillOfLading(transportDocument.get().getTransportDocumentJson().asText());
    return bol.toPdf();
  }

  @PostMapping(value = API_PATH, consumes = {"application/json"}, produces = {"application/json"})
  @ResponseBody
  public ResponseEntity<TransportDocument> addTransportDocument(@RequestBody TransportDocument transportDocument, UriComponentsBuilder builder){
    transportDocumentRepository.save(transportDocument);
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(builder.path(contextPath + API_PATH + "/{id}").buildAndExpand(transportDocument.getDocumentHash()).toUri());
    return new ResponseEntity<TransportDocument>(headers, HttpStatus.CREATED);
  }

}
