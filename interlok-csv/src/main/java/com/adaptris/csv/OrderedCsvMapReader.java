package com.adaptris.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.Util;

import com.adaptris.core.util.Args;

/**
 * Extends {@code CsvMapReader} but uses {@link LinkedHashMap} as the underlying map implementation for predictable iteration order.
 *
 *
 */
public class OrderedCsvMapReader extends CsvMapReader {

  public OrderedCsvMapReader(final Reader reader, final CsvPreference preferences) {
    super(reader, preferences);
  }

  @Override
  public Map<String, String> read(final String... nameMapping) throws IOException {
    Args.notNull(nameMapping, "mappings");
    if (readRow()) {
      final Map<String, String> destination = new LinkedHashMap<>(nameMapping.length);
      Util.filterListToMap(destination, nameMapping, getColumns());
      return destination;
    }
    return null; // EOF
  }

  /**
   * Read the next row without thinking about nameMappings
   *
   * @return the next row of data.
   */
  public List<String> readNext() throws IOException {
    if (readRow()) {
      return getColumns();
    }
    return null; // EOF
  }

}
