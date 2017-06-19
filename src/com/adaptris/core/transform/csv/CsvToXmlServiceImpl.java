package com.adaptris.core.transform.csv;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.OutputStream;

import javax.validation.constraints.NotNull;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

/**
 * Base class for transforming CSV into XML.
 * 
 */
public abstract class CsvToXmlServiceImpl extends ServiceImp {

  protected static final String CSV_RECORD_NAME = "record";
  protected static final String XML_ROOT_ELEMENT = "csv-xml";
  protected static final String CSV_FIELD_NAME = "csv-field";

  @NotNull
  @AutoPopulated
  private FormatBuilder format;
  @AdvancedConfig
  private String outputMessageEncoding = null;
  @InputFieldDefault(value = "true")
  @AdvancedConfig
  private Boolean stripIllegalXmlChars = null;

  public CsvToXmlServiceImpl() {
    setFormat(new BasicFormatBuilder());
  }

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {}

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Document doc = transform(msg);
      writeXmlDocument(doc, msg);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  protected abstract Document transform(AdaptrisMessage msg) throws ServiceException;

  public FormatBuilder getFormat() {
    return format;
  }

  public void setFormat(FormatBuilder csvFormat) {
    this.format = Args.notNull(csvFormat, "format");
  }

  public Boolean getStripIllegalXmlChars() {
    return stripIllegalXmlChars;
  }

  /**
   * Specify whether or not to strip illegal XML characters from all the data before converting to XML.
   * <p>
   * The following regular expression is used to strip out all invalid XML 1.0 characters :
   * <code>"[^\u0009\r\n\u0020-\uD7FF\uE000-\uFFFD\ud800\udc00-\udbff\udfff]"</code>.
   * </p>
   * 
   * @param s true to enable stripping, default is null (true)
   */
  public void setStripIllegalXmlChars(Boolean s) {
    this.stripIllegalXmlChars = s;
  }

  boolean stripIllegalXmlChars() {
    return getStripIllegalXmlChars() != null ? getStripIllegalXmlChars().booleanValue() : true;
  }

  public String getOutputMessageEncoding() {
    return outputMessageEncoding;
  }

  /**
   * Set the encoding for the resulting XML document.
   * <p>
   * If not specified the following rules will be applied:
   * </p>
   * <ol>
   * <li>If the {@link AdaptrisMessage#getCharEncoding()} is non-null then that will be used.</li>
   * <li>UTF-8</li>
   * </ol>
   * <p>
   * As a result; the character encoding on the message is always set using {@link AdaptrisMessage#setCharEncoding(String)}.
   * </p>
   * 
   * @param encoding the character
   */
  public void setOutputMessageEncoding(String encoding) {
    outputMessageEncoding = encoding;
  }

  protected Node createTextNode(Document doc, String value) {
    String munged = stripIllegalXmlChars() ? XmlHelper.stripIllegalXmlCharacters(value) : value;
    return doc.createTextNode(munged);
  }

  protected Element addNewElement(Document doc, Element parent, String name) {
    Element newElement = doc.createElement(name);
    parent.appendChild(newElement);
    return newElement;
  }

  /**
   * Helper method to write the XML document to the AdaptrisMessage taking into account any encoding requirements.
   * 
   * @param doc the XML document
   * @param msg the AdaptrisMessage
   * @throws Exception
   */
  protected void writeXmlDocument(Document doc, AdaptrisMessage msg) throws Exception {
    try (OutputStream out = msg.getOutputStream()) {
      String encoding = evaluateEncoding(msg);
      new XmlUtils().writeDocument(doc, out, encoding);
      msg.setContentEncoding(encoding);
    }
  }

  private String evaluateEncoding(AdaptrisMessage msg) {
    String encoding = "UTF-8";
    if (!isEmpty(getOutputMessageEncoding())) {
      encoding = getOutputMessageEncoding();
    }
    else if (!isEmpty(msg.getContentEncoding())) {
      encoding = msg.getContentEncoding();
    }
    return encoding;
  }
}
