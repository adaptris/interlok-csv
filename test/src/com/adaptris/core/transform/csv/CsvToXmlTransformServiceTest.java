package com.adaptris.core.transform.csv;

import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.TransformServiceExample;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

public class CsvToXmlTransformServiceTest extends TransformServiceExample {

  private static final String LINE_ENDING = System.getProperty("line.separator");
  private static final String ILLEGAL_XML_CHAR = new String(new byte[]
  {
    (byte) 0x02
  });

  private static final String CSV_INPUT_ILLEGAL_HEADER = "Name?,Order Date#,Date Attending,Total Paid" + ILLEGAL_XML_CHAR
      + LINE_ENDING + "Record1,\"Sep 15, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING;

  private static final String CSV_INPUT = "Name,Order Date,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING;

  private static final String CSV_ILLEGAL = "Name,Order Date,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",\"Oct 22, 2012 at 6:00 PM\"," + ILLEGAL_XML_CHAR + "" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING;

  private static final String CSV_TAB = "Name\tOrder Date\tDate Attending\tTotal Paid" + LINE_ENDING
      + "Record1\t\"Sep 15, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0" + LINE_ENDING
      + "Record2\t\"Sep 16, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0" + LINE_ENDING
      + "Record3\t\"Sep 17, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0" + LINE_ENDING;

  private static final String CSV_MYSQL = "Name\tOrder Date\tDate Attending\tTotal Paid\n"
      + "Record1\t\"Sep 15, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0\n"
      + "Record2\t\"Sep 16, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0\n"
      + "Record3\t\"Sep 17, 2012\"\t\"Oct 22, 2012 at 6:00 PM\"\t0\n";

  private static final String CSV_BLANK_HDR = "Name,,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",\"Oct 22, 2012 at 6:00 PM\",0" + LINE_ENDING;

  private static final String EXAMPLE_COMMENT_HEADER = "\n<!--"
      + "\nThis is a simple CSV to XML transformation service; based on commons-csv"
      + "\nThe styles are not configurable, only the standard styles are"
      + "\navailable: DEFAULT, RFC4180, MYSQL, EXCEL and TAB_DELIMITED" + "\n"
      + "\nIf you wish to have complex transformation rules then you"
      + "\nshould be looking to use our flat file based transformation engine" + "\ninstead" + "\n-->\n";

  public CsvToXmlTransformServiceTest(String name) {
    super(name);
  }

  public void testSetStripIllegalChars() throws Exception {
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    assertNull(svc.getStripIllegalXmlChars());
    assertEquals(true, svc.stripIllegalXmlChars());

    svc.setStripIllegalXmlChars(Boolean.TRUE);
    assertEquals(Boolean.TRUE, svc.getStripIllegalXmlChars());
    assertEquals(true, svc.stripIllegalXmlChars());

    svc.setStripIllegalXmlChars(null);
    assertNull(svc.getStripIllegalXmlChars());
    assertEquals(true, svc.stripIllegalXmlChars());
  }

  public void testSetDocumentFormatBuilder() throws Exception {
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    assertNotNull(svc.getFormat());
    assertEquals(BasicFormatBuilder.class, svc.getFormat().getClass());

    try {
      svc.setFormat(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(BasicFormatBuilder.class, svc.getFormat().getClass());
  }

  public void testSetElementNamesFromHeader() throws Exception {
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    assertNull(svc.getElementNamesFromFirstRecord());
    assertEquals(false, svc.elemNamesFirstRec());

    svc.setElementNamesFromFirstRecord(Boolean.TRUE);
    assertEquals(Boolean.TRUE, svc.getElementNamesFromFirstRecord());
    assertEquals(true, svc.elemNamesFirstRec());

    svc.setElementNamesFromFirstRecord(null);
    assertNull(svc.getElementNamesFromFirstRecord());
    assertEquals(false, svc.elemNamesFirstRec());
  }

  public void testSetOutputEncoding() throws Exception {
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    assertNull(svc.getOutputMessageEncoding());

    svc.setOutputMessageEncoding("UTF-8");
    assertEquals("UTF-8", svc.getOutputMessageEncoding());
  }

  public void testDoService_IllegalHeaders() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT_ILLEGAL_HEADER);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setStripIllegalXmlChars(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/Order_Date_"));
  }

  public void testDoService_DefaultStyle() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_NonUniqueRecords() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setUniqueRecordNames(false);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[3]/csv-field-2"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_RFC4180() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setFormat(new BasicFormatBuilder(BasicFormatBuilder.Style.RFC4180));
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_Excel() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setFormat(new BasicFormatBuilder(BasicFormatBuilder.Style.EXCEL));
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_TabDelimiter() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_TAB);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setFormat(new BasicFormatBuilder(BasicFormatBuilder.Style.TAB_DELIMITED));
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_MySql() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_MYSQL);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setFormat(new BasicFormatBuilder(BasicFormatBuilder.Style.MYSQL));
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("\"Sep 15, 2012\"", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_CustomStyle() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    CustomFormatBuilder builder = new CustomFormatBuilder();
    builder.setDelimiter(',');
    builder.setQuoteChar('"');
    builder.setRecordSeparator("\r\n");
    builder.setIgnoreEmptyLines(true);
    // Should now be the same as "DEFAULT"
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService(builder);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_HeaderRow() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setElementNamesFromFirstRecord(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/Order_Date"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_HeaderRow_BlankField() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_BLANK_HDR);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setElementNamesFromFirstRecord(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/blank"));
    assertEquals("UTF-8", msg.getCharEncoding());
  }

  public void testDoService_MessageIsEncoded() throws Exception {
    DefaultMessageFactory fact = new DefaultMessageFactory();
    fact.setDefaultCharEncoding("ISO-8859-1");
    AdaptrisMessage msg = fact.newMessage(CSV_INPUT);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("ISO-8859-1", msg.getCharEncoding());
  }

  public void testDoService_OutputEncoding() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setOutputMessageEncoding("ISO-8859-1");
    svc.setStripIllegalXmlChars(false);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("Order Date", xpath.selectSingleTextItem(doc, "/csv-xml/record-1/csv-field-2"));
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-2"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record-3/csv-field-2"));
    assertEquals("ISO-8859-1", msg.getCharEncoding());
  }

  public void testDoService_InvalidOutputEncoding() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setOutputMessageEncoding("ISO-8849-1");
    svc.setStripIllegalXmlChars(false);
    try {
      execute(svc, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testDoService_NotCSV() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("<hello>\nabc,def</hello>");
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    try {
      // This fails because the 2nd record has "more fields" than the first.
      execute(svc, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testDoService_IllegalXml() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_ILLEGAL);
    SimpleCsvToXmlTransformService svc = new SimpleCsvToXmlTransformService();
    svc.setStripIllegalXmlChars(false);
    execute(svc, msg);
    try {
      // This should fail.
      Document doc = XmlHelper.createDocument(msg, null);
      fail();
    }
    catch (Exception expected) {

    }
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_ILLEGAL);
    svc.setStripIllegalXmlChars(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, null);
    assertEquals("", xpath.selectSingleTextItem(doc, "/csv-xml/record-2/csv-field-4"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new SimpleCsvToXmlTransformService();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + EXAMPLE_COMMENT_HEADER;
  }

}
