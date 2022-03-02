package com.adaptris.core.transform.csvjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.splitter.LineCountSplitter;
import com.adaptris.core.transform.csvjson.CSVToJson.OutputStyle;
import com.adaptris.interlok.util.CloseableIterable;
import com.jayway.jsonpath.ReadContext;

public class CSVToJsonTest extends CsvBaseCase {

  @Test
  public void testService_Lines() throws Exception {
    CSVToJson service = createForTests().withStyle(OutputStyle.JSON_LINES);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    execute(service, msg);
    assertNotNull(msg.getContent());
    LineCountSplitter splitter = new LineCountSplitter(1);
    Collection c = collect(splitter.splitMessage(msg));
    assertEquals(3, c.size());
  }

  @Test
  public void testBrokenInput_Lines() throws Exception {
    CSVToJson service = createForTests().withStyle(OutputStyle.JSON_LINES);
    AdaptrisMessage msg = new BrokenMessageFactory(WhenToBreak.INPUT).newMessage(CSV_INPUT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testService_Array() throws Exception {
    CSVToJson service = createForTests().withStyle(OutputStyle.JSON_ARRAY);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_INPUT);
    execute(service, msg);
    assertNotNull(msg.getContent());
    System.err.println(msg.getContent());
    ReadContext ctx = parse(msg);
    assertEquals("alice", ctx.read("$[0].firstname"));
    assertEquals("bob", ctx.read("$[1].firstname"));
    assertEquals("carol", ctx.read("$[2].firstname"));
  }


  @Test
  public void testBrokenInput_Array() throws Exception {
    CSVToJson service = createForTests().withStyle(OutputStyle.JSON_ARRAY);
    AdaptrisMessage msg = new BrokenMessageFactory(WhenToBreak.INPUT).newMessage(CSV_INPUT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Test
  public void testBrokenOutput_Array() throws Exception {
    CSVToJson service = createForTests();
    AdaptrisMessage msg = new BrokenMessageFactory(WhenToBreak.OUTPUT).newMessage(CSV_INPUT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected CSVToJson createForTests() {
    return new CSVToJson().withStyle(CSVToJson.OutputStyle.JSON_ARRAY);
  }

  private static Collection<AdaptrisMessage> collect(Iterable<AdaptrisMessage> iter)
      throws IOException, CoreException {
    if (iter instanceof Collection) {
      return (Collection<AdaptrisMessage>) iter;
    }
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    try (CloseableIterable<AdaptrisMessage> messages = CloseableIterable.ensureCloseable(iter)) {
      for (AdaptrisMessage msg : messages) {
        result.add(msg);
      }
    }
    return result;
  }
}
