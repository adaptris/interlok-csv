package com.adaptris.csv.aggregator;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregatorImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;

@XStreamAlias("csv-aggregator")
public class CsvAggregator extends MessageAggregatorImpl {

  private String header;

  @Override
  public void joinMessage(AdaptrisMessage adaptrisMessage, Collection<AdaptrisMessage> collection) throws CoreException {
    try (OutputStream outputStream = new BufferedOutputStream(adaptrisMessage.getOutputStream())) {
      if (getHeader() != null) {
        outputStream.write(getHeader().concat(System.lineSeparator()).getBytes(Charset.forName("UTF-8")));
      }
      for(AdaptrisMessage msg : filter(collection)){
        IOUtils.copy(msg.getInputStream(), outputStream);
        overwriteMetadata(msg, adaptrisMessage);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public String getHeader() {
    return header;
  }

  CsvAggregator withHeader(String header){
    setHeader(header);
    return this;
  }
}
