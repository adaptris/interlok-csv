package com.adaptris.core.transform.csvjson;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonObjectProvider;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonStyle;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.util.CloseableIterable;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.supercsv.io.CsvListWriter;

/**
 * Transfrom a JSON array into a CSV with only the specified fields.
 *
 * @config json-to-fixed-csv-service
 *
 */
@XStreamAlias("json-to-fixed-csv-service")
@AdapterComponent
@ComponentProfile(summary = "Transform a JSON document to CSV with fixed fields", tag = "service,transform,json,csv")
@DisplayOrder(order = {"csvHeader", "includeHeader", "jsonStyle", "preferenceBuilder"})
@NoArgsConstructor
public class JsonToFixedCSV extends JsonArrayToCSV {

  /**
   * The CSV Columns to emit.
   * <p>A comma separated list of keys in the JSON object that will be used to grab data for each CSV column. This
   * can be an expression to resolve that list from metadata.
   * </p>
   */
  @NotBlank(message="CSV Header may not be blank")
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  private String csvHeader = "";

  public JsonToFixedCSV(String hdrs) {
    this();
    setCsvHeader(hdrs);
  }

  @Override
  protected boolean includeHeader(AdaptrisMessage msg) {
    return super.includeHeader(msg);
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {

    try {
      boolean includeHeaders = includeHeader(msg);
      log.trace("Starting JSON to CSV transformation with" + (includeHeaders ? "" : "out") + " header");
      List<String> headers = header(msg);
      JsonObjectProvider style = jsonStyle();
      try (CsvListWriter csvWriter = new CsvListWriter(msg.getWriter(), getPreferenceBuilder().build());
          CloseableIterable<AdaptrisMessage> splitMsgs = CloseableIterable.ensureCloseable(style.createIterator(msg))) {
        if (includeHeaders) {
          csvWriter.writeHeader(headers.toArray(new String[0]));
        }

        // If it's a JSON Object split then effectively each split message is part of the same "row"
        // so it's a single row output + a possible header.
        if (style == JsonStyle.JSON_OBJECT) {
          Map<String, String> jMap = new HashMap<>();
          for (AdaptrisMessage splitMessage : splitMsgs) {
            Map<String, String> map = JsonUtil.mapifyJson(splitMessage);
            jMap.putAll(map);
          }
          csvWriter.write(toRecord(jMap, headers));
        } else {
          for (AdaptrisMessage splitMessage : splitMsgs) {
            Map<String, String> jMap = JsonUtil.mapifyJson(splitMessage);
            log.trace("JSON object has " + jMap.size() + " keys");
            csvWriter.write(toRecord(jMap, headers));
          }
        }
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException("Could not parse JSON nor generate CSV data", e);
    }
  }


  /**
   * Grabs the values from a map, and makes sure it's in the same order as the headers.
   *
   * @return The CSV row as a list of Strings.
   */
  private List<String> toRecord(Map<String, String> jMap, List<String> headers) {
    List<String> record = new ArrayList<>();
    for (String header : headers) {
      // either write the value or ""
      record.add(jMap.getOrDefault(header, ""));
    }
    return record;
  }

  /**
   * Simple helper method, as the CSV header is better suited to being a list in most situations.
   *
   * @return A list of the header columns.
   */
  private List<String> header(AdaptrisMessage msg) {
    String hdr = msg.resolve(getCsvHeader());
    return new ArrayList<>(Arrays.asList(hdr.trim().split(",")));
  }

  @Override
  public void prepare() throws CoreException {
    super.prepare();
    Args.notBlank(getCsvHeader(), "csv-header");
  }

}
