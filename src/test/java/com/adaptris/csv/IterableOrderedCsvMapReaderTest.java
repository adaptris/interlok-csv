package com.adaptris.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * @author mwarman
 */
public class IterableOrderedCsvMapReaderTest {
  public static final String LINE_ENDING = System.getProperty("line.separator");

  private static final String CSV_INPUT_FIELD = "Name,Order Date,Date Attending,Total Paid" + LINE_ENDING
      + "Record1,\"Sep 15, 2012\",,0" + LINE_ENDING
      + "Record2,\"Sep 16, 2012\",,0" + LINE_ENDING
      + "Record3,\"Sep 17, 2012\",,0" + LINE_ENDING;

  @Test
  public void testIteratorWithSetHeader() throws Exception {
    try (IterableOrderedCsvMapReader reader = new IterableOrderedCsvMapReader(new StringReader(CSV_INPUT_FIELD), new BasicPreferenceBuilder().build())){
      reader.setHeaders(reader.getHeader(true));
      List<Map<String, String>> results = CsvCaseUtils.toList(reader);
      assertTrue(results.get(0).containsKey("Name"));
      assertTrue(results.get(1).containsKey("Name"));
      assertTrue(results.get(2).containsKey("Name"));
      assertTrue(results.get(0).containsKey("Order Date"));
      assertTrue(results.get(1).containsKey("Order Date"));
      assertTrue(results.get(2).containsKey("Order Date"));
      assertTrue(results.get(0).containsKey("Date Attending"));
      assertTrue(results.get(1).containsKey("Date Attending"));
      assertTrue(results.get(2).containsKey("Date Attending"));
      assertTrue(results.get(0).containsKey("Total Paid"));
      assertTrue(results.get(1).containsKey("Total Paid"));
      assertTrue(results.get(2).containsKey("Total Paid"));
      assertEquals("Record1", results.get(0).get("Name"));
      assertEquals("Record2", results.get(1).get("Name"));
      assertEquals("Record3", results.get(2).get("Name"));
      assertEquals("Sep 15, 2012", results.get(0).get("Order Date"));
      assertEquals("Sep 16, 2012", results.get(1).get("Order Date"));
      assertEquals("Sep 17, 2012", results.get(2).get("Order Date"));
      assertNull(results.get(0).get("Date Attending"));
      assertNull(results.get(1).get("Date Attending"));
      assertNull(results.get(2).get("Date Attending"));
      assertEquals("0", results.get(0).get("Total Paid"));
      assertEquals("0", results.get(1).get("Total Paid"));
      assertEquals("0", results.get(2).get("Total Paid"));
    }
  }

  @Test
  public void testIteratorWithoutSetHeader() throws Exception {
    try (IterableOrderedCsvMapReader reader = new IterableOrderedCsvMapReader(new StringReader(CSV_INPUT_FIELD), new BasicPreferenceBuilder().build())){
      List<Map<String, String>> results = CsvCaseUtils.toList(reader);
      assertTrue(results.get(0).containsKey("Name"));
      assertTrue(results.get(1).containsKey("Name"));
      assertTrue(results.get(2).containsKey("Name"));
      assertTrue(results.get(0).containsKey("Order Date"));
      assertTrue(results.get(1).containsKey("Order Date"));
      assertTrue(results.get(2).containsKey("Order Date"));
      assertTrue(results.get(0).containsKey("Date Attending"));
      assertTrue(results.get(1).containsKey("Date Attending"));
      assertTrue(results.get(2).containsKey("Date Attending"));
      assertTrue(results.get(0).containsKey("Total Paid"));
      assertTrue(results.get(1).containsKey("Total Paid"));
      assertTrue(results.get(2).containsKey("Total Paid"));
      assertEquals("Record1", results.get(0).get("Name"));
      assertEquals("Record2", results.get(1).get("Name"));
      assertEquals("Record3", results.get(2).get("Name"));
      assertEquals("Sep 15, 2012", results.get(0).get("Order Date"));
      assertEquals("Sep 16, 2012", results.get(1).get("Order Date"));
      assertEquals("Sep 17, 2012", results.get(2).get("Order Date"));
      assertNull(results.get(0).get("Date Attending"));
      assertNull(results.get(1).get("Date Attending"));
      assertNull(results.get(2).get("Date Attending"));
      assertEquals("0", results.get(0).get("Total Paid"));
      assertEquals("0", results.get(1).get("Total Paid"));
      assertEquals("0", results.get(2).get("Total Paid"));
    }
  }
}