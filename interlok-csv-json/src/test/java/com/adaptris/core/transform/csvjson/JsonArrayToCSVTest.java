package com.adaptris.core.transform.csvjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.splitter.json.JsonProvider.JsonStyle;
import com.jayway.jsonpath.ReadContext;

public class JsonArrayToCSVTest extends CsvBaseCase {

  @Test
  public void testService() throws Exception {
    JsonArrayToCSV service = new JsonArrayToCSV();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY_INPUT);
    execute(service, msg);
    System.err.println(msg.getContent());
    try (InputStream in = msg.getInputStream()){
      List<String> lines = IOUtils.readLines(in, Charset.defaultCharset());
      assertEquals(4, lines.size());
    }
  }

  @Test
  public void testServiceIncludeHeader() throws Exception {
    JsonArrayToCSV service = new JsonArrayToCSV().withJsonStyle(JsonStyle.JSON_LINES);
    service.setIncludeHeader("false");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_LINES_INPUT);
    execute(service, msg);
    System.err.println(msg.getContent());
    try (InputStream in = msg.getInputStream()){
      List<String> lines = IOUtils.readLines(in, Charset.defaultCharset());
      assertEquals(3, lines.size());
    }
  }

  @Test
  public void testServiceRoundTrip() throws Exception {
    JsonArrayToCSV s1 = new JsonArrayToCSV();
    CSVToJson s2 = new CSVToJson();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY_INPUT);
    execute(s1, msg);
    execute(s2, msg);
    System.err.println(msg.getContent());
    ReadContext ctx = parse(msg);
    assertEquals("alice", ctx.read("$[0].firstname"));
    assertEquals("bob", ctx.read("$[1].firstname"));
    assertEquals("carol", ctx.read("$[2].firstname"));
  }



  @Test
  public void testBrokenInput() throws Exception {
    JsonArrayToCSV service = createForTests();
    AdaptrisMessage msg = new BrokenMessageFactory(WhenToBreak.INPUT).newMessage(JSON_ARRAY_INPUT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testBrokenOutput() throws Exception {
    JsonArrayToCSV service = createForTests();
    AdaptrisMessage msg = new BrokenMessageFactory(WhenToBreak.OUTPUT).newMessage(JSON_ARRAY_INPUT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }


  @Override
  protected JsonArrayToCSV createForTests() {
    return new JsonArrayToCSV();
  }

}
