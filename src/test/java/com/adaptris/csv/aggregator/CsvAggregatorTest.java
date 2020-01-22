package com.adaptris.csv.aggregator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CsvAggregatorTest {

  @Test
  public void test() throws Exception{
    CsvAggregator csvAggregator  = new CsvAggregator().withHeader("key,value");
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    csvAggregator.joinMessage(message, Arrays.asList(AdaptrisMessageFactory.getDefaultInstance().newMessage("k1,v1\nk2,v2\n"), AdaptrisMessageFactory.getDefaultInstance().newMessage("k3,v3\nk4,v4\n")));
    List<String> actualLines = IOUtils.readLines(new StringReader(message.getContent()));
    assertEquals(5, actualLines.size());
    assertEquals("key,value", actualLines.get(0));
    assertEquals("k1,v1", actualLines.get(1));
    assertEquals("k2,v2", actualLines.get(2));
    assertEquals("k3,v3", actualLines.get(3));
    assertEquals("k4,v4", actualLines.get(4));
  }

}
