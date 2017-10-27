package com.adaptris.csv.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.prefs.CsvPreference;

/**
 * Implementation of {@link PreferenceBuilder} that allows for custom csv formats.
 * 
 * @deprecated since 3.6.6 use {@link com.adaptris.csv.CustomPreferenceBuilder} instead
 */
@Deprecated
public class CustomPreferenceBuilder extends com.adaptris.csv.CustomPreferenceBuilder implements PreferenceBuilder {
  private Logger log = LoggerFactory.getLogger(this.getClass());


  @Override
  public CsvPreference build() {
    log.warn("{} is deprecated; use {} instead", this.getClass().getName(),
        com.adaptris.csv.CustomPreferenceBuilder.class.getName());
    return super.build();
  }

}
