package com.adaptris.csv;

import org.supercsv.prefs.CsvPreference;

/**
 * Builder for creating the required format for building the CSV file.
 *
 * @author lchan
 *
 */
public interface PreferenceBuilder {

  /**
   * Create the CSVFormat.
   *
   * @return the CSV Format.
   */
  CsvPreference build();

}
