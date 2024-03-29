package com.adaptris.csv.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.interlok.junit.scaffolding.services.TransformServiceExample;
import com.adaptris.util.text.xml.XPath;

public class UncheckedCsvToXmlTest extends TransformServiceExample {

  private static final String LINE_ENDING = System.getProperty("line.separator");

  private static final String CSV_INPUT =
      "HEADER,19052017" + LINE_ENDING
    + "PRODUCT-CODE,BARCODE,DP,PROMPRICE,DISCOUNT,INSTOCK,REPORT,REPORT-DATE,SOURCE" + LINE_ENDING
    + "XXXXXXXX,1234567890123,2.75,0.00,5.00,2,,,GSL" + LINE_ENDING
    + "YYYYYYYY,1234567890123,20.42,0.00,5.00,0,NYP,24/07/2017,GSL" + LINE_ENDING
    + "AAAAAAAA,1234567890123,2.75,0.00,5.00,88,O/P,13/03/2017,GSL" + LINE_ENDING
    + "BBBBBBBB,1234567890123,3.60,2.94,5.00,21,,,GSL" + LINE_ENDING
    + "TRAILER,4" + LINE_ENDING;

  private static final String NOT_CSV =
      "<hello>" + LINE_ENDING
    + "abc,def" + LINE_ENDING
    + "</hello>";

  private static final String EXAMPLE_COMMENT_HEADER = "\n<!--"
    + "\nThis is a simple CSV to XML transformation service; based on commons-csv"
    + "\nThe styles can be configured but popular defaults are"
    + "\navailable: DEFAULT, RFC4180, MYSQL, EXCEL and TAB_DELIMITED" + "\n"
    + "\n-->\n";


  @Test
  public void testDoService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    UncheckedCsvToXml svc = new UncheckedCsvToXml();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("19052017", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/csv-field-2"));
    assertEquals("GSL", xpath.selectSingleTextItem(doc, "/csv-xml/record[3]/csv-field-9"));
    assertEquals("YYYYYYYY", xpath.selectSingleTextItem(doc, "/csv-xml/record[4]/csv-field-1"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_IncludeLineNumberAttribute() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    UncheckedCsvToXml svc = new UncheckedCsvToXml();
    svc.setIncludeLineNumberAttribute(true);
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("19052017", xpath.selectSingleTextItem(doc, "/csv-xml/record[@line='1']/csv-field-2"));
    assertEquals("GSL", xpath.selectSingleTextItem(doc, "/csv-xml/record[@line='3']/csv-field-9"));
    assertEquals("YYYYYYYY", xpath.selectSingleTextItem(doc, "/csv-xml/record[@line='4']/csv-field-1"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testDoService_Exception() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage(CSV_INPUT);
    UncheckedCsvToXml svc = new UncheckedCsvToXml();
    try {
      execute(svc, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testDoService_NotCsv() throws Exception {
    // This is just a 3 line CSV file... (gasp).
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(NOT_CSV);
    UncheckedCsvToXml svc = new UncheckedCsvToXml();
    execute(svc, msg);
    XPath xpath = new XPath();
    Document doc = XmlHelper.createDocument(msg, new DocumentBuilderFactoryBuilder());
    assertEquals("<hello>", xpath.selectSingleTextItem(doc, "/csv-xml/record[1]/csv-field-1"));
    assertEquals("def", xpath.selectSingleTextItem(doc, "/csv-xml/record[2]/csv-field-2"));
    assertEquals("</hello>", xpath.selectSingleTextItem(doc, "/csv-xml/record[3]/csv-field-1"));
  }

  @Override
  protected UncheckedCsvToXml retrieveObjectForSampleConfig() {
    return new UncheckedCsvToXml();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + EXAMPLE_COMMENT_HEADER;
  }

}
