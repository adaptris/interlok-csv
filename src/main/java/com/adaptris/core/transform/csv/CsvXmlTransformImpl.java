package com.adaptris.core.transform.csv;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;

/**
 * Base class for transforming CSV into XML.
 *
 * @deprecated since 3.11.0 : switch to using net.supercsv based implementations instead
 */
@Deprecated
@Removal(version = "4.0.0", message = "Switch to using net.supercsv based implementations instead")
public abstract class CsvXmlTransformImpl extends ServiceImp {

  protected static final String CSV_RECORD_NAME = "record";
  protected static final String XML_ROOT_ELEMENT = "csv-xml";
  protected static final String CSV_FIELD_NAME = "csv-field";

  @AdvancedConfig
  @InputFieldHint(expression = true)
  private String outputMessageEncoding = null;
  @InputFieldDefault(value = "false")
  private Boolean includeLineNumberAttribute = null;

  public CsvXmlTransformImpl() {
  }

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {}

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

  protected String evaluateEncoding(AdaptrisMessage msg) {
    String encoding = "UTF-8";
    if (!isEmpty(getOutputMessageEncoding())) {
      encoding = defaultIfEmpty(msg.resolve(getOutputMessageEncoding()), "UTF-8");
    }
    else if (!isEmpty(msg.getContentEncoding())) {
      encoding = msg.getContentEncoding();
    }
    return encoding;
  }


  public Boolean getIncludeLineNumberAttribute() {
    return includeLineNumberAttribute;
  }

  /**
   * Specify whether or not to include the line number as an attribute on each record.
   *
   *
   * @param b whether to include the line number attribute default null (false)
   */
  public void setIncludeLineNumberAttribute(Boolean b) {
    includeLineNumberAttribute = b;
  }

  protected boolean includeLineNumberAttribute() {
    return BooleanUtils.toBooleanDefaultIfNull(getIncludeLineNumberAttribute(), false);
  }
}
