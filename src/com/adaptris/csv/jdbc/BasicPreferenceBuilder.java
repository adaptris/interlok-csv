package com.adaptris.csv.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.prefs.CsvPreference;

/**
 * Implementation of {@link PreferenceBuilder} that maps the standard super-csv formats.
 * 
 * @deprecated since 3.6.6 use {@link com.adaptris.csv.BasicPreferenceBuilder} instead
 */
@Deprecated
public class BasicPreferenceBuilder extends com.adaptris.csv.BasicPreferenceBuilder implements PreferenceBuilder {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  public BasicPreferenceBuilder() {
    super();
  }

  @Override
  public CsvPreference build() {
    log.warn("{} is deprecated; use {} instead", this.getClass().getName(),
        com.adaptris.csv.BasicPreferenceBuilder.class.getName());
    return super.build();
  }

}
