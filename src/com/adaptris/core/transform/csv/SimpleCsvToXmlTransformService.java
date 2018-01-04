package com.adaptris.core.transform.csv;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.FfTransformService;
import com.adaptris.core.util.XmlHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple CSV to XML using {@link CSVParser}.
 * 
 * <p>
 * This transformation uses <a href="http://commons.apache.org/proper/commons-csv/">commons-csv</a> as the parsing engine for a CSV
 * file. Basic parsing options are supported : {@link BasicFormatBuilder.Style#DEFAULT DEFAULT},
 * {@link BasicFormatBuilder.Style#EXCEL EXCEL}, {@link BasicFormatBuilder.Style#RFC4180 RFC4180},
 * {@link BasicFormatBuilder.Style#MYSQL MYSQL} and {@link BasicFormatBuilder.Style#TAB_DELIMITED TAB DELIMITED} which correspond to
 * the base formats defined by {@link CSVFormat}. Custom CSV formats are provided via {@link CustomFormatBuilder}. In the event that
 * you have more complex requirements then you should be looking to use the {@link FfTransformService} instead.
 * </p>
 * <p>
 * If {@link #setElementNamesFromFirstRecord(Boolean)} is true, then the first record is used to generate the element names;
 * otherwise names are auto-generated based on the count of fields in the first row. Results are undefined if subsequent record
 * contains more fields than the first record. If the first row contains a blank field then the <code>blank</code> is used as the
 * element name. Note that if your header rows contains characters that would not be allowed in an standard XML element name then it
 * will be replaced with an '_', so "Order Date" becomes "Order_Date".
 * </p>
 * <p>
 * For example, given an input document :
 * 
 * <pre>
 * <code>
Event Name,Order Date,Ticket Type,Date Attending,Total Paid
Glastonbury,"Sep 15, 2012",Free entry,"Jun 26, 2014 at 6:00 PM",0
Reading Festival,"Sep 16, 2012",Free entry,"Aug 30, 2014 at 6:00 PM",0
 * </code> </pre> Then the output (without a header row, and with unique-record-names=true) would be
 * 
 * <pre>
 * <code>
 * &lt;csv-xml>
 *   &lt;record-1>
 *     &lt;csv-field-1>Event Name&lt;/csv-field-1>
 *     &lt;csv-field-2>Order Date&lt;/csv-field-2>
 *     &lt;csv-field-3>Ticket Type&lt;/csv-field-3>
 *     &lt;csv-field-4>Date Attending&lt;/csv-field-4>
 *     &lt;csv-field-5>Total Paid&lt;/csv-field-5>
 *   &lt;/record-1>
 *   &lt;record-2>
 *     &lt;csv-field-1>Glastonbury&lt;/csv-field-1>
 *     &lt;csv-field-2>Sep 15, 2012&lt;/csv-field-2>
 *     &lt;csv-field-3>Free entry&lt;/csv-field-3>
 *     &lt;csv-field-4>Jun 26, 2014 at 6:00 PM&lt;/csv-field-4>
 *     &lt;csv-field-5>0&lt;/csv-field-5>
 *   &lt;/record-2>
 *   &lt;record-3>
 *     &lt;csv-field-1>Reading Festival&lt;/csv-field-1>
 *     &lt;csv-field-2>Sep 16, 2012&lt;/csv-field-2>
 *     &lt;csv-field-3>Free entry&lt;/csv-field-3>
 *     &lt;csv-field-4>Aug 30, 2014 at 6:00 PM&lt;/csv-field-4>
 *     &lt;csv-field-5>0&lt;/csv-field-5>
 *   &lt;/record-3>
 * </code> </pre>
 * 
 * And with a header row specified (and unique-record-names = true):
 * 
 * <pre>
 * <code>
 * &lt;csv-xml>
 *   &lt;record-1>
 *     &lt;Event_Name>Glastonbury&lt;/Event_Name>
 *     &lt;Order_Date>Sep 15, 2012&lt;/Order_Date>
 *     &lt;Ticket_Type>Free entry&lt;/Ticket_Type>
 *     &lt;Date_Attending>Jun 26, 2014 at 6:00 PM&lt;/Date_Attending>
 *     &lt;Total_Paid>0&lt;/Total_Paid>
 *   &lt;/record-1>
 *   &lt;record-2>
 *     &lt;Event_Name>Reading Festival&lt;/Event_Name>
 *     &lt;Order_Date>Sep 16, 2012&lt;/Order_Date>
 *     &lt;Ticket_Type>Free entry&lt;/Ticket_Type>
 *     &lt;Date_Attending>Aug 30, 2014 at 6:00 PM&lt;/Date_Attending>
 *     &lt;Total_Paid>0&lt;/Total_Paid>
 *   &lt;/record-2>
 * </code> </pre>
 * 
 * @config simple-csv-to-xml-transform
 * 
 */
@XStreamAlias("simple-csv-to-xml-transform")
@AdapterComponent
@ComponentProfile(summary = "Easily transform a document from CSV to XML", tag = "service,transform,csv,xml")
@DisplayOrder(order = {"format", "outputMessageEncoding", "elementNamesFromFirstRecord", "uniqueRecordNames", "stripIllegalXmlChars"})
public class SimpleCsvToXmlTransformService extends CsvToXmlServiceImpl {

  @InputFieldDefault(value = "false")
  private Boolean elementNamesFromFirstRecord;
  @InputFieldDefault(value = "false")
  private Boolean uniqueRecordNames = null;

  public SimpleCsvToXmlTransformService() {
    super();
  }

  public SimpleCsvToXmlTransformService(FormatBuilder f) {
    this();
    setFormat(f);
  }


  protected Document transform(AdaptrisMessage msg) throws ServiceException {
    Document doc = null;
    CSVFormat format = getFormat().createFormat();

    try (Reader in = msg.getReader()) {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      CSVParser parser = format.parse(in);
      int recordCount = 1;
      boolean firstRecord = true;
      List<String> elemNames = null;
      Element root = doc.createElement(XML_ROOT_ELEMENT);
      doc.appendChild(root);
      for (CSVRecord record : parser) {
        if (firstRecord) {
          firstRecord = false;
          if (elemNamesFirstRec()) {
            elemNames = createElementNames(record);
            continue;
          }
          else {
            elemNames = createElementNames(record.size());
          }
        }
        String recordName = CSV_RECORD_NAME + ((uniqueRecords()) ? "-" + recordCount : "");
        Element recordElement = addNewElement(doc, root, recordName);
        // Create the sub doc here.
        List<Element> csvFields = createFields(doc, record, elemNames);
        for (Element field : csvFields) {
          recordElement.appendChild(field);
        }
        recordCount++;
      }
    }
    catch (Exception e) {
      throw new ServiceException("Failed to convert CSV to XML", e);
    }
    return doc;
  }

  public Boolean getElementNamesFromFirstRecord() {
    return elementNamesFromFirstRecord;
  }

  /**
   * Specify if element names should be derived from the first record.
   * 
   * @param b true to set the first record as the header.
   */
  public void setElementNamesFromFirstRecord(Boolean b) {
    this.elementNamesFromFirstRecord = b;
  }

  boolean elemNamesFirstRec() {
    return getElementNamesFromFirstRecord() != null ? getElementNamesFromFirstRecord().booleanValue() : false;
  }


  private static final List<String> createElementNames(int count) {
    List<String> result = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      result.add(CSV_FIELD_NAME + "-" + i);
    }
    return result;
  }

  private final List<String> createElementNames(CSVRecord hdr) {
    List<String> result = new ArrayList<>();
    for (String hdrValue : hdr) {
      result.add(safeElementName(hdrValue));
    }
    return result;
  }

  private List<Element> createFields(Document doc, CSVRecord record, List<String> headerNames) {
    List<Element> result = new ArrayList<>();
    for (int i = 0; i < record.size(); i++) {
      Element element = doc.createElement(headerNames.get(i));
      element.appendChild(createTextNode(doc, record.get(i)));
      result.add(element);
    }
    return result;
  }

  private String safeElementName(String input) {
    return XmlHelper.safeElementName(input, "blank");
  }

  public Boolean getUniqueRecordNames() {
    return uniqueRecordNames;
  }
  
  /**
   * Specify whether or not to have unique element names for each record in the CSV file.
   * 
   * <p>
   * If there are multiple records in then each record can have a unique element name; e.g <code>record-1, record-2, record-3</code>
   * and so on. If false, then they are all called <code>record</code>
   * </p>
   * 
   * @param b true to generate a unique element name for each line in the CSV, default null (false)
   */
  public void setUniqueRecordNames(Boolean b) {
    this.uniqueRecordNames = b;
  }

  boolean uniqueRecords() {
    return getUniqueRecordNames() != null ? getUniqueRecordNames() : false;
  }
}
