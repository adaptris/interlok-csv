package com.adaptris.core.transform.csv;

import java.lang.reflect.Method;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.BooleanUtils;
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

  private transient Logger log = LoggerFactory.getLogger(this.getClass());
  private static final Character COMMA = Character.valueOf(',');
  private static final String DEFAULT_RECORD_SEPARATOR = "\r\n";

  private Character delimiter;
  private Character commentStart;
  private Character escape;
  private Character quoteChar;
  private Boolean ignoreEmptyLines;
  private Boolean ignoreSurroundingSpaces;
  private String recordSeparator;
  
  // These are to protect us mostly from API changes in the nightly build
  // We call the appropriate methods reflectively.
  private static final String[] COMMENT_MARKER_METHODS = {
    "withCommentMarker", 
    "withCommentStart"
  };

  private static final String[] QUOTE_CHAR_METHODS =
  {
      "withQuoteChar", "withQuote"
  };

  private static final String[] ESCAPE_CHAR_METHODS =
  {
      "withEscape", "withEscapeChar"
  };

  private static final String[] IGNORE_EMPTY_LINES_METHODS =
  {
    "withIgnoreEmptyLines"
  };

  private static final String[] IGNORE_SURROUNDING_LINES_METHODS =
  {
    "withIgnoreSurroundingSpaces"
  };

  private static final String[] RECORD_SEPARATOR_METHODS =
  {
    "withRecordSeparator"
  };

  private enum FormatOptions {
    COMMENT_MARKER {
      @Override
      public CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return configureCSV(current, COMMENT_MARKER_METHODS, Character.class, this.name(), config.getCommentStart());
      }
    },
    ESCAPE_CHARACTER {
      @Override
      public CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return configureCSV(current, ESCAPE_CHAR_METHODS, Character.class, this.name(), config.getEscape());
      }
    },
    QUOTE_CHARACTER {
      @Override
      public CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return configureCSV(current, QUOTE_CHAR_METHODS, Character.class, this.name(), config.getQuoteChar());
      }

    },
    IGNORE_EMPTY_LINES {
      @Override
      public CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return configureCSV(current, IGNORE_EMPTY_LINES_METHODS, boolean.class, this.name(), config.ignoreEmptyLines());
      }

    },
    IGNORE_SURROUNDING_SPACES {
      @Override
      public CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return configureCSV(current, IGNORE_SURROUNDING_LINES_METHODS, boolean.class, this.name(), config.ignoreSurroundingSpaces());
      }

    },
    RECORD_SEPARATOR {
      @Override
      public CSVFormat create(CustomFormatBuilder config, CSVFormat current) {
        return configureCSV(current, RECORD_SEPARATOR_METHODS, String.class, this.name(), config.recordSeparator());
      }

    };
    public abstract CSVFormat create(CustomFormatBuilder config, CSVFormat current);
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
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreEmptyLines(), false);
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
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreSurroundingSpaces(), false);
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

  protected static CSVFormat configureCSV(CSVFormat obj, String[] candidates, Class type, String optionName, Object value) {
    CSVFormat result = obj;
    try {
      Method m = findMethod(CSVFormat.class, candidates, type);
      result = (CSVFormat) m.invoke(obj, value);
    }
    catch (Exception e) {
      throw new UnsupportedOperationException("Cannot find appropriate method to handle " + optionName);
    }
    return result;
  }

  private static Method findMethod(Class c, String[] candidates, Class type) {
    Method result = null;
    for (String m : candidates) {
      try {
        result = c.getMethod(m, type);
        break;
      }
      catch (NoSuchMethodException e) {
      }
    }

    return result;
  }
}
