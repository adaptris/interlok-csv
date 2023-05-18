package com.adaptris.csv.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.CustomPreferenceBuilder;
import com.adaptris.interlok.junit.scaffolding.services.TransformServiceExample;
import com.adaptris.util.text.xml.XPath;

public class CsvToXmlTest extends TransformServiceExample {

  public static final String LINE_ENDING = System.getProperty("line.separator");
  public static final String ILLEGAL_XML_CHAR = new String(new byte[] { (byte) 0x02 });

  public static final String CSV_INPUT_ILLEGAL_HEADER = "Name?,Order Date#,Date Attending,Total Paid" + ILLEGAL_XML_CHAR
      + LINE_ENDING + "Record1,\"Sep 15, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING;

  public static final String CSV_INPUT = "Name,Order Date,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING;

  public static final String CSV_ILLEGAL = "Name,Order Date,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",\"Oct 22, 2012 at 6:00 PM\"," + ILLEGAL_XML_CHAR + "" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING;

  public static final String CSV_TAB = "Name\tOrder Date\tDate Attending\tTotal Paid" + LINE_ENDING
      + "Record1\t\"Sep 15, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0" + LINE_ENDING
      + "Record2\t\"Sep 16, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0" + LINE_ENDING
      + "Record3\t\"Sep 17, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0" + LINE_ENDING;

  public static final String CSV_BLANK_HDR = "Name,,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING;

  private static final String EXAMPLE_COMMENT_HEADER = "\n<!--"
      + "\nThis is a simple CSV to XML transformation service; based on commons-csv"
      + "\nThe styles can be configured but popular defaults are"
      + "\navailable: DEFAULT, RFC4180, MYSQL, EXCEL and TAB_DELIMITED" + "\n"
      + "\nIf you wish to have complex transformation rules then you"
      + "\nshould be looking to use our flat file based transformation engine" + "\ninstead" + "\n-->\n";

  @Test
  public void testDoService_IllegalHeaders() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT_ILLEGAL_HEADER);
    CsvToXml svc = new CsvToXml();
    svc.setStripIllegalXmlChars(true);
    svc.setUniqueRecordNames(true);
    svc.setElementNamesFromFirstRecord(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/Order_Date_"));
  }

  @Test
  public void testDoService_DefaultStyle() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CsvToXml svc = new CsvToXml();
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_NonUniqueRecords() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CsvToXml svc = new CsvToXml();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[3]/csv-field-2"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_NonUniqueRecords_IncludeLineNumberAttribute() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CsvToXml svc = new CsvToXml();
    svc.setIncludeLineNumberAttribute(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record[@line='1']/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[@line='2']/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[@line='3']/csv-field-2"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_Excel() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CsvToXml svc = new CsvToXml();
    svc.setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.EXCEL_PREFERENCE));
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_TabDelimiter() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_TAB);
    CsvToXml svc = new CsvToXml();
    svc.setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.TAB_PREFERENCE));
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_CustomStyle() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CustomPreferenceBuilder builder = new CustomPreferenceBuilder();
    builder.setDelimiter(',');
    builder.setQuoteChar('"');
    builder.setRecordSeparator("\r\n");
    // Should now be the same as "DEFAULT"
    CsvToXml svc = new CsvToXml();
    svc.setPreferenceBuilder(builder);
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_HeaderRow() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CsvToXml svc = new CsvToXml();
    svc.setElementNamesFromFirstRecord(true);
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/Order_Date"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_HeaderRow_BlankField() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_BLANK_HDR);
    CsvToXml svc = new CsvToXml();
    svc.setElementNamesFromFirstRecord(true);
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/blank"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_MessageIsEncoded() throws Exception {
    DefaultMessageFactory fact = new DefaultMessageFactory();
    fact.setDefaultCharEncoding("ISO-8859-1");
    AdaptrisMessage msg = fact.newMessage(CSV_INPUT);
    CsvToXml svc = new CsvToXml();
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }

  @Test
  public void testDoService_OutputEncoding() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CsvToXml svc = new CsvToXml();
    svc.setOutputMessageEncoding("ISO-8859-1");
    svc.setStripIllegalXmlChars(false);
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }

  @Test
  public void testDoService_InvalidOutputEncoding() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CsvToXml svc = new CsvToXml();
    svc.setOutputMessageEncoding("ISO-8849-1");
    svc.setStripIllegalXmlChars(false);
    try {
      execute(svc, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testDoService_NotCSV() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("<hello>\nabc,def</hello>");
    CsvToXml svc = new CsvToXml();
    try {
      // This fails because the 2nd record has "more fields" than the first.
      execute(svc, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testDoService_IllegalXml() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_ILLEGAL);
    CsvToXml svc = new CsvToXml();
    svc.setStripIllegalXmlChars(false);
    execute(svc, msg);
    try {
      // This should fail.
      XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
      fail();
    } catch (Exception expected) {

    }
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_ILLEGAL);
    svc.setStripIllegalXmlChars(true);
    svc.setUniqueRecordNames(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-4"));
  }

  @Override
  protected CsvToXml retrieveObjectForSampleConfig() {
    return new CsvToXml();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + EXAMPLE_COMMENT_HEADER;
  }

}
