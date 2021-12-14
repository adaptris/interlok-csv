package com.adaptris.core.transform.csvjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultAdaptrisMessageImp;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.CustomPreferenceBuilder;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.IdGenerator;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public abstract class CsvBaseCase extends ExampleServiceCase {

  public static final String CSV_INPUT = "firstname,lastname,dob\r\n" +
      "\"alice\",\"smith\",\"2017-01-01\"\r\n" +
      "\"bob\", \"smith\",\"2017-01-02\"\r\n" +
      "\"carol\",\"smith\",\"2017-01-03\"";

  public static final String JSON_ARRAY_INPUT = "[{ \"firstname\":\"alice\", \"lastname\":\"smith\", \"dob\":\"2017-01-01\" },"
          + "{ \"firstname\":\"bob\", \"lastname\":\"smith\", \"dob\":\"2017-01-02\" },"
          + "{ \"firstname\":\"carol\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }]";

  public static final String JSON_LINES_INPUT = "{ \"firstname\":\"alice\", \"lastname\":\"smith\", \"dob\":\"2017-01-01\" }\n"
      + "{ \"firstname\":\"bob\", \"lastname\":\"smith\", \"dob\":\"2017-01-02\" }\n"
      + "{ \"firstname\":\"carol\", \"lastname\":\"smith\", \"dob\":\"2017-01-03\" }\n";

  private Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
      .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();

  protected static enum WhenToBreak {
    INPUT,
    OUTPUT,
    BOTH,
    NEVER
  };

  protected abstract CSVConverter createForTests();


  @Test
  public void testPreferenceBuilder() throws Exception {
    CSVConverter service = createForTests();
    assertNotNull(service.getPreferenceBuilder());
    assertEquals(BasicPreferenceBuilder.class, service.getPreferenceBuilder().getClass());
    service.setPreferenceBuilder(new CustomPreferenceBuilder());
    assertEquals(CustomPreferenceBuilder.class, service.getPreferenceBuilder().getClass());
  }

  @Override
  protected CSVConverter retrieveObjectForSampleConfig() {
    return createForTests();
  }

  protected ReadContext parse(String content) {
    return JsonPath.parse(content, jsonConfig);
  }

  protected ReadContext parse(AdaptrisMessage content) {
    return parse(content.getContent());
  }

  protected class BrokenMessageFactory extends DefaultMessageFactory {

    private WhenToBreak when;

    public BrokenMessageFactory(WhenToBreak w) {
      when = w;
    }

    @Override
    public AdaptrisMessage newMessage() {
      AdaptrisMessage result = new BrokenMessage(uniqueIdGenerator(), this);
      return result;
    }

    boolean brokenInput() {
      return (when == WhenToBreak.INPUT) || (when == WhenToBreak.BOTH);
    }

    boolean brokenOutput() {
      return (when == WhenToBreak.OUTPUT) || (when == WhenToBreak.BOTH);
    }

  }


  public class BrokenMessage extends DefaultAdaptrisMessageImp {

    protected BrokenMessage(IdGenerator guid, AdaptrisMessageFactory amf) throws RuntimeException {
      super(guid, amf);
      setPayload(new byte[0]);
    }

    /**
     *
     * @see com.adaptris.core.AdaptrisMessage#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
      if (((BrokenMessageFactory) getFactory()).brokenInput()) {
        return new ErroringInputStream(super.getInputStream());
      }
      return super.getInputStream();
    }

    /**
     *
     * @see com.adaptris.core.AdaptrisMessage#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
      if (((BrokenMessageFactory) getFactory()).brokenOutput()) {
        return new ErroringOutputStream();
      }
      return super.getOutputStream();
    }

    private class ErroringOutputStream extends OutputStream {

      protected ErroringOutputStream() {
        super();
      }

      @Override
      public void write(int b) throws IOException {
        throw new IOException("Failed to write");
      }

    }

    private class ErroringInputStream extends FilterInputStream {

      protected ErroringInputStream(InputStream in) {
        super(in);
      }

      @Override
      public int read() throws IOException {
        throw new IOException("Failed to read");
      }

      @Override
      public int read(byte[] b) throws IOException {
        throw new IOException("Failed to read");
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        throw new IOException("Failed to read");
      }

    }
  }
}
