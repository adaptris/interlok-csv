package com.adaptris.core.transform.csvjson;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonStyle;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.BasicPreferenceBuilder.Style;

// Note that using super-csv the standard preference terminates explicitly with \r\n
// However, the line-endings are LF in the test files, which means that a straight comparison .equals()
// won't work!.
// We can compare each line though
@SuppressWarnings("deprecation")
public class JsonToFixedCSVTest extends ServiceCase {
  private static final String CSV_HEADER = "sentence_1,sentence_2,sentence_3,sentence_4,sentence_5,sentence_6,sentence_7";

  private static final String JSON_ARRAY = "array.json";
  private static final String CSV_ARRAY_HEADER = "array-header.csv";
  private static final String CSV_ARRAY = "array.csv";

  private static final String JSON_OBJECT = "object.json";
  private static final String CSV_OBJECT_HEADER = "object-header.csv";
  private static final String CSV_OBJECT = "object.csv";

  private static final String JSON_LINES = "jsonlines.json";
  private static final String CSV_JSON_LINES = "jsonlines-header.csv";

  private static final String JSON_ARRAY_PATH = "array-path.json";
  private static final String JSON_ARRAY_PATH_JSONPATH = "$.sentences";
  private static final String CSV_JSON_ARRAY_PATH = "array-path-header.csv";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }


  @Test
  public void testArrayWithHeader() throws Exception {
    AdaptrisMessage message = getMessage(JSON_ARRAY);
    JsonToFixedCSV service =
        new JsonToFixedCSV(CSV_HEADER).withIncludeHeader(Boolean.TRUE.toString()).withJsonStyle(JsonStyle.JSON_ARRAY)
            .withPreferenceBuilder(new BasicPreferenceBuilder(Style.EXCEL_PREFERENCE));
    execute(service, message);
    Assert.assertEquals(asList(CSV_ARRAY_HEADER), listify(message.getInputStream()));
  }

  /**
   * Test that a JSON array becomes several lines on CSV, and not displaying CSV header column names.
   *
   * @throws Exception
   */
  @Test
  public void testArrayNoHeader() throws Exception {
    AdaptrisMessage message = getMessage(JSON_ARRAY);
    JsonToFixedCSV service =
        new JsonToFixedCSV(CSV_HEADER).withIncludeHeader(Boolean.FALSE.toString()).withJsonStyle(JsonStyle.JSON_ARRAY);

    execute(service, message);
    Assert.assertEquals(asList(CSV_ARRAY), listify(message.getInputStream()));
  }


  /**
   * Test that a JSON object becomes CSV data, and displaying CSV header column names.
   *
   * @throws Exception
   */
  @Test
  public void testObjectWithHeader() throws Exception {
    AdaptrisMessage message = getMessage(JSON_OBJECT);
    JsonToFixedCSV service =
        new JsonToFixedCSV(CSV_HEADER).withIncludeHeader(Boolean.TRUE.toString()).withJsonStyle(JsonStyle.JSON_OBJECT)
            .withPreferenceBuilder(new BasicPreferenceBuilder(Style.EXCEL_PREFERENCE));

    execute(service, message);

    System.err.println(message.getContent());
    Assert.assertEquals(asList(CSV_OBJECT_HEADER), listify(message.getInputStream()));
  }

  /**
   * Test that a JSON object becomes CSV data, and not displaying CSV header column names.
   *
   * @throws Exception
   */
  @Test
  public void testObjectNoHeader() throws Exception {
    AdaptrisMessage message = getMessage(JSON_OBJECT);
    JsonToFixedCSV service =
        new JsonToFixedCSV(CSV_HEADER).withIncludeHeader(Boolean.FALSE.toString()).withJsonStyle(JsonStyle.JSON_OBJECT)
            .withPreferenceBuilder(new BasicPreferenceBuilder(Style.EXCEL_PREFERENCE));

    execute(service, message);

    Assert.assertEquals(asList(CSV_OBJECT), listify(message.getInputStream()));
  }

  @Test
  public void testJsonLines() throws Exception {
    AdaptrisMessage message = getMessage(JSON_LINES);
    JsonToFixedCSV service = new JsonToFixedCSV(CSV_HEADER).withIncludeHeader(Boolean.TRUE.toString())
        .withJsonStyle(JsonStyle.JSON_LINES);
    execute(service, message);
    Assert.assertEquals(asList(CSV_JSON_LINES), listify(message.getInputStream()));
  }

  /**
   * Test that the service behaves as expected if bad JSON is given to it.
   *
   * @throws Exception
   */
  @Test
  public void testNotJson() throws Exception {
    try {
      AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage("This isn't JSON");
      JsonToFixedCSV service = buildService(false, CSV_HEADER);

      execute(service, message);
      fail();
    } catch (CoreException e) {
      /* expected */
    }
  }

  private AdaptrisMessage getMessage(String resource) throws IOException {
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(getResource(resource));
  }

  private String getResource(String resource) throws IOException {
    try (InputStream in = getClass().getResourceAsStream(resource)) {
      return IOUtils.toString(in, StandardCharsets.UTF_8);
    }
  }

  private List<String> asList(String resource) throws IOException {
    return listify(getClass().getResourceAsStream(resource));
  }

  private List<String> listify(InputStream in) throws IOException {
    try (InputStream tryIn = in) {
      return IOUtils.readLines(tryIn, StandardCharsets.UTF_8);
    }
  }

  private JsonToFixedCSV buildService(boolean showHeader, String header) {
    JsonToFixedCSV service = new JsonToFixedCSV();
    service.setIncludeHeader(String.valueOf(showHeader));
    service.setCsvHeader(header);
    return service;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new JsonToFixedCSV("field1,field2");
  }
}
