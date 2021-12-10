package com.adaptris.csv.aggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;

public class CsvValidatingAggregatorTest
{
  @Test
  public void testAggregate_WithHeader() throws Exception
  {
    CsvValidatingAggregator ag = new CsvValidatingAggregator().withHeader("key,value");
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ag.joinMessage(message, Arrays.asList(AdaptrisMessageFactory.getDefaultInstance().newMessage("k1,v1\nk2,v2\n"),
        AdaptrisMessageFactory.getDefaultInstance().newMessage("k3,v3\nk4,v4\n")));
    List<String> actualLines = IOUtils.readLines(new StringReader(message.getContent()));
    assertTrue(ag.forceColumns());
    assertEquals(5, actualLines.size());
    assertEquals("key,value", actualLines.get(0));
    assertEquals("k1,v1", actualLines.get(1));
    assertEquals("k2,v2", actualLines.get(2));
    assertEquals("k3,v3", actualLines.get(3));
    assertEquals("k4,v4", actualLines.get(4));
  }

  @Test
  public void testAggregate_NoHeader() throws Exception
  {
    CsvValidatingAggregator ag = new CsvValidatingAggregator();
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ag.joinMessage(message, Arrays.asList(AdaptrisMessageFactory.getDefaultInstance().newMessage("k1,v1\nk2,v2\n"), AdaptrisMessageFactory.getDefaultInstance().newMessage("k3,v3\nk4,v4\n")));
    List<String> actualLines = IOUtils.readLines(new StringReader(message.getContent()));
    assertEquals(4, actualLines.size());
    assertEquals("k1,v1", actualLines.get(0));
    assertEquals("k2,v2", actualLines.get(1));
    assertEquals("k3,v3", actualLines.get(2));
    assertEquals("k4,v4", actualLines.get(3));
  }

  @Test
  public void testAggregate_MultiHeader() throws Exception
  {
    CsvValidatingAggregator ag = new CsvValidatingAggregator().withHeader("key,value\nk2,v2");
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ag.joinMessage(message, Arrays.asList(AdaptrisMessageFactory.getDefaultInstance().newMessage("k3,v3\nk4,v4\n"),
        AdaptrisMessageFactory.getDefaultInstance().newMessage("k5,v5\nk6,v6\n")));
    List<String> actualLines = IOUtils.readLines(new StringReader(message.getContent()));
    assertEquals(6, actualLines.size());
    assertEquals("key,value", actualLines.get(0));
    assertEquals("k2,v2", actualLines.get(1));
    assertEquals("k3,v3", actualLines.get(2));
    assertEquals("k4,v4", actualLines.get(3));
    assertEquals("k5,v5", actualLines.get(4));
    assertEquals("k6,v6", actualLines.get(5));
  }

  @Test(expected = CoreException.class)
  public void testAggregate_MismatchedColumns() throws Exception
  {
    CsvValidatingAggregator ag = new CsvValidatingAggregator().withHeader("key,value");
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ag.joinMessage(message, Arrays.asList(AdaptrisMessageFactory.getDefaultInstance().newMessage("k1\nk2,v2\n\n"),
        AdaptrisMessageFactory.getDefaultInstance().newMessage("k3,v3,z3\n,v4\nk5,v5\n")));
  }

  @Test
  public void testAggregate_OldBehaviour() throws Exception
  {
    CsvValidatingAggregator ag = new CsvValidatingAggregator().withHeader("key,value");
    ag.setForceColumns(false);
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    ag.joinMessage(message, Arrays.asList(AdaptrisMessageFactory.getDefaultInstance().newMessage("k1\nk2,v2\n\n"),
        AdaptrisMessageFactory.getDefaultInstance().newMessage("k4,v4,z4\n,\nk6,v6\n")));
    List<String> actualLines = IOUtils.readLines(new StringReader(message.getContent()));
    assertFalse(ag.getForceColumns());
    assertEquals(6, actualLines.size());
    assertEquals("key,value", actualLines.get(0));
    assertEquals("k1", actualLines.get(1));
    assertEquals("k2,v2", actualLines.get(2));
    assertEquals("k4,v4,z4", actualLines.get(3));
    // This isn't true, since empty values don't render with super-csv
    // assertEquals("\"\",", actualLines.get(4)); // forced an empty value
    assertEquals("k6,v6", actualLines.get(5));
  }

  @Test(expected = CoreException.class)
  public void testAggregate_Broken() throws Exception
  {
    CsvValidatingAggregator ag = new CsvValidatingAggregator().withHeader("key,value");
    AdaptrisMessage message = new DefectiveMessageFactory(WhenToBreak.OUTPUT).newMessage();
    ag.joinMessage(message, Arrays.asList(AdaptrisMessageFactory.getDefaultInstance().newMessage("k1,v1\nk2,v2\n"),
        AdaptrisMessageFactory.getDefaultInstance().newMessage("k3,v3\nk4,v4\n")));
  }

}
