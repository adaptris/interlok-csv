package com.adaptris.csv;

import jakarta.validation.constraints.NotNull;

import org.supercsv.prefs.CsvPreference;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link PreferenceBuilder} that maps the standard super-csv formats.
 * 
 * 
 * 
 * @config csv-basic-preference-builder
 * @author lchan
 * 
 */
@XStreamAlias("csv-basic-preference-builder")
public class BasicPreferenceBuilder implements PreferenceBuilder {

  /**
   * enum representing the standard supported CSV Formats.
   * 
   */
  public enum Style {

    STANDARD_PREFERENCE {
      @Override
      CsvPreference createFormat() {
        return CsvPreference.STANDARD_PREFERENCE;
      }
    },

    EXCEL_PREFERENCE {
      @Override
      CsvPreference createFormat() {
        return CsvPreference.EXCEL_PREFERENCE;
      }
    },
    EXCEL_NORTH_EUROPE_PREFERENCE {
      @Override
      CsvPreference createFormat() {
        return CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
      }
    },
    TAB_PREFERENCE {
      @Override
      CsvPreference createFormat() {
        return CsvPreference.TAB_PREFERENCE;
      }
    };

    abstract CsvPreference createFormat();
  };

  @NotNull
  @AutoPopulated
  private Style style;

  public BasicPreferenceBuilder() {
    this(Style.STANDARD_PREFERENCE);
  }

  public BasicPreferenceBuilder(Style style) {
    setStyle(style);
  }

  public Style getStyle() {
    return style;
  }

  public void setStyle(Style csvFormat) {
    this.style = Args.notNull(csvFormat, "style");
  }

  @Override
  public CsvPreference build() {
    return getStyle().createFormat();
  }

}
