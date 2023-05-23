package com.adaptris.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class OrderedCsvMapReaderTest {
  public static final String LINE_ENDING = System.getProperty("line.separator");

  private static final String CSV_INPUT_FIELD = "Name,Order Date,Date Attending,Total Paid" + LINE_ENDING + "Record1,\"Sep 15, 2012\",,0"
      + LINE_ENDING + "Record2,\"Sep 16, 2012\",,0" + LINE_ENDING + "Record3,\"Sep 17, 2012\",,0" + LINE_ENDING;

  @Test
  public void testRead() throws Exception {
    try (OrderedCsvMapReader csvReader = new OrderedCsvMapReader(new StringReader(CSV_INPUT_FIELD), new BasicPreferenceBuilder().build())) {
      String[] hdrs = csvReader.getHeader(true);
      for (Map<String, String> record; (record = csvReader.read(hdrs)) != null;) {
        assertTrue(record.containsKey("Name"));
        assertTrue(record.containsKey("Order Date"));
        assertTrue(record.containsKey("Date Attending"));
        assertTrue(record.containsKey("Total Paid"));
      }
    }
  }

  @Test
  public void testReadNext() throws Exception {
    try (OrderedCsvMapReader csvReader = new OrderedCsvMapReader(new StringReader(CSV_INPUT_FIELD), new BasicPreferenceBuilder().build())) {
      int recordCount = 0;
      for (; csvReader.readNext() != null;) {
        recordCount++;
      }
      assertEquals(4, recordCount);
    }
  }

}