package com.adaptris.core.transform.csvjson;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.csv.PreferenceBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Transform a CSV document into a JSON object of some description.
 *
 * <p>
 * Takes a CSV document and renders as it as JSON, either a standard JSON Array or a <a href="http://jsonlines.org">jsonlines</a>.
 * </p>
 * <p>
 * If our input document is
 *
 * <pre>
 * {@code
 * firstname, lastname, dob
 * "alice","smith", "2017-01-01"
 * "bob", "smith", "2017-01-02"
 * "carol","smith", "2017-01-03"
 * }
 * </pre>
 *
 * then selecting JSON_ARRAY will effectively render as
 *
 * <pre>
 * {@code
 * [
 *   { "firstname":"alice", "lastname":"smith", "dob":"2017-01-01" },
 *   { "firstname":"bob", "lastname":"smith", "dob":"2017-01-02" },
 *   { "firstname":"carol", "lastname":"smith", "dob":"2017-01-03" }
 * ]}
 * </pre>
 *
 * Selecting JSON_LINES will effectively render as
 *
 * <pre>
 * {@code
 * { "firstname":"alice", "lastname":"smith", "dob":"2017-01-01" }
 * { "firstname":"bob", "lastname":"smith", "dob":"2017-01-02" }
 * { "firstname":"carol", "lastname":"smith", "dob":"2017-01-03" }
 * }
 * </pre>
 * </p>
 *
 * @config csv-to-json
 */
@AdapterComponent
@ComponentProfile(summary = "Transfrom CSV into JSON", tag = "service,csv,json")
@XStreamAlias("csv-to-json")
@NoArgsConstructor
public class CSVToJson extends CSVConverter {

  public enum OutputStyle {
    JSON_ARRAY {
      @Override
      public CsvJsonTransformer build(PreferenceBuilder b) {
        return new CsvJsonArrayTransformer(b);
      }
    },
    JSON_LINES {
      @Override
      public CsvJsonTransformer build(PreferenceBuilder b) {
        return new CsvJsonLinesTransformer(b);
      }

    };

    public abstract CsvJsonTransformer build(PreferenceBuilder b);
  }

  @Getter(AccessLevel.PRIVATE)
  @Setter(AccessLevel.PRIVATE)
  private transient CsvJsonTransformer transformer;

  /**
   * The JSON output style.
   * <p>
   * The default if not specified is {@code JSON_ARRAY} for backwards compatibility reasons.
   * </p>
   */
  @InputFieldDefault(value = "JSON_ARRAY")
  @Getter
  @Setter
  private OutputStyle style;

  @Override
  public void prepare() throws CoreException {
    super.prepare();
    setTransformer(style().build(getPreferenceBuilder()));
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
    getTransformer().transform(msg);
  }

  private OutputStyle style() {
    return ObjectUtils.defaultIfNull(getStyle(), OutputStyle.JSON_ARRAY);
  }

  public CSVToJson withStyle(OutputStyle s) {
    setStyle(s);
    return this;
  }

}
