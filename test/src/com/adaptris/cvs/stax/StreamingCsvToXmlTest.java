package com.adaptris.cvs.stax;

import static com.adaptris.core.transform.csv.CsvToXmlTransformServiceTest.CSV_ILLEGAL;
import static com.adaptris.core.transform.csv.CsvToXmlTransformServiceTest.CSV_INPUT;
import static com.adaptris.core.transform.csv.CsvToXmlTransformServiceTest.CSV_INPUT_ILLEGAL_HEADER;
import static com.adaptris.core.transform.csv.CsvToXmlTransformServiceTest.LINE_ENDING;

import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.TransformServiceExample;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.stax.SaxonStreamWriterFactory;
import com.adaptris.csv.stax.StreamingCsvToXml;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.text.xml.XPath;

import net.sf.saxon.s9api.Serializer;

public class StreamingCsvToXmlTest extends TransformServiceExample {

  public static final String CSV_INPUT_EMPTY_FIELD = "Name,Order Date,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",,0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",,0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",,0" + LINE_ENDING;

  private static final String EXAMPLE_COMMENT_HEADER = "\n<!--"
      + "\nThis is a simple CSV to XML transformation service; based on super-csv"
      + "\nusing XMLStreamWriter."
      + "\nIf you wish to have complex transformation rules then you"
      + "\nshould be looking to use our flat file based transformation engine" + "\ninstead" + "\n-->\n";

  public StreamingCsvToXmlTest(String name) {
    super(name);
  }

  public void testSetDocumentFormatBuilder() throws Exception {
    StreamingCsvToXml svc = new StreamingCsvToXml();
    assertNotNull(svc.getPreferenceBuilder());
    assertEquals(BasicPreferenceBuilder.class, svc.getPreferenceBuilder().getClass());

    try {
      svc.setPreferenceBuilder(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(BasicPreferenceBuilder.class, svc.getPreferenceBuilder().getClass());
  }

  public void testSetOutputEncoding() throws Exception {
    StreamingCsvToXml svc = new StreamingCsvToXml();
    assertNull(svc.getOutputMessageEncoding());

    svc.setOutputMessageEncoding("UTF-8");
    assertEquals("UTF-8", svc.getOutputMessageEncoding());
  }

  public void testDoService_IllegalHeaders() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT_ILLEGAL_HEADER);
    StreamingCsvToXml svc = new StreamingCsvToXml();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/Order_Date_"));
  }

  public void testDoService_Defaults() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    StreamingCsvToXml svc = new StreamingCsvToXml();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/Order_Date"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/Order_Date"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  public void testDoService_SaxonWriter() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    StreamingCsvToXml svc = new StreamingCsvToXml(new BasicPreferenceBuilder(),
        new SaxonStreamWriterFactory(new KeyValuePair(Serializer.Property.INDENT.name(), "yes")));
    execute(svc, msg);
    System.err.println(msg.getContent());
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/Order_Date"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/Order_Date"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }


  public void testDoService_EmptyField() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT_EMPTY_FIELD);
    StreamingCsvToXml svc = new StreamingCsvToXml();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/Order_Date"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/Order_Date"));
    // Should be empty not null.
    assertEquals("", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/Date_Attending"));
    assertEquals("", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/Date_Attending"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  public void testDoService_IllegalXml() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_ILLEGAL);
    StreamingCsvToXml svc = new StreamingCsvToXml();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/Order_Date"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/Order_Date"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  public void testDoService_MessageIsEncoded() throws Exception {
    DefaultMessageFactory fact = new DefaultMessageFactory();
    fact.setDefaultCharEncoding("ISO-8859-1");
    AdaptrisMessage msg = fact.newMessage(CSV_INPUT);
    StreamingCsvToXml svc = new StreamingCsvToXml();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/Order_Date"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/Order_Date"));
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }

  public void testDoService_OutputEncoding() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    StreamingCsvToXml svc = new StreamingCsvToXml();
    svc.setOutputMessageEncoding("ISO-8859-1");
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("Sep 15, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/Order_Date"));
    assertEquals("Sep 16, 2012", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/Order_Date"));
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }

  public void testDoService_InvalidOutputEncoding() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    StreamingCsvToXml svc = new StreamingCsvToXml();
    svc.setOutputMessageEncoding("ISO-8849-1");
    try {
      execute(svc, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Override
  protected StreamingCsvToXml retrieveObjectForSampleConfig() {
    return new StreamingCsvToXml();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + EXAMPLE_COMMENT_HEADER;
  }

}
