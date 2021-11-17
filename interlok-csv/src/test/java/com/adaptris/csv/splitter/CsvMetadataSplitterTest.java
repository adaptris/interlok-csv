package com.adaptris.csv.splitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.csv.CsvCaseUtils;
import com.adaptris.interlok.util.CloseableIterable;

/**
 * @author mwarman
 */
public class CsvMetadataSplitterTest {
  public static final String LINE_ENDING = System.getProperty("line.separator");

  private static final String CSV_INPUT_FIELD = "Name,Order Date,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",,0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",,0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",,0" + LINE_ENDING;

  @Test
  public void testSplitMessage() throws Exception{
    CsvMetadataSplitter splitter = new CsvMetadataSplitter();
    AdaptrisMessage adaptrisMessage = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT_FIELD);
    try (CloseableIterable<AdaptrisMessage> iterable = CloseableIterable.ensureCloseable(splitter.splitMessage(adaptrisMessage))) {
      List<AdaptrisMessage> results = CsvCaseUtils.toList(iterable);
      assertEquals(3, results.size());
      assertTrue(results.get(0).headersContainsKey("Name"));
      assertTrue(results.get(1).headersContainsKey("Name"));
      assertTrue(results.get(2).headersContainsKey("Name"));
      assertTrue(results.get(0).headersContainsKey("Order Date"));
      assertTrue(results.get(1).headersContainsKey("Order Date"));
      assertTrue(results.get(2).headersContainsKey("Order Date"));
      assertTrue(results.get(0).headersContainsKey("Date Attending"));
      assertTrue(results.get(1).headersContainsKey("Date Attending"));
      assertTrue(results.get(2).headersContainsKey("Date Attending"));
      assertTrue(results.get(0).headersContainsKey("Total Paid"));
      assertTrue(results.get(1).headersContainsKey("Total Paid"));
      assertTrue(results.get(2).headersContainsKey("Total Paid"));
      assertEquals("Record1", results.get(0).getMetadataValue("Name"));
      assertEquals("Record2", results.get(1).getMetadataValue("Name"));
      assertEquals("Record3", results.get(2).getMetadataValue("Name"));
      assertEquals("Sep 15, 2012", results.get(0).getMetadataValue("Order Date"));
      assertEquals("Sep 16, 2012", results.get(1).getMetadataValue("Order Date"));
      assertEquals("Sep 17, 2012", results.get(2).getMetadataValue("Order Date"));
      assertEquals("", results.get(0).getMetadataValue("Date Attending"));
      assertEquals("", results.get(1).getMetadataValue("Date Attending"));
      assertEquals("", results.get(2).getMetadataValue("Date Attending"));
      assertEquals("0", results.get(0).getMetadataValue("Total Paid"));
      assertEquals("0", results.get(1).getMetadataValue("Total Paid"));
      assertEquals("0", results.get(2).getMetadataValue("Total Paid"));
    }
  }


}