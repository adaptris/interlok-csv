package com.adaptris.core.transform.csvjson;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonObjectProvider;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonStyle;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.util.CloseableIterable;
import com.adaptris.validation.constraints.BooleanExpression;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.ArrayList;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.supercsv.io.CsvListWriter;


/**
 * Transfrom a JSON array into a CSV.
 *
 * <p>
 * Takes a JSON array and renders it as a CSV document:
 * <pre>
 * {@code
 * [
 *   { "firstname":"alice", "lastname":"smith", "dob":"2017-01-01" },
 *   { "firstname":"bob", "lastname":"smith", "dob":"2017-01-02" },
 *   { "firstname":"carol", "lastname":"smith", "dob":"2017-01-03" }
 * ]}
 * </pre>
 * will effectively render as
 * <pre>
 * {@code
 * firstname, lastname, dob
 * "alice","smith", "2017-01-01"
 * "bob", "smith", "2017-01-02"
 * "carol","smith", "2017-01-03"
 * }
 * </pre>
 * </p>
 * <p>
 * nested JSON objects will be rendered as strings before being passed into the appropriate statement; so
 * {@code { "firstname":"alice", "lastname":"smith", "address": { "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}}} still
 * only be 3 CSV columns the address column will be {@code "{ "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}"}
 * </p>
 *
 * </p>
 *
 * @config json-to-csv
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Transfrom a JSON Array/JSON Lines document into a CSV", tag = "service,csv,json")
@XStreamAlias("json-to-csv")
@DisplayOrder(order = {"includeHeader", "jsonStyle", "preferenceBuilder"})
@NoArgsConstructor
public class JsonArrayToCSV extends CSVConverter {

  /**
   * Whether or not to emit a header line.
   *
   */
  @AdvancedConfig
  @InputFieldHint(expression = true)
  @InputFieldDefault(value = "true")
  @BooleanExpression
  @Getter
  @Setter
  private String includeHeader;
  /**
   * Specify how the payload is parsed to provide JSON objects.
   *
   * <p>
   * The default if not specified is a {@code JSON_ARRAY}
   * </p>
   *
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "JSON_ARRAY")
  private JsonStyle jsonStyle;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      boolean first = true;
      try (CsvListWriter csvWriter = new CsvListWriter(msg.getWriter(), getPreferenceBuilder().build());
          CloseableIterable<AdaptrisMessage> splitMsgs = CloseableIterable.ensureCloseable(jsonStyle().createIterator(msg))) {
        for (AdaptrisMessage m : splitMsgs) {
          Map<String, String> json = JsonUtil.mapifyJson(m);
          if (first && includeHeader(msg)) {
            csvWriter.writeHeader(json.keySet().toArray(new String[0]));
            first = false;
          }
          csvWriter.write(new ArrayList<>(json.values()));
        }
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected boolean includeHeader(AdaptrisMessage msg) {
    return getIncludeHeader() == null || Boolean.parseBoolean(msg.resolve(getIncludeHeader()));
  }

  protected JsonObjectProvider jsonStyle() {
    return ObjectUtils.defaultIfNull(getJsonStyle(), JsonStyle.JSON_ARRAY);
  }

  @SuppressWarnings("unchecked")
  public <T extends JsonArrayToCSV> T withJsonStyle(JsonStyle p) {
    setJsonStyle(p);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public <T extends JsonArrayToCSV> T withIncludeHeader(String s) {
    setIncludeHeader(s);
    return (T) this;
  }
}
