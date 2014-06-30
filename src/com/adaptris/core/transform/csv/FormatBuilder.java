package com.adaptris.core.transform.csv;

import org.apache.commons.csv.CSVFormat;

/**
 * Builder for creating the required format for parsing the CSV file.
 * 
 * @author lchan
 * 
 */
public interface FormatBuilder {

  /**
   * Create the CSVFormat.
   * 
   * @return the CSV Format.
   */
  CSVFormat createFormat();
}
