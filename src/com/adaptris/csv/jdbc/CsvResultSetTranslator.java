package com.adaptris.csv.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.supercsv.io.CsvListWriter;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.services.jdbc.ResultSetTranslatorImp;
import com.adaptris.core.util.Args;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ResultSetTranslator} that outputs each row as a part of a CSV.
 * 
 *
 */
@XStreamAlias("jdbc-csv-output")
public class CsvResultSetTranslator extends ResultSetTranslatorImp {
  protected static final String ELEMENT_NAME_COLUMN = "column-";

  @NotNull
  @AutoPopulated
  private PreferenceBuilder preferenceBuilder;

  public CsvResultSetTranslator() {
    setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.STANDARD_PREFERENCE));
  }

  @Override
  public void close() {}

  @Override
  public void init() throws CoreException {}

  @Override
  public void start() throws CoreException {}

  @Override
  public void stop() {}

  @Override
  public void prepare() throws CoreException {}

  @Override
  public void translate(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
    boolean first = true;
    try (CsvListWriter csvWriter = new CsvListWriter(target.getWriter(), getPreferenceBuilder().build())) {
      for (JdbcResultSet r : source.getResultSets()) {
        for (JdbcResultRow row : r.getRows()) {
          if (first) {
            csvWriter.writeHeader(createHeaderNames(row).toArray(new String[0]));
            first = false;
          }
          List<String> values = new ArrayList<>();
          for (int i = 0; i < row.getFieldCount(); i++) {
            values.add(toString(row, i));
          }
          // Write each row to the msg.
          csvWriter.write(values);
        }
      }
    } catch (IOException e) {
      throw new ServiceException(e);
    }
  }

  private List<String> createHeaderNames(JdbcResultRow row) throws SQLException {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < row.getFieldCount(); i++) {
      String columnName = row.getFieldName(i);
      String hdrName = getColumnNameStyle().format(StringUtils.defaultIfBlank(columnName, ELEMENT_NAME_COLUMN + (i + 1)));
      result.add(hdrName);
    }
    return result;
  }

  /**
   * @return the formatBuilder
   */
  public PreferenceBuilder getPreferenceBuilder() {
    return preferenceBuilder;
  }

  /**
   * @param formatBuilder the formatBuilder to set
   */
  public void setPreferenceBuilder(PreferenceBuilder formatBuilder) {
    this.preferenceBuilder = Args.notNull(formatBuilder, "preference-builder");
  }


}
