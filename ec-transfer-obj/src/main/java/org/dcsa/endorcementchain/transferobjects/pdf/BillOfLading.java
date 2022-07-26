package org.dcsa.endorcementchain.transferobjects.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/*
 * A class representing a Bill of Lading as rendered to paper.
 */

@Entity
@Table(name = "billoflading")
@NoArgsConstructor
public class BillOfLading {

  //ToDo replace with proper Mapstruct mapper
  public BillOfLading(String jsonSource) throws IOException, IllegalAccessException {
    ObjectMapper mapper = new ObjectMapper();
    BillOfLading bol = mapper.readValue(jsonSource, BillOfLading.class);

    Field[] fields = BillOfLading.class.getFields();
    for (Field field : fields) {
      field.set(this, field.get(bol));
    }
  }

  @Id
  @Column
  public String documentHash;

  @Column
  public String blno;

  @Column
  public String shipper;

  @Column
  public String exportImportSvcRef;

  @Column
  public String forwardingAgentReferences;

  @Column
  public String consignee;

  @Column
  public String notifyParty;

  @Column
  public String alsoNotifyParty;

  @Column
  public String bookingNoCarrierRef;

  @Column
  public String vessel;

  @Column
  public String voyageNo;

  @Column
  public String portOfLoading;

  @Column
  public String portOfDischarge;

  @Column
  public String preCarriageBy;

  @Column
  public String placeOfReceipt;

  @Column
  public String placeOfDelivery;

  @Column
  public String onwardInlandRoutingFinalDestination;

  @Column
  public String containerNumbers;

  @Column
  public String sizeTypes;

  @Column
  public String sealNos;

  @Column
  public String descriptionOfGoods;

  @Column
  public String cargoGrossWeights;

  @Column
  public String receiptDeliveryType;

  @Column
  public String cargoMovementType;

  @Column
  public String isPartLoad;

  @Column
  public String hsCode;

  @Column
  public String reeferSettings;

  @Column
  public String pointAndCountryOfOriginOfGoods;

  @Column
  public String shipperDeclaredValue;

  @Column
  public String freightAndChargesPayableByAt;

  @Column
  public String freightAndCharges;

  @Column
  public String basis;

  @Column
  public String rate;

  @Column
  public String prepaid;

  @Column
  public String collect;

  @Column
  public String totalFreight;

  @Column
  public String carrierClauses;

  @Column
  public String totalNumberOfContainersReceived;

  @Column
  public String placeOfIssue;

  @Column
  public String shippedOnBoardDate;

  @Column
  public String dateOfIssue;

  @Column
  public String noAndSequenceOfOriginalBLs;

  //ToDo replace with mapper, investigate if Mapstruct can be used
  public byte[] toPdf() throws IOException, IllegalAccessException {
    PDDocument pdfDocument = PDDocument.load(new File(Thread.currentThread().getContextClassLoader().getResource("BillOfLadingWithForms.pdf").getPath()));
    PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
    Field[] fields = BillOfLading.class.getFields();
    for (Field field : fields) {
      String k = field.toString().split("\\.")[6];
      acroForm.getField(k).setValue((String) field.get(this));
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    pdfDocument.save(baos);
    pdfDocument.close();
    return baos.toByteArray();
  }
}
