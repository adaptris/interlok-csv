package com.adaptris.csv.stax;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.Writer;

import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;

// to avoid possible NoClassDef issues in the event there is no saxon available (but you're just using the default).
// Of course if you actually configure a SaxonStreamWriterFactory, then there will be a NoClassDef
class SaxonWriterImpl {
  private transient static InheritableThreadLocal<Serializer> mySerializer = new InheritableThreadLocal<>();
  private transient Logger log = LoggerFactory.getLogger(SaxonStreamWriterFactory.class);

  SaxonWriterImpl() {

  }

  XMLStreamWriter create(Writer w, KeyValuePairSet conf) throws Exception {
    Serializer serializer = configure(new Processor(new Configuration()).newSerializer(w), conf);
    mySerializer.set(serializer);
    return serializer.getXMLStreamWriter();
  }


  void close() {
    closeQuietly(mySerializer.get());
    mySerializer.set(null);
  }

  private static void closeQuietly(Serializer w) {
    try {
      if (w != null) {
        w.close();
      }
    } catch (Exception ignore) {

    }
  }

  private Serializer configure(Serializer serializer, KeyValuePairSet conf) {
    for (KeyValuePair kvp : conf) {
      try {
        Serializer.Property p = Serializer.Property.valueOf(defaultIfEmpty(kvp.getKey(), "").toUpperCase());
        serializer.setOutputProperty(p, kvp.getValue());
      } catch (IllegalArgumentException e) {
        log.warn("Ignoring {} as an output property", kvp.getKey());
      }
    }
    return serializer;
  }

}
