package com.adaptris.core.transform.csv;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;

/**
 * Base class for transforming CSV into XML.
 * 
 */
public abstract class CsvXmlTransformImpl extends ServiceImp {

  protected static final String CSV_RECORD_NAME = "record";
  protected static final String XML_ROOT_ELEMENT = "csv-xml";
  protected static final String CSV_FIELD_NAME = "csv-field";

  @AdvancedConfig
  @InputFieldHint(expression = true)
  private String outputMessageEncoding = null;

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
}
