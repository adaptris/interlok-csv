package com.adaptris.core.transform.csv;

import static com.adaptris.core.transform.csv.CsvToXmlTransformServiceTest.CSV_INPUT;
import static com.adaptris.core.transform.csv.CsvToXmlTransformServiceTest.LINE_ENDING;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.w3c.dom.Document;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.transform.TransformServiceExample;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

@Deprecated
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

  private static enum WhenToBreak {
    INPUT,
    OUTPUT,
    BOTH,
    NEVER
  };

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
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


  @Override
  protected StreamingCsvToXml retrieveObjectForSampleConfig() {
    return new StreamingCsvToXml();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + EXAMPLE_COMMENT_HEADER;
  }

}
