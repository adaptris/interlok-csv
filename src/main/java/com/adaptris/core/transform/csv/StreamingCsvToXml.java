package com.adaptris.core.transform.csv;

import javax.xml.stream.XMLStreamWriter;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LoggingHelper;
import lombok.NoArgsConstructor;

/**
 * CSV to XML using {@code net.sf.supercsv:super-csv} via {@link XMLStreamWriter}
 * @deprecated since 3.11.0 : Use {@link com.adaptris.csv.transform.StreamingCsvToXml} instead.
 */
@Deprecated
@ComponentProfile(summary = "Transform CSV to XML using the XML streaming API (STaX).", tag = "service,transform,csv,xml", since = "3.6.6")
@DisplayOrder(order = {"preferenceBuilder", "outputMessageEncoding", "streamWriterFactory"})
@Removal(version = "4.0.0", message = "Use com.adaptris.csv.transform.StreamingCsvToXml instead")
@NoArgsConstructor
public class StreamingCsvToXml extends com.adaptris.csv.transform.StreamingCsvToXml {

  private transient boolean warningLogged;


  @Override
  public void prepare() throws CoreException {
    LoggingHelper.logDeprecation(warningLogged, () -> warningLogged = true,
        this.getClass().getCanonicalName(),
        com.adaptris.csv.transform.StreamingCsvToXml.class.getCanonicalName());
    super.prepare();
  }



}
