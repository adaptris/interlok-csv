package com.adaptris.csv.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvListWriter;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.ResultSetTranslator;
import com.adaptris.core.services.jdbc.ResultSetTranslatorImp;
import com.adaptris.core.util.Args;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.PreferenceBuilder;
import com.adaptris.jdbc.JdbcResult;
import com.adaptris.jdbc.JdbcResultRow;
import com.adaptris.jdbc.JdbcResultSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ResultSetTranslator} that outputs each row as a part of a CSV.
 * 
 * @config jdbc-csv-output
 */
@XStreamAlias("jdbc-csv-output")
@ComponentProfile(summary = "Output each row in the result set as a line in a CSV", since = "3.8.0")
@DisplayOrder(order = {"preferenceBuilder", "columnNameStyle", "columnTranslators", "columnFilter"})
public class CsvResultSetTranslator extends ResultSetTranslatorImp {
  protected static final String ELEMENT_NAME_COLUMN = "column-";

  @NotNull
  @AutoPopulated
  private PreferenceBuilder preferenceBuilder;
  
  private ColumnFilter columnFilter;

  public CsvResultSetTranslator() {
    setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.STANDARD_PREFERENCE));
  }

  @Override
  public long translateResult(JdbcResult source, AdaptrisMessage target) throws SQLException, ServiceException {
    boolean first = true;
    long resultSetCount = 0;
    Set<Integer> excludedIndexes = new HashSet<>();
    try (CsvListWriter csvWriter = new CsvListWriter(target.getWriter(), getPreferenceBuilder().build())) {
      for (JdbcResultSet r : source.getResultSets()) {
        for (JdbcResultRow row : r.getRows()) {
          if (first) {
            excludedIndexes.addAll(negatedInclusionIndexes(target, row));
            excludedIndexes.addAll(createExcludedIndexes(target, row));
            csvWriter.writeHeader(createHeaderNames(row, excludedIndexes).toArray(new String[0]));
            first = false;
          }
          List<String> values = new ArrayList<>();
          for (int i = 0; i < row.getFieldCount(); i++) {
            if (excludedIndexes.contains(i)) {
              continue;
            }
            values.add(toString(row, i));
          }
          // Write each row to the msg.
          csvWriter.write(values);
          resultSetCount++;
        }
      }
    } catch (IOException e) {
      throw new ServiceException(e);
    }
    return resultSetCount;
  }

  private Collection<Integer> createExcludedIndexes(AdaptrisMessage msg, JdbcResultRow row) {
    if(getColumnFilter() == null) {
      return new ArrayList<Integer>();
    }
    
    final Set<String> excludedColumns = getColumnFilter().getExcludeColumnNames(msg);
    List<Integer> result = new ArrayList<>();
    for(int i = 0; i < row.getFieldCount(); i++) {
      String columnName = row.getFieldName(i);
      if(excludedColumns.contains(columnName)) {
        result.add(i);
      };
    }
    return result;
  }
  
  // Check all the column names, if they don't match the list of columns to include, then add them
  // to the exclusion list.
  // The specia case is if there are no include columns, then it's implicitly include all.
  private Collection<Integer> negatedInclusionIndexes(AdaptrisMessage msg, JdbcResultRow row) {
    if (getColumnFilter() == null) {
      return new ArrayList<Integer>();
    }

    final Set<String> columnsToInclude = getColumnFilter().getIncludeColumnNames(msg);
    if (columnsToInclude.size() == 0) {
      return new ArrayList<Integer>();
    }
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < row.getFieldCount(); i++) {
      String columnName = row.getFieldName(i);
      if (!columnsToInclude.contains(columnName)) {
        result.add(i);
      }
    }
    return result;
  }

  private List<String> createHeaderNames(JdbcResultRow row, Collection<Integer> excludedIndexes) throws SQLException {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < row.getFieldCount(); i++) {
      if (excludedIndexes.contains(i)) {
        continue;
      }
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

  public ColumnFilter getColumnFilter() {
    return columnFilter;
  }

  /**
   * @param columnFilter Optional filter for columns that should not be output
   */
  public void setColumnFilter(ColumnFilter columnFilter) {
    this.columnFilter = columnFilter;
  }

}
