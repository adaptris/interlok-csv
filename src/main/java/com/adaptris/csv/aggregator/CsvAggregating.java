package com.adaptris.csv.aggregator;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregatorImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.BasicPreferenceBuilder.Style;
import com.adaptris.csv.OrderedCsvMapReader;
import com.adaptris.csv.PreferenceBuilder;
import lombok.Getter;
import lombok.Setter;

public abstract class CsvAggregating extends MessageAggregatorImpl
{

  @InputFieldHint(expression = true)
  @Getter
  @Setter
  private String header;
  /**
   * How to write the CSV file.
   *
   */
  @Valid
  @Getter
  @Setter
  @InputFieldDefault(value = "csv-basic-preference-builder")
  private PreferenceBuilder preferenceBuilder;

  @Override
  public void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs)
      throws CoreException {
    aggregate(msg, msgs);
  }

  @Override
  public void aggregate(AdaptrisMessage original, Iterable<AdaptrisMessage> msgs)
      throws CoreException {
    String resolvedHeader = original.resolve(getHeader());
    CsvPreference prefs = buildPreferences();

    try (CsvListWriter csvWriter = new CsvListWriter(original.getWriter(), prefs)) {
      int columnCount = writeHeaders(csvWriter, resolvedHeader, prefs);
      for (AdaptrisMessage m : msgs) {
        if (filter(m)) {
          try (Reader in = m.getReader();
              OrderedCsvMapReader csvReader = new OrderedCsvMapReader(in, buildPreferences())) {
            for (List<String> record; (record = csvReader.readNext()) != null;) {
              assertColumnCount(columnCount, record);
              csvWriter.write(record);
            }
          }
          overwriteMetadata(m, original);
        }
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  protected abstract boolean forceColumns();

  private int writeHeaders(CsvListWriter csvWriter, String resolvedHeader, CsvPreference prefs)
      throws Exception {
    int columnCount = -1;
    if (!StringUtils.isEmpty(resolvedHeader)) {
      List<List<String>> headers = buildHeader(resolvedHeader, prefs);
      boolean first = true;
      for (List<String> h : headers) {
        if (first) {
          columnCount = h.size();
          first = false;
        }
        assertColumnCount(columnCount, h);
        csvWriter.write(h);
      }
    }
    return columnCount;
  }

  private void assertColumnCount(int expected, List columns) throws Exception {
    if (BooleanUtils.and(new boolean[] {forceColumns(), expected != -1})) {
      if (columns.size() != expected) {
        throw new Exception("CSV Column count differs from header column count");
      }
    }
  }

  private CsvPreference buildPreferences() {
    return ObjectUtils.defaultIfNull(getPreferenceBuilder(),
        new BasicPreferenceBuilder(Style.STANDARD_PREFERENCE)).build();
  }

  // Need to include support for "multi-line headers" which is what was supported originally.
  // I don't understand why?
  private List<List<String>> buildHeader(String s, CsvPreference prefs) throws Exception {
    List<List<String>> result = new ArrayList<>();
    try (StringReader in = new StringReader(s);
        OrderedCsvMapReader csvReader = new OrderedCsvMapReader(in, prefs);) {
      for (List<String> record; (record = csvReader.readNext()) != null;) {
        result.add(new ArrayList<String>(record));
      }
    }
    return result;
  }

  public <T extends CsvAggregating> T withHeader(String s) {
    setHeader(s);
    return (T) this;
  }
}
