package com.adaptris.csv.aggregator;

import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Attempts to aggregate messages into a CSV file.
 *
 * This aggregator attempts to validate the CSV by making use of the
 * Apache Commons CSV library. If a header is set then it defines how
 * many columns each row should have, otherwise the first record in
 * the first message dictates this.
 *
 * Each record is parsed using the Commons CSV Parser, which will
 * (hopefully) ensure that the record is valid CSV.
 *
 * @author ashley
 */
@XStreamAlias("csv-validating-aggregator")
@ComponentProfile(summary = "Aggregate messages into a CSV, optionally prefixing a header",
    since = "3.10.0",
    tag = "csv,aggregator,validate")
public class CsvValidatingAggregator extends CsvAggregating
{
  @InputFieldHint(friendly = "Ensure number of columns in each row match the header")
  @InputFieldDefault(value = "true")
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean forceColumns;

  @Override
  protected boolean forceColumns() {
    return BooleanUtils.toBooleanDefaultIfNull(getForceColumns(), true);
  }
}
