package com.adaptris.csv.transform;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.XmlHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base class for transforming CSV into XML.
 *
 */
@NoArgsConstructor
public abstract class CsvXmlTransformImpl extends ServiceImp {

  protected static final String CSV_RECORD_NAME = "record";
  protected static final String XML_ROOT_ELEMENT = "csv-xml";
  protected static final String CSV_FIELD_NAME = "csv-field";

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
   * As a result; the character encoding on the message is always set using
   * {@link AdaptrisMessage#setCharEncoding(String)}.
   * </p>
   *
   */
  @AdvancedConfig
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  private String outputMessageEncoding = null;
  /**
   * Specify whether or not to include the line number as an attribute on each record.
   *
   *
   */
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean includeLineNumberAttribute = null;

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {}


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

  protected boolean includeLineNumberAttribute() {
    return BooleanUtils.toBooleanDefaultIfNull(getIncludeLineNumberAttribute(), false);
  }


  protected static String[] safeElementNames(String[] input) {
    for (int i = 0; i < input.length; i++) {
      input[i] = XmlHelper.safeElementName(input[i], "blank");
    }
    return input;
  }

  protected static List<String> safeElementNames(List<String> input) {
    return input.stream().map((s) -> XmlHelper.safeElementName(s, "blank"))
        .collect(Collectors.toList());
  }

}
