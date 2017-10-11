package com.adaptris.csv.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.supercsv.prefs.CsvPreference;

public class CustomPreferenceBuilderTest {

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testDelimiter() throws Exception {
    CustomPreferenceBuilder builder = new CustomPreferenceBuilder();
    assertNull(builder.getDelimiter());
    assertEquals(',', builder.delimiter());
    builder.setDelimiter('\t');
    assertEquals(Character.valueOf('\t'), builder.getDelimiter());
    assertEquals('\t', builder.delimiter());
    builder.setDelimiter(null);
    assertNull(builder.getDelimiter());
    assertEquals(',', builder.delimiter());
    // Test might fail if they change the API (yet again)
    assertNotNull(builder.build());
    CsvPreference format = builder.build();
    assertEquals(',', format.getDelimiterChar());
  }

  @Test
  public void testQuoteChar() throws Exception {
    CustomPreferenceBuilder builder = new CustomPreferenceBuilder();
    assertNull(builder.getQuoteChar());
    assertEquals('"', builder.quoteChar());
    builder.setQuoteChar('a');
    assertEquals(Character.valueOf('a'), builder.getQuoteChar());
    assertNotNull(builder.build());
  }

  @Test
  public void testIgnoreEmptyLines() throws Exception {
    CustomPreferenceBuilder builder = new CustomPreferenceBuilder();
    assertNull(builder.getIgnoreEmptyLines());
    assertFalse(builder.ignoreEmptyLines());

    builder.setIgnoreEmptyLines(Boolean.FALSE);
    assertEquals(Boolean.FALSE, builder.getIgnoreEmptyLines());
    assertFalse(builder.ignoreEmptyLines());

    builder.setIgnoreEmptyLines(null);
    assertNull(builder.getIgnoreEmptyLines());
    assertFalse(builder.ignoreEmptyLines());

    assertNotNull(builder.build());
  }


  @Test
  public void testRecordSeparator() throws Exception {
    CustomPreferenceBuilder builder = new CustomPreferenceBuilder();
    assertNull(builder.getRecordSeparator());
    assertEquals("\r\n", builder.recordSeparator());
    builder.setRecordSeparator("\n");
    assertEquals("\n", builder.getRecordSeparator());
    assertEquals("\n", builder.recordSeparator());

    builder.setRecordSeparator(null);
    assertNull(builder.getRecordSeparator());
    assertEquals("\r\n", builder.recordSeparator());

    assertNotNull(builder.build());
  }

}
