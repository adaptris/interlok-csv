package com.adaptris.csv.jdbc;

import org.supercsv.prefs.CsvPreference;

/**
 * Builder for creating the required format for building the CSV file.
 * 
 * @deprecated since 3.6.6 use {@link com.adaptris.csv.PreferenceBuilder} instead.
 */
@Deprecated
public interface PreferenceBuilder {

  /**
   * Create the CSVFormat.
   * 
   * @return the CSV Format.
   */
  CsvPreference build();
}
