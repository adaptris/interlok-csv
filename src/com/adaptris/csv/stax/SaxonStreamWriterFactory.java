package com.adaptris.csv.stax;

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

/**
 * {@link StreamWriterFactory} implementation that uses {@code net.sf.saxon.s9api.Serializer#getXMLStreamWriter()}.
 * 
 * <p>
 * Using this factory allows to configure various Saxon output properties (such as INDENT) to
 * 
 * @config csv-saxon-stream-writer
 */
@XStreamAlias("csv-saxon-stream-writer")
public class SaxonStreamWriterFactory extends StreamWriterFactoryImpl {
  private transient static InheritableThreadLocal<SaxonWriterImpl> myFactory = new InheritableThreadLocal<SaxonWriterImpl>() {
    public SaxonWriterImpl initialValue() {
      return new SaxonWriterImpl();
    }
  };

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
    return myFactory.get().create(w, getOutputProperties());
  }


  @Override
  public void close(XMLStreamWriter w) {
    super.close(w);
    myFactory.get().close();
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
   * The keys should match the enums specified by {@code net.sf.saxon.s9api.Serializer.Property}; bear in mind no validation is done
   * on the values. For instance {@code INDENT=yes} would effectively invoke
   * {@code Serializer#setOutputProperty(Serializer.Property.INDENT, "yes")}.
   * </p>
   * 
   * @param kvps any output properties to set
   */
  public void setOutputProperties(KeyValuePairSet kvps) {
    this.outputProperties = Args.notNull(kvps, "outputProperties");
  }
}
