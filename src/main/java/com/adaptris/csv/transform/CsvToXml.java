package com.adaptris.csv.transform;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.csv.OrderedCsvMapReader;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CSV to XML using {@code net.sf.supercsv:super-csv} replacing
 * {@link com.adaptris.core.transform.csv.SimpleCsvToXmlTransformService}.
 * <p>
 * This transformation uses {@code net.sf.supercsv:super-csv} as the parsing engine for a CSV file
 * to write each row as part of an XML document.
 * <p>
 * If {@link #setElementNamesFromFirstRecord(Boolean)} is true, then the first record is used to
 * generate the element names; otherwise names are auto-generated based on the count of fields in
 * the first row. Results are undefined if subsequent record contains more fields than the first
 * record. If the first row contains a blank field then the <code>blank</code> is used as the
 * element name. Note that if your header rows contains characters that would not be allowed in an
 * standard XML element name then it will be replaced with an '_', so "Order Date" becomes
 * "Order_Date".
 * </p>
 * <p>
 * For example, given an input document :
 *
 * <pre>
 * <code>
Event Name,Order Date,Ticket Type,Date Attending,Total Paid
Glastonbury,"Sep 15, 2012",Free entry,"Jun 26, 2014 at 6:00 PM",0
Reading Festival,"Sep 16, 2012",Free entry,"Aug 30, 2014 at 6:00 PM",0
 * </code>
 * </pre>
 *
 * Then the output (without a header row, and with unique-record-names=true) would be
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
 * </code>
 * </pre>
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
 * </code>
 * </pre>
 *
 * @config csv-to-xml-transform
 */
@XStreamAlias("csv-to-xml-transform")
@AdapterComponent
@ComponentProfile(summary = "Transform a document from CSV to XML",
    tag = "service,transform,csv,xml", since = "3.11.0")
@DisplayOrder(order = {"format", "outputMessageEncoding", "elementNamesFromFirstRecord", "uniqueRecordNames", "stripIllegalXmlChars"})
@NoArgsConstructor
public class CsvToXml extends CsvToXmlServiceImpl {

  /**
   * Specify if element names should be derived from the first record.
   *
   */
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean elementNamesFromFirstRecord;
  /**
   * Specify whether or not to have unique element names for each record in the CSV file.
   *
   * <p>
   * If there are multiple records in then each record can have a unique element name; e.g
   * <code>record-1, record-2, record-3</code> and so on. If false, then they are all called
   * <code>record</code> with an associated attribute that declares the line number.
   * </p>
   *
   */
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean uniqueRecordNames = null;

  @Override
  protected Document transform(AdaptrisMessage msg) throws ServiceException {
    Document doc = null;
    try (Reader in = msg.getReader();
        OrderedCsvMapReader csvReader =
            new OrderedCsvMapReader(in, buildPreferences());) {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      int recordCount = 1;
      boolean firstRecord = true;
      List<String> elemNames = null;
      Element root = doc.createElement(XML_ROOT_ELEMENT);
      doc.appendChild(root);
      for (List<String> record; (record = csvReader.readNext()) != null;) {
        if (firstRecord) {
          firstRecord = false;
          if (elemNamesFirstRec()) {
            elemNames = safeElementNames(record);
            continue;
          }
          else {
            elemNames = createElementNames(record.size());
          }
        }
        String recordName = CSV_RECORD_NAME + ((uniqueRecords()) ? "-" + recordCount : "");
        Element recordElement = addNewElement(doc, root, recordName);
        if (includeLineNumberAttribute()) {
          recordElement.setAttribute("line", String.valueOf(recordCount));
        }
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

  private boolean elemNamesFirstRec() {
    return BooleanUtils.toBooleanDefaultIfNull(getElementNamesFromFirstRecord(), false);
  }


  private static final List<String> createElementNames(int count) {
    List<String> result = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      result.add(CSV_FIELD_NAME + "-" + i);
    }
    return safeElementNames(result);
  }

  private List<Element> createFields(Document doc, List<String> record, List<String> headerNames) {
    List<Element> result = new ArrayList<>();
    for (int i = 0; i < record.size(); i++) {
      Element element = doc.createElement(headerNames.get(i));
      element.appendChild(createTextNode(doc, record.get(i)));
      result.add(element);
    }
    return result;
  }

  private boolean uniqueRecords() {
    return BooleanUtils.toBooleanDefaultIfNull(getUniqueRecordNames(), false);
  }

}
