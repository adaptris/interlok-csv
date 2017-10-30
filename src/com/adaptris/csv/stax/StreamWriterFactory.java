package com.adaptris.csv.stax;

import java.io.Writer;

import javax.xml.stream.XMLStreamWriter;

/**
 * Pluggable implementation for building specific {@link XMLStreamWriter} instances.
 * 
 *
 */
public interface StreamWriterFactory {

  /**
   * Create a {@link XMLStreamWriter}.
   * 
   */
  XMLStreamWriter create(Writer w) throws Exception;

  /**
   * Close the {@link XMLStreamWriter} and any other resources.
   * 
   */
  void close(XMLStreamWriter w);
}
