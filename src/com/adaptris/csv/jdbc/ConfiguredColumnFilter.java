package com.adaptris.csv.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("csv-configured-column-filter")
public class ConfiguredColumnFilter implements ColumnFilter {

  @XStreamImplicit(itemFieldName = "filtered-column")
  private List<String> filteredColumns;

  public ConfiguredColumnFilter() {
    setFilteredColumns(new ArrayList<String>());
  }
  
  public List<String> getFilteredColumns() {
    return filteredColumns;
  }

  /**
   * @param filteredColumns list of column names to filter
   */
  public void setFilteredColumns(List<String> filteredColumns) {
    this.filteredColumns = filteredColumns;
  }

  @Override
  public List<String> getFilteredColumnNames(AdaptrisMessage msg) {
    return getFilteredColumns();
  }

}
