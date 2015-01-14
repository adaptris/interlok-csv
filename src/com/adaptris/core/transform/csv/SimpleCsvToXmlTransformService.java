package com.adaptris.core.transform.csv;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.transform.FfTransformService;
import com.adaptris.util.XmlUtils;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple CSV to XML using {@link CSVParser}
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
 * </code>
 * </pre>
 * Then the output (without a header row) would be
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
 * And with a header row specified :
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
 * @config simple-csv-to-xml-transform
 * @license BASIC
 * 
 */
@XStreamAlias("simple-csv-to-xml-transform")
public class SimpleCsvToXmlTransformService extends ServiceImp {

  private static final String CSV_RECORD_NAME = "record";
  private static final String XML_ROOT_ELEMENT = "csv-xml";
  private static final String CSV_FIELD_NAME = "csv-field";

  // XML 1.0
  // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
  // private static final String XML10 = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff"
  // + "]";

  // XML 1.1
  // [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
  // private static final String XML11 = "[^" + "\u0001-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]+";

  private static final String ILLEGAL_XML = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]";
  private static final String[] INVALID_ELEMENT_CHARS =
  {
      "\\\\", "\\?", "\\*", "\\:", " ", "\\|", "&", "\\\"", "\\'", "<", ">", "\\)", "\\(", "\\/", "#"
  };
  private static final String ELEM_REPL_VALUE = "_";

  private Boolean elementNamesFromFirstRecord;
  @NotNull
  @AutoPopulated
  private FormatBuilder format;
  private Boolean stripIllegalXmlChars = null;
  private String outputMessageEncoding = null;
  private Boolean uniqueRecordNames = null;

  public SimpleCsvToXmlTransformService() {
    this(new BasicFormatBuilder());
  }

  public SimpleCsvToXmlTransformService(FormatBuilder f) {
    setFormat(f);
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Document doc = transform(msg);
      writeXmlDocument(doc, msg);
    }
    catch (Exception e) {
      rethrowServiceException(e);
    }
  }

  @Override
  public void close() {

  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

  private Document transform(AdaptrisMessage msg) throws ServiceException {
    Document doc = null;
    CSVFormat format = getFormat().createFormat();

    try (Reader in = msg.getReader()) {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      CSVParser parser = format.parse(in);
      List<CSVRecord> records = parser.getRecords();
      List<String> elemNames = elemNamesFirstRec() ? createElementNames(records.get(0)) : createElementNames(records.get(0).size());
      int index = elemNamesFirstRec() ? 1 : 0;
      Element root = doc.createElement(XML_ROOT_ELEMENT);
      doc.appendChild(root);
      int recordCount = 1;
      while (index < records.size()) {
        CSVRecord next = records.get(index);
        String recordName = CSV_RECORD_NAME + ((uniqueRecords()) ? "-" + recordCount : "");
        Element recordElement = addNewElement(doc, root, recordName);
        // Create the sub doc here.
        List<Element> csvFields = createFields(doc, records.get(index), elemNames);
        for (Element field : csvFields) {
          recordElement.appendChild(field);
        }
        index++;
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

  public FormatBuilder getFormat() {
    return format;
  }

  public void setFormat(FormatBuilder csvFormat) {
    if (csvFormat == null) {
      throw new IllegalArgumentException("DocumentFormatBuilder may not be null");
    }
    this.format = csvFormat;
  }

  public Boolean getStripIllegalXmlChars() {
    return stripIllegalXmlChars;
  }

  /**
   * Specify whether or not to strip illegal XML characters from all the data before converting to XML.
   * <p>
   * The following regular expression is used to strip out all invalid XML 1.0 characters :
   * <code>"[^\u0009\r\n\u0020-\uD7FF\uE000-\uFFFD\ud800\udc00-\udbff\udfff]"</code>.
   * </p>
   * 
   * @param s true to enable stripping, default is null (true)
   */
  public void setStripIllegalXmlChars(Boolean s) {
    this.stripIllegalXmlChars = s;
  }

  boolean stripIllegalXmlChars() {
    return getStripIllegalXmlChars() != null ? getStripIllegalXmlChars().booleanValue() : true;
  }

  public String getOutputMessageEncoding() {
    return outputMessageEncoding;
  }

  /**
   * Set the encoding for the resulting XML document.
   * <p>
   * If not specified the following rules will be applied:
   * </p>
   * <ol>
   * <li>If the {@link AdaptrisMessage#getCharEncoding()} is non-null then that will be used.</li>
   * <li>UTF-8</li>
   * </ol>
   * <p>
   * As a result; the character encoding on the message is always set using {@link AdaptrisMessage#setCharEncoding(String)}.
   * </p>
   * 
   * @param encoding the character
   */
  public void setOutputMessageEncoding(String encoding) {
    outputMessageEncoding = encoding;
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

  private Node createTextNode(Document doc, String value) {
    String munged = stripIllegalXmlChars() ? value.replaceAll(ILLEGAL_XML, "") : value;
    return doc.createTextNode(munged);
  }

  private Element addNewElement(Document doc, Element parent, String name) {
    Element newElement = doc.createElement(name);
    parent.appendChild(newElement);
    return newElement;
  }

  /**
   * Helper method to write the XML document to the AdaptrisMessage taking into account any encoding requirements.
   * 
   * @param doc the XML document
   * @param msg the AdaptrisMessage
   * @throws Exception
   */
  private void writeXmlDocument(Document doc, AdaptrisMessage msg) throws Exception {
    try (OutputStream out = msg.getOutputStream()) {
      String encoding = evaluateEncoding(msg);
      new XmlUtils().writeDocument(doc, out, encoding);
      msg.setCharEncoding(encoding);
    }
  }

  private String evaluateEncoding(AdaptrisMessage msg) {
    String encoding = "UTF-8";
    if (!isEmpty(getOutputMessageEncoding())) {
      encoding = getOutputMessageEncoding();
    }
    else if (!isEmpty(msg.getCharEncoding())) {
      encoding = msg.getCharEncoding();
    }
    return encoding;
  }

  private String safeElementName(String input) {
    String name = input;
    name = stripIllegalXmlChars() ? name.replaceAll(ILLEGAL_XML, "") : name;
    for (String invalid : INVALID_ELEMENT_CHARS) {
      name = name.replaceAll(invalid, ELEM_REPL_VALUE);
    }
    if (isEmpty(name)) {
      name = "blank";
    }
    return name;
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
   * @param b true to generate a unique element name for each line in the CSV, default null (true)
   */
  public void setUniqueRecordNames(Boolean b) {
    this.uniqueRecordNames = b;
  }

  boolean uniqueRecords() {
    return getUniqueRecordNames() != null ? getUniqueRecordNames() : true;
  }
}
