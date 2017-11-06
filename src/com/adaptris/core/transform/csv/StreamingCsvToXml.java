package com.adaptris.core.transform.csv;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.OrderedCsvMapReader;
import com.adaptris.csv.PreferenceBuilder;
import com.adaptris.stax.DefaultWriterFactory;
import com.adaptris.stax.StreamWriterFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * CSV to XML using {@code net.sf.supercsv:super-csv} via {@link XMLStreamWriter}
 * 
 * <p>
 * This transformation uses {@code net.sf.supercsv:super-csv} as the parsing engine for a CSV file and uses {@link XMLStreamWriter}
 * via {@code com.adaptris:interlok-stax} to write each row as part of an XML document. It may have better performance
 * characteristics with large CSV files compared to {@link SimpleCsvToXmlTransformService}. It also reduces the number of
 * configuration options that are available.
 * <p>
 * Element names are always generated from the first line; so a header line is always assumed; Note that if your header rows
 * contains characters that would not be allowed in an standard XML element name then it will be replaced with an '_', so "Order
 * Date" becomes "Order_Date". Additionally illegal XML characters are always stripped (illegal characters would cause an exception
 * regardless in the {@link XMLStreamWriter}).
 * </p>
 * <p>
 * For example, given an input document :
 * 
 * <pre>
 * <code>
Event Name,Order Date,Ticket Type,Date Attending,Total Paid
Glastonbury,"Sep 15, 2012",Free entry,"Jun 26, 2014 at 6:00 PM",0
Reading Festival,"Sep 16, 2012",Free entry,"Aug 30, 2014 at 6:00 PM",0
 * </code> </pre> Then the output (without a header row) would be
 * 
 * <pre>
 * <code>
 * &lt;csv-xml>
 *   &lt;record>
 *     &lt;Event_Name>Glastonbury&lt;/Event_Name>
 *     &lt;Order_Date>Sep 15, 2012&lt;/Order_Date>
 *     &lt;Ticket_Type>Free entry&lt;/Ticket_Type>
 *     &lt;Date_Attending>Jun 26, 2014 at 6:00 PM&lt;/Date_Attending>
 *     &lt;Total_Paid>0&lt;/Total_Paid>
 *   &lt;/record>
 *   &lt;record>
 *     &lt;Event_Name>Reading Festival&lt;/Event_Name>
 *     &lt;Order_Date>Sep 16, 2012&lt;/Order_Date>
 *     &lt;Ticket_Type>Free entry&lt;/Ticket_Type>
 *     &lt;Date_Attending>Aug 30, 2014 at 6:00 PM&lt;/Date_Attending>
 *     &lt;Total_Paid>0&lt;/Total_Paid>
 *   &lt;/record>
 * </code> </pre>
 * 
 * @config streaming-csv-to-xml-transform
 * 
 */
@XStreamAlias("streaming-csv-to-xml-transform")
@AdapterComponent
@ComponentProfile(summary = "Transform CSV to XML using the XML streaming API (STaX).", tag = "service,transform,csv,xml", since = "3.6.6")
@DisplayOrder(order = {"preferenceBuilder", "outputMessageEncoding", "streamWriterFactory"})
public class StreamingCsvToXml extends CsvXmlTransformImpl {

  @NotNull
  @AutoPopulated
  @Valid
  @InputFieldDefault(value = "csv-basic-preference-builder")
  private PreferenceBuilder preferenceBuilder;

  @Valid
  @AdvancedConfig
  @InputFieldDefault(value = "stax-default-stream-writer")
  private StreamWriterFactory streamWriter;

  public StreamingCsvToXml() {
    setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.STANDARD_PREFERENCE));
  }

  public StreamingCsvToXml(PreferenceBuilder prefs, StreamWriterFactory fact) {
    this();
    setPreferenceBuilder(prefs);
    setStreamWriter(fact);
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
    StreamWriterFactory writerFactory = writerFactory();
    XMLStreamWriter xmlWriter = null;
    int count = 0;
    long start = System.currentTimeMillis();
    try {
      String encoding = evaluateEncoding(msg);
      XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
      try (Reader reader = new BufferedReader(msg.getReader());
          OrderedCsvMapReader csvReader = new OrderedCsvMapReader(reader, getPreferenceBuilder().build());
          Writer writer = new BufferedWriter(msg.getWriter(encoding))) {

        xmlWriter = writerFactory.create(writer);
        log.trace("Using {}", xmlWriter.getClass().getName());
        xmlWriter.writeStartDocument(encoding, "1.0");
        xmlWriter.writeStartElement(XML_ROOT_ELEMENT);
        String[] hdrs = safeElementNames(csvReader.getHeader(true));
        for (Map<String, String> row; (row = csvReader.read(hdrs)) != null;) {
          xmlWriter.writeStartElement(CSV_RECORD_NAME);
          count++;
          for (Map.Entry<String, String> entry : row.entrySet()) {
            String elementData = XmlHelper.stripIllegalXmlCharacters(defaultIfEmpty(entry.getValue(), ""));
            if (!isEmpty(elementData)) {
              xmlWriter.writeStartElement(entry.getKey());
              xmlWriter.writeCharacters(XmlHelper.stripIllegalXmlCharacters(entry.getValue()));
              xmlWriter.writeEndElement();
            }
            else {
              xmlWriter.writeEmptyElement(entry.getKey());
            }
          }
          xmlWriter.writeEndElement();
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    finally {
      writerFactory.close(xmlWriter);
    }
    log.trace("Converted {} rows to XML in {}ms", count, System.currentTimeMillis() - start);
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  public PreferenceBuilder getPreferenceBuilder() {
    return preferenceBuilder;
  }

  public void setPreferenceBuilder(PreferenceBuilder b) {
    this.preferenceBuilder = Args.notNull(b, "preferenceBuilder");
  }

  /**
   * @return the streamWriterFactory
   */
  public StreamWriterFactory getStreamWriter() {
    return streamWriter;
  }

  /**
   * Set the stream writer factory to use.
   * 
   * @param factory the streamWriterFactory to set, default is {@link DefaultWriterFactory} if not specified.
   */
  public void setStreamWriter(StreamWriterFactory factory) {
    this.streamWriter = factory;
  }

  StreamWriterFactory writerFactory() {
    return getStreamWriter() != null ? getStreamWriter() : new DefaultWriterFactory();
  }

  private static String[] safeElementNames(String[] input) {
    for (int i = 0; i < input.length; i++) {
      input[i] = XmlHelper.safeElementName(input[i], "blank");
    }
    return input;
  }

}
