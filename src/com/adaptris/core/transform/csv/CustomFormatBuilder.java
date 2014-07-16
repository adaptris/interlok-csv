package com.adaptris.core.transform.csv;

import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link FormatBuilder} that allows for custom csv formats.
 * 
 * 
 * 
 * @config csv-custom-format
 * @author lchan
 * 
 */
@XStreamAlias("csv-custom-format")
public class CustomFormatBuilder implements FormatBuilder {

  private Logger log = LoggerFactory.getLogger(this.getClass());
  private static final Character COMMA = Character.valueOf(',');
  private static final String DEFAULT_RECORD_SEPARATOR = "\r\n";

  private Character delimiter;
  private Character commentStart;
  private Character escape;
  private Character quoteChar;
  private Boolean ignoreEmptyLines;
  private Boolean ignoreSurroundingSpaces;
  private String recordSeparator;
  
  private enum FormatOptions {
    COMMENT {
      @Override
      CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return current.withCommentMarker(config.getCommentStart());
      }      
    },
    ESCAPE {
      @Override
      CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return current.withEscape(config.getEscape());
      }
    },
    QUOTE {
      @Override
      CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return current.withQuoteChar(config.getQuoteChar());
      }

    },
    IGNORE_EMPTY {
      @Override
      CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return current.withIgnoreEmptyLines(config.ignoreEmptyLines());
      }

    },
    TRIM {
      @Override
      CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return current.withIgnoreSurroundingSpaces(config.ignoreSurroundingSpaces());
      }

    },
    RECORD_SEPARATOR {
      @Override
      CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return current.withRecordSeparator(config.recordSeparator());
      }

    };
    abstract CSVFormat create(CustomFormatBuilder config, CSVFormat current);
  };
  
  @Override
  public CSVFormat createFormat() {
    CSVFormat format = CSVFormat.newFormat(delimiter().charValue());
    for (FormatOptions b : FormatOptions.values()) {
      format = b.create(this, format);
    }
    log.trace("CVSFormat created : {}", format);
    return format;
  }

  public Character getDelimiter() {
    return delimiter;
  }

  /**
   * Set the delimiter for the CSV file.
   * 
   * @param d the delimiter; if not specified, defaults to ","
   */
  public void setDelimiter(Character d) {
    this.delimiter = d;
  }

  Character delimiter() {
    return getDelimiter() != null ? getDelimiter() : COMMA;
  }

  public Character getCommentStart() {
    return commentStart;
  }

  public void setCommentStart(Character commentStart) {
    this.commentStart = commentStart;
  }

  public Character getEscape() {
    return escape;
  }

  public void setEscape(Character escape) {
    this.escape = escape;
  }

  public Character getQuoteChar() {
    return quoteChar;
  }

  public void setQuoteChar(Character quoteChar) {
    this.quoteChar = quoteChar;
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

  public Boolean getIgnoreSurroundingSpaces() {
    return ignoreSurroundingSpaces;
  }

  /**
   * Specify whether or not to ignore surrounding spaces.
   * 
   * @param b true or false, if not specified false.
   */
  public void setIgnoreSurroundingSpaces(Boolean b) {
    this.ignoreSurroundingSpaces = b;
  }

  boolean ignoreSurroundingSpaces() {
    return getIgnoreSurroundingSpaces() != null ? getIgnoreSurroundingSpaces().booleanValue() : false;
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
}
