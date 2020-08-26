package com.adaptris.core.transform.csv;

import org.apache.commons.csv.CSVFormat;
import com.adaptris.annotation.Removal;

/**
 * Builder for creating the required format for parsing the CSV file.
 *
 * @deprecated since 3.11.0 : switch to using net.supercsv based implementations instead
 */
@Deprecated
@Removal(version = "4.0.0", message = "Switch to using net.supercsv based implementations instead")
public interface FormatBuilder {

  /**
   * Create the CSVFormat.
   *
   * @return the CSV Format.
   */
  CSVFormat createFormat();
}
