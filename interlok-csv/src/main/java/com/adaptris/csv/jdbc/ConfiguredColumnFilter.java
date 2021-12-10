package com.adaptris.csv.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link ColumnFilter} implementation that allows you to filter columns based on configuration.
 * 
 * @config csv-configured-column-filter
 */
@XStreamAlias("csv-configured-column-filter")
@ComponentProfile(summary = "Filter CSV column output based on configuration", since = "3.8.0")
@DisplayOrder(order = {"includeColumns", "excludeColumns"})
public class ConfiguredColumnFilter implements ColumnFilter {

  @XStreamImplicit(itemFieldName = "exclude-column")
  @NotNull
  @AutoPopulated
  private List<String> excludeColumns;

  @XStreamImplicit(itemFieldName = "include-column")
  @NotNull
  @AutoPopulated
  private List<String> includeColumns;


  public ConfiguredColumnFilter() {
    setExcludeColumns(new ArrayList<String>());
    setIncludeColumns(new ArrayList<String>());
  }
  
  public List<String> getExcludeColumns() {
    return excludeColumns;
  }

  /**
   * Specify the list of columns to explicitly exclude
   * 
   * @param c list of column names to filter; if not configured, then all columns are included.
   */
  public void setExcludeColumns(List<String> c) {
    this.excludeColumns = Args.notNull(c, "excludeColumns");
  }

  @Override
  public Set<String> getExcludeColumnNames(AdaptrisMessage msg) {
    // Switch to using a Treeset with case insenstivie because then
    // set.add("ABCD");
    // set.contains("abcd") == true;
    Set<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    result.addAll(getExcludeColumns());
    return result;
  }

  @Override
  public Set<String> getIncludeColumnNames(AdaptrisMessage msg) {
    Set<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    result.addAll(getIncludeColumns());
    return result;
  }

  /**
   * @return the includeColumns
   */
  public List<String> getIncludeColumns() {
    return includeColumns;
  }

  /**
   * Specify the list of columns to explicitly include
   * 
   * @param c the list of columsn to explicitly include; if not configured, then all columns are included.
   */
  public void setIncludeColumns(List<String> c) {
    this.includeColumns = Args.notNull(c, "includeColumns");
  }

}
