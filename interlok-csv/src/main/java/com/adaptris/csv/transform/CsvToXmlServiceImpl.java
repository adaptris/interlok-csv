package com.adaptris.csv.transform;

import java.io.OutputStream;
import jakarta.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.prefs.CsvPreference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.BasicPreferenceBuilder.Style;
import com.adaptris.csv.PreferenceBuilder;
import com.adaptris.util.XmlUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base class for transforming CSV into XML.
 *
 */
@NoArgsConstructor
public abstract class CsvToXmlServiceImpl extends CsvXmlTransformImpl {

  /**
   * How to parse the CSV file.
   *
   */
  @Valid
  @Getter
  @Setter
  @InputFieldDefault(value = "csv-basic-preference-builder")
  private PreferenceBuilder preferenceBuilder;
  /**
   * Specify whether or not to strip illegal XML characters from all the data before converting to XML.
   * <p>
   * The following regular expression is used to strip out all invalid XML 1.0 characters using {@link
   * XmlHelper#stripIllegalXmlCharacters(String)}
   * </p>
   *
   * @see XmlHelper#stripIllegalXmlCharacters(String)
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "true")
  @AdvancedConfig
  private Boolean stripIllegalXmlChars = null;

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

  protected boolean stripIllegalXmlChars() {
    return BooleanUtils.toBooleanDefaultIfNull(getStripIllegalXmlChars(), true);
  }

  protected Node createTextNode(Document doc, String value) {
    String v = StringUtils.defaultIfEmpty(value, "");
    String munged = stripIllegalXmlChars() ? XmlHelper.stripIllegalXmlCharacters(v) : v;
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

  protected CsvPreference buildPreferences() {
    return ObjectUtils.defaultIfNull(getPreferenceBuilder(),
        new BasicPreferenceBuilder(Style.STANDARD_PREFERENCE)).build();
  }
}
