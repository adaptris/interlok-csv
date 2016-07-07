package com.adaptris.csv.jdbc;

import java.util.Set;

import com.adaptris.core.AdaptrisMessage;

/**
 * A simple column filter for CSV output.
 * 
 * @author lchan
 *
 */
public interface ColumnFilter {
  /**
   * The list of columns that will be filtered.
   * 
   */
  public Set<String> getExcludeColumnNames(AdaptrisMessage msg);

  /**
   * The list of columsn that will be explicitly included.
   * 
   */
  public Set<String> getIncludeColumnNames(AdaptrisMessage msg);

}
