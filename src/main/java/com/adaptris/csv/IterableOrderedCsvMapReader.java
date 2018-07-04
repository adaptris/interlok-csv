package com.adaptris.csv;

import com.adaptris.core.util.Args;
import com.adaptris.core.util.CloseableIterable;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

/**
 * @author mwarman
 */
public class IterableOrderedCsvMapReader extends OrderedCsvMapReader implements CloseableIterable<Map<String, String>>, Iterator<Map<String, String>> {

  private boolean firstLineCheck = true;
  private String[] headers;
  private Map<String, String> nextLine;

  public IterableOrderedCsvMapReader(Reader reader, CsvPreference preferences) {
    super(reader, preferences);
  }

  @Override
  public Iterator<Map<String, String>> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    try {
      if(firstLineCheck && headers == null){
        headers = this.getHeader(true);
        firstLineCheck = false;
      }
      if (nextLine == null) {
        nextLine = this.read(headers);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not read line", e);
    }
    return nextLine != null;
  }

  @Override
  public Map<String, String> next() {
    Map<String, String> next = nextLine;
    nextLine = null;
    return next;
  }

  public void setHeaders(String[] headers) {
    this.headers = Args.notNull(headers, "headers");
  }
}
