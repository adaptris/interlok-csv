package com.adaptris.core.transform.csv;

import java.io.OutputStream;
import javax.validation.constraints.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

/**
 * Base class for transforming CSV into XML.
 *
 * @deprecated since 3.11.0 : switch to using net.supercsv based implementations instead
 *
 */
@Deprecated
@Removal(version = "4.0.0", message = "Switch to using net.supercsv based implementations instead")
public abstract class CsvToXmlServiceImpl extends CsvXmlTransformImpl {

  @NotNull
  @AutoPopulated
  private FormatBuilder format;
  @InputFieldDefault(value = "true")
  @AdvancedConfig
  private Boolean stripIllegalXmlChars = null;

  public CsvToXmlServiceImpl() {
    setFormat(new BasicFormatBuilder());
  }

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
    format = Args.notNull(csvFormat, "format");
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
    stripIllegalXmlChars = s;
  }

  protected boolean stripIllegalXmlChars() {
    return getStripIllegalXmlChars() != null ? getStripIllegalXmlChars().booleanValue() : true;
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

}
