package com.adaptris.cvs.stax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.csv.stax.SaxonStreamWriterFactory;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

import net.sf.saxon.s9api.Serializer;

public class SaxonWriterFactoryTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}


  @Test
  public void testCreate() throws Exception {
    SaxonStreamWriterFactory factory = new SaxonStreamWriterFactory(new KeyValuePair(Serializer.Property.INDENT.name(), "yes"),
        new KeyValuePair("not-known", "false"));
    assertNotNull(factory.create(new StringWriter()));
  }

  @Test
  public void testClose() throws Exception {
    SaxonStreamWriterFactory factory = new SaxonStreamWriterFactory();
    XMLStreamWriter writer = factory.create(new StringWriter());
    assertNotNull(writer);
    factory.close(writer);
    factory.close(null);
  }

  @Test
  public void testConstructors() {
    SaxonStreamWriterFactory factory = new SaxonStreamWriterFactory();
    assertNotNull(factory.getOutputProperties());
    assertEquals(0, factory.getOutputProperties().size());

    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair(Serializer.Property.INDENT.name(), "yes"));
    factory = new SaxonStreamWriterFactory(kvps);
    assertEquals(1, factory.getOutputProperties().size());
    assertEquals(kvps, factory.getOutputProperties());

    factory = new SaxonStreamWriterFactory(new KeyValuePair(Serializer.Property.INDENT.name(), "yes"));
    assertEquals(1, factory.getOutputProperties().size());

  }


}
