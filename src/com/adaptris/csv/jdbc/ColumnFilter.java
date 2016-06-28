package com.adaptris.csv.jdbc;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;

public interface ColumnFilter {
  public List<String> getFilteredColumnNames(AdaptrisMessage msg);
}
