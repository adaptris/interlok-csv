package com.adaptris.csv.splitter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.supercsv.prefs.CsvPreference;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.splitter.MessageSplitterImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.CloseableIterable;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.OrderedCsvMapReader;
import com.adaptris.csv.PreferenceBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author mwarman
 */
@XStreamAlias("csv-metadata-splitter")
public class CsvMetadataSplitter extends MessageSplitterImp {

  @NotNull
  @AutoPopulated
  @Valid
  @InputFieldDefault(value = "csv-basic-preference-builder")
  private PreferenceBuilder preferenceBuilder;

  public CsvMetadataSplitter() {
    setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.STANDARD_PREFERENCE));
  }

  @Override
  public CloseableIterable<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    try {
      return new CsvSplitGenerator(msg, getPreferenceBuilder().build());
    } catch (IOException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  public PreferenceBuilder getPreferenceBuilder() {
    return preferenceBuilder;
  }

  public void setPreferenceBuilder(PreferenceBuilder b) {
    this.preferenceBuilder = Args.notNull(b, "preferenceBuilder");
  }

  private class CsvSplitGenerator extends OrderedCsvMapReader implements CloseableIterable<AdaptrisMessage>, Iterator<AdaptrisMessage>{

    private AdaptrisMessage originalMessage;
    private boolean firstLineCheck = true;
    private String[] headers;
    private AdaptrisMessage nextMessage;
    private boolean iteratorInvoked = false;

    private CsvSplitGenerator(AdaptrisMessage adaptrisMessage, CsvPreference preferences) throws IOException {
      super(adaptrisMessage.getReader(), preferences);
      this.originalMessage = adaptrisMessage;
    }

    @Override
    public boolean hasNext() {
      try {
        if(firstLineCheck && headers == null){
          headers = this.getHeader(true);
          firstLineCheck = false;
        }
        if (nextMessage == null) {
          Map<String, String> inputRow = this.read(headers);
          if (inputRow != null) {
            AdaptrisMessage splitMsg = selectFactory(originalMessage).newMessage();
            copyMetadata(originalMessage, splitMsg);
            addMetadata(splitMsg, inputRow);
            nextMessage = splitMsg;
          }
        }
      } catch (IOException e) {
        throw new RuntimeException("Could not read line", e);
      }
      return nextMessage != null;
    }

    private void addMetadata(AdaptrisMessage message, Map<String, String> headers) {
      for(Map.Entry<String, String> e : headers.entrySet()){
        message.addMetadata(e.getKey(), StringUtils.defaultIfBlank(e.getValue(), ""));
      }
    }

    @Override
    public AdaptrisMessage next() {
      AdaptrisMessage next = nextMessage;
      nextMessage = null;
      return next;
    }

    @Override
    public Iterator<AdaptrisMessage> iterator() {
      if (iteratorInvoked) {
        throw new IllegalStateException("iterator already invoked");
      }
      iteratorInvoked = true;
      return this;
    }
  }
}
