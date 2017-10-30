package com.adaptris.csv.stax;

import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides empty methods for {@link StreamWriterFactory} lifecycle.
 * 
 */
public abstract class StreamWriterFactoryImpl implements StreamWriterFactory {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());


  @Override
  public void close(XMLStreamWriter w) {
    closeQuietly(w);
  }

  protected static void closeQuietly(XMLStreamWriter w) {
    try {
      if (w != null) {
        w.close();
      }
    } catch (Exception ignore) {

    }
  }
}
