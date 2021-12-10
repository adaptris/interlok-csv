package interlok.csv.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class ValidateCsvTest extends ExampleServiceCase {

  public static final String TEST_SCHEMA = "CsvSchema.file";
  public static final String TEST_INPUT_VALID = "CsvSchema.input.passes";
  public static final String TEST_INPUT_WARNING = "CsvSchema.input.warnings";
  public static final String TEST_INPUT_ERRORS = "CsvSchema.input.errors";

  @Test
  public void testCsvValid() throws Exception {
    ValidateCsv service = new ValidateCsv().withSchema(getConfiguration(TEST_SCHEMA));
    service.setSubstitutions(createSubstitutions());
    File file = new File(getConfiguration(TEST_INPUT_VALID));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(FileUtils.readFileToByteArray(file));
    execute(service, msg);
  }

  @Test
  public void testCsvWithErrors() throws Exception {
    ValidateCsv service = new ValidateCsv().withSchema(getConfiguration(TEST_SCHEMA))
        .withViolationHandler(new FailOnError());
    File file = new File(getConfiguration(TEST_INPUT_ERRORS));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(FileUtils.readFileToByteArray(file));
    assertThrows(ServiceException.class, () -> execute(service, msg));
  }

  @Test
  public void testCsvWithWarnings() throws Exception {
    ValidateCsv service = new ValidateCsv().withSchema(getConfiguration(TEST_SCHEMA))
        .withViolationHandler(new FailOnError());
    File file = new File(getConfiguration(TEST_INPUT_WARNING));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(FileUtils.readFileToByteArray(file));
    // No Exceptions
    execute(service, msg);
  }

  @Test
  public void testCsvWithErrors_AsMetadata() throws Exception {
    ValidateCsv service = new ValidateCsv().withSchema(getConfiguration(TEST_SCHEMA))
        .withViolationHandler(new ErrorsAsMetadata().withMetadataKey("schemaViolations"));
    File file = new File(getConfiguration(TEST_INPUT_ERRORS));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(FileUtils.readFileToByteArray(file));
    // No Exceptions
    execute(service, msg);
    assertTrue(msg.headersContainsKey("schemaViolations"));
    String toString = msg.getMetadataValue("schemaViolations");
    System.err.println("-----------------");
    System.err.println(toString);
    // is("WO") should be marked as a warning in the schema
    assertFalse(toString.contains("is(\"WO\")"));
    assertTrue(toString.contains("is(\"13\")"));
    assertEquals(2, StringUtils.countMatches(toString, "is(\"13\")"));
  }

  @Test
  public void testCsvWithWarnings_Metadata_FailFast() throws Exception {
    ValidateCsv service = new ValidateCsv().withSchema(getConfiguration(TEST_SCHEMA))
        .withViolationHandler(new FailuresAsMetadata().withMetadataKey("schemaViolations"));
    service.setFailFast(true);
    File file = new File(getConfiguration(TEST_INPUT_ERRORS));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(FileUtils.readFileToByteArray(file));
    // No Exceptions
    execute(service, msg);
    assertTrue(msg.headersContainsKey("schemaViolations"));
    String toString = msg.getMetadataValue("schemaViolations");
    System.err.println("-----------------");
    System.err.println(toString);
    // is("WO") is marked as a warning in the schema
    // Since we fail fast we will get warnings, but only 1 error from is("13")
    // We should also not get any "errors" since we shouldn't fire is("13") on department.
    assertTrue(toString.contains("is(\"WO\")"));
    assertTrue(toString.contains("is(\"13\")"));
    assertEquals(1, StringUtils.countMatches(toString, "is(\"13\")"));

  }

  @Override
  protected ValidateCsv retrieveObjectForSampleConfig() {
    return new ValidateCsv().withSchema("%message{csv-schema}");
  }

  private KeyValuePairSet createSubstitutions() {
    KeyValuePairSet result = new KeyValuePairSet();
    result.add(new KeyValuePair("dataPath", "./data"));
    return result;
  }
}
