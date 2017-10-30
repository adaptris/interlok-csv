package com.adaptris.csv.stax;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.Writer;
import java.util.Arrays;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.stream.XMLStreamWriter;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;

/**
 * {@link StreamWriterFactory} implementation that uses {@code net.sf.saxon.s9api.Serializer#getXMLStreamWriter()}.
 * 
 * @config csv-saxon-stream-writer
 */
@XStreamAlias("csv-saxon-stream-writer")
public class SaxonStreamWriterFactory extends StreamWriterFactoryImpl {

  private transient static InheritableThreadLocal<Serializer> localSerializer = new InheritableThreadLocal<>();

  @NotNull
  @AutoPopulated
  @Valid
  @InputFieldDefault(value = "empty set")
  private KeyValuePairSet outputProperties;

  public SaxonStreamWriterFactory() {
    super();
    setOutputProperties(new KeyValuePairSet());
  }

  public SaxonStreamWriterFactory(KeyValuePairSet outputProperties) {
    this();
    setOutputProperties(outputProperties);
  }

  public SaxonStreamWriterFactory(KeyValuePair... outputProperties) {
    this(new KeyValuePairSet(Arrays.asList(outputProperties)));
  }

  @Override
  public XMLStreamWriter create(Writer w) throws Exception {
    Serializer serializer = configure(new Processor(new Configuration()).newSerializer(w));
    localSerializer.set(serializer);
    return serializer.getXMLStreamWriter();
  }


  @Override
  public void close(XMLStreamWriter w) {
    super.close(w);
    closeQuietly(localSerializer.get());
    localSerializer.set(null);
  }

  private static void closeQuietly(Serializer w) {
    try {
      if (w != null) {
        w.close();
      }
    } catch (Exception ignore) {

    }
  }

  private Serializer configure(Serializer serializer) {
    for (KeyValuePair kvp : getOutputProperties()) {
      try {
        Serializer.Property p = Serializer.Property.valueOf(defaultIfEmpty(kvp.getKey(), "").toUpperCase());
        serializer.setOutputProperty(p, kvp.getValue());
      } catch (IllegalArgumentException e) {
        log.warn("Ignoring {} as an output property", kvp.getKey());
      }
    }
    return serializer;
  }

  /**
   * @return the outputProperties
   */
  public KeyValuePairSet getOutputProperties() {
    return outputProperties;
  }

  /**
   * Set any output properties required.
   * <p>
   * The keys should match the enums specified by {@code Serializer.Property}; bear in mind no validation is done on the values. For
   * instance {@code INDENT=yes} would effectively invoke {@code Serializer#setOutputProperty(Serializer.Property.INDENT, "yes")}.
   * </p>
   * 
   * @param kvps any output properties to set
   */
  public void setOutputProperties(KeyValuePairSet kvps) {
    this.outputProperties = Args.notNull(kvps, "outputProperties");
  }
}
