package com.adaptris.core.transform.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CustomFormatBuilderTest {

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testDelimiter() throws Exception {
    CustomFormatBuilder builder = new CustomFormatBuilder();
    assertNull(builder.getDelimiter());
    assertEquals(Character.valueOf(','), builder.delimiter());
    builder.setDelimiter('\t');
    assertEquals(Character.valueOf('\t'), builder.getDelimiter());
    assertEquals(Character.valueOf('\t'), builder.delimiter());
    builder.setDelimiter(null);
    assertNull(builder.getDelimiter());
    assertEquals(Character.valueOf(','), builder.delimiter());
    assertNotNull(builder.createFormat());
  }

  @Test
  public void testCommentStart() throws Exception {
    CustomFormatBuilder builder = new CustomFormatBuilder();
    assertNull(builder.getCommentStart());
    builder.setCommentStart('a');
    assertEquals(Character.valueOf('a'), builder.getCommentStart());
    assertNotNull(builder.createFormat());
  }

  @Test
  public void testEscape() throws Exception {
    CustomFormatBuilder builder = new CustomFormatBuilder();
    assertNull(builder.getEscape());
    builder.setEscape('a');
    assertEquals(Character.valueOf('a'), builder.getEscape());
    assertNotNull(builder.createFormat());
  }

  @Test
  public void testQuoteChar() throws Exception {
    CustomFormatBuilder builder = new CustomFormatBuilder();
    assertNull(builder.getQuoteChar());
    builder.setQuoteChar('a');
    assertEquals(Character.valueOf('a'), builder.getQuoteChar());
    assertNotNull(builder.createFormat());
  }

  @Test
  public void testIgnoreEmptyLines() throws Exception {
    CustomFormatBuilder builder = new CustomFormatBuilder();
    assertNull(builder.getIgnoreEmptyLines());
    assertFalse(builder.ignoreEmptyLines());
    
    builder.setIgnoreEmptyLines(Boolean.FALSE);
    assertEquals(Boolean.FALSE, builder.getIgnoreEmptyLines());
    assertFalse(builder.ignoreEmptyLines());
    
    builder.setIgnoreEmptyLines(null);
    assertNull(builder.getIgnoreEmptyLines());
    assertFalse(builder.ignoreEmptyLines());

    assertNotNull(builder.createFormat());
  }

  @Test
  public void testIgnoreSurroundingSpaces() throws Exception {
    CustomFormatBuilder builder = new CustomFormatBuilder();
    assertNull(builder.getIgnoreSurroundingSpaces());
    assertFalse(builder.ignoreSurroundingSpaces());

    builder.setIgnoreSurroundingSpaces(Boolean.FALSE);
    assertEquals(Boolean.FALSE, builder.getIgnoreSurroundingSpaces());
    assertFalse(builder.ignoreSurroundingSpaces());

    builder.setIgnoreSurroundingSpaces(null);
    assertNull(builder.getIgnoreSurroundingSpaces());
    assertFalse(builder.ignoreSurroundingSpaces());

    assertNotNull(builder.createFormat());
  }

  @Test
  public void testRecordSeparator() throws Exception {
    CustomFormatBuilder builder = new CustomFormatBuilder();
    assertNull(builder.getRecordSeparator());
    assertEquals("\r\n", builder.recordSeparator());
    builder.setRecordSeparator("\n");
    assertEquals("\n", builder.getRecordSeparator());
    assertEquals("\n", builder.recordSeparator());

    builder.setRecordSeparator(null);
    assertNull(builder.getRecordSeparator());
    assertEquals("\r\n", builder.recordSeparator());

    assertNotNull(builder.createFormat());
  }

}
