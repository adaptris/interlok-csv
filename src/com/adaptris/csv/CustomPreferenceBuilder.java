package com.adaptris.csv;

import org.supercsv.prefs.CsvPreference;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link PreferenceBuilder} that allows for custom csv formats.
 * 
 * 
 * 
 * @config csv-custom-preference-builder
 * @author lchan
 * 
 */
@XStreamAlias("csv-custom-preference-builder")
public class CustomPreferenceBuilder implements PreferenceBuilder {

  private static final String DEFAULT_RECORD_SEPARATOR = "\r\n";

  private Character delimiter;
  private Character quoteChar;
  private Boolean ignoreEmptyLines;
  private String recordSeparator;
  
  
  public Character getDelimiter() {
    return delimiter;
  }

  /**
   * Set the delimiter for the CSV file.
   * 
   * @param d the delimiter; if not specified, defaults to {@code ,}
   */
  public void setDelimiter(Character d) {
    this.delimiter = d;
  }

  char delimiter() {
    return getDelimiter() != null ? getDelimiter().charValue() : ',';
  }

  public Character getQuoteChar() {
    return quoteChar;
  }

  /**
   * Set the quote character for the CSV file.
   * 
   * @param d the delimiter; if not specified, defaults to {@code "}
   */
  public void setQuoteChar(Character quoteChar) {
    this.quoteChar = quoteChar;
  }

  char quoteChar() {
    return getQuoteChar() != null ? getQuoteChar().charValue() : '"';
  }

  public Boolean getIgnoreEmptyLines() {
    return ignoreEmptyLines;
  }

  /**
   * Specify whether or not to ignore empty lines.
   * 
   * @param b true or false, if not specified false.
   */
  public void setIgnoreEmptyLines(Boolean b) {
    this.ignoreEmptyLines = b;
  }

  boolean ignoreEmptyLines() {
    return getIgnoreEmptyLines() != null ? getIgnoreEmptyLines().booleanValue() : false;
  }

  public String getRecordSeparator() {
    return recordSeparator;
  }

  /**
   * Set the record separactor
   * 
   * @param sep the record separator; if not specified defaults to "\r\n"
   */
  public void setRecordSeparator(String sep) {
    this.recordSeparator = sep;
  }

  String recordSeparator() {
    return getRecordSeparator() != null ? getRecordSeparator() : DEFAULT_RECORD_SEPARATOR;
  }

  @Override
  public CsvPreference build() {
    return new CsvPreference.Builder(quoteChar(), delimiter(), recordSeparator()).ignoreEmptyLines(ignoreEmptyLines())
        .build();
  }

}
