package interlok.csv.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import interlok.csv.schema.SchemaViolationHandler.FailureType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;
import uk.gov.nationalarchives.csv.validator.api.java.ErrorMessage;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;
import uk.gov.nationalarchives.csv.validator.api.java.WarningMessage;

public class SchemaViolationHandlerTest {


  @Test
  public void testFilter() {
    Collection<FailMessage> failures = createFailures(10, 10);
    Collection<FailMessage> warnings = FailureType.Warning.filter(failures);
    Collection<FailMessage> errors = FailureType.Error.filter(failures);
    Collection<FailMessage> any = FailureType.All.filter(failures);
    assertEquals(10, warnings.size());
    assertEquals(10, errors.size());
    assertEquals(20, any.size());
  }

  @Test
  public void testToString() {
    Collection<FailMessage> failures = createFailures(10, 10);
    SchemaViolationHandler handler = (f, m) -> {};
    String toString = handler.toString(failures);
    assertTrue(toString.contains("error 9"));
    assertTrue(toString.contains("warning 5"));
  }

  @Test
  public void testFailOnError() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Collection<FailMessage> noErrors = createFailures(10, 0);
    Collection<FailMessage> hasErrors = createFailures(10, 10);
    SchemaViolationHandler handler = new FailOnError();
    handler.handle(noErrors, msg);
    assertThrows(ServiceException.class, () -> handler.handle(hasErrors, msg));
  }

  @Test
  public void testIgnoreViolations() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Collection<FailMessage> noErrors = createFailures(10, 0);
    Collection<FailMessage> hasErrors = createFailures(10, 10);
    SchemaViolationHandler handler = new IgnoreViolations();
    handler.handle(noErrors, msg);
    handler.handle(hasErrors, msg);
  }

  @Test
  public void testFailOnWarningOrError() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Collection<FailMessage> noErrors = createFailures(10, 0);
    Collection<FailMessage> hasErrors = createFailures(0, 10);
    SchemaViolationHandler handler = new FailOnWarningOrError();
    handler.handle(Collections.emptyList(), msg);
    assertThrows(ServiceException.class, () -> handler.handle(noErrors, msg));
    assertThrows(ServiceException.class, () -> handler.handle(hasErrors, msg));
  }

  @Test
  public void testErrorsAsMetadata_HasErrors() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Collection<FailMessage> hasErrors = createFailures(10, 10);
    SchemaViolationHandler handler = new ErrorsAsMetadata().withMetadataKey("schemaViolations");
    handler.handle(hasErrors, msg);
    assertTrue(msg.headersContainsKey("schemaViolations"));
    String toString = msg.getMetadataValue("schemaViolations");
    assertTrue(toString.contains("error 9"));
    assertFalse(toString.contains("warning 9"));
  }

  @Test
  public void testErrorsAsMetadata_NoErrors() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Collection<FailMessage> warningsOnly = createFailures(10, 0);
    SchemaViolationHandler handler = new ErrorsAsMetadata().withMetadataKey("schemaViolations");
    handler.handle(warningsOnly, msg);
    assertFalse(msg.headersContainsKey("schemaViolations"));
 }

  @Test
  public void testFailuresAsMetadata() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Collection<FailMessage> hasErrors = createFailures(10, 10);
    SchemaViolationHandler handler = new FailuresAsMetadata().withMetadataKey("schemaViolations");
    handler.handle(hasErrors, msg);
    assertTrue(msg.headersContainsKey("schemaViolations"));
    String toString = msg.getMetadataValue("schemaViolations");
    assertTrue(toString.contains("error 9"));
    assertTrue(toString.contains("warning 9"));
  }

  @Test
  public void testFailuresAsMetadata_NoFailures() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    SchemaViolationHandler handler = new FailuresAsMetadata().withMetadataKey("schemaViolations");
    handler.handle(Collections.emptyList(), msg);
    assertFalse(msg.headersContainsKey("schemaViolations"));
  }

  public static Collection<FailMessage> createFailures(int warnings, int errors) {
    Random rand = ThreadLocalRandom.current();
    List<FailMessage> result = new ArrayList<>();
    for (int i = 0; i < warnings; i++) {
      result.add(new WarningMessage("warning " + i, rand.nextInt(20), rand.nextInt(20) ));
    }
    for (int i = 0; i < errors; i++) {
      result.add(new ErrorMessage("error " + i, rand.nextInt(20), rand.nextInt(20) ));
    }
    return result;
  }

}
