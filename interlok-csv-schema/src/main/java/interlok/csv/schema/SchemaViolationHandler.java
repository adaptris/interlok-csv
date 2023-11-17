package interlok.csv.schema;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.stream.Collectors;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.InterlokException;

import uk.gov.nationalarchives.csv.validator.api.java.ErrorMessage;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;
import uk.gov.nationalarchives.csv.validator.api.java.WarningMessage;

/**
 * Interface that allows us to plugin behaviour around warnings and errors.
 *
 */
@FunctionalInterface
public interface SchemaViolationHandler {

  enum FailureType {
    Warning() {
      @Override
      public Collection<FailMessage> filter(Collection<FailMessage> failures) {
        return failures.stream().filter((e) -> (e instanceof WarningMessage)).collect(Collectors.toList());
      }
    },
    Error() {
      @Override
      public Collection<FailMessage> filter(Collection<FailMessage> failures) {
        return failures.stream().filter((e) -> (e instanceof ErrorMessage)).collect(Collectors.toList());
      }
    },
    All() {
      @Override
      public Collection<FailMessage> filter(Collection<FailMessage> failures) {
        return failures;
      }
    };

    /**
     * Filter a collection of failure messages by type.
     *
     */
    public abstract Collection<FailMessage> filter(Collection<FailMessage> failures);
  }

  /**
   * Handle any schema violations.
   *
   * @param failures
   *          the failures representing the violations, non-null.
   * @param msg
   *          the adaptris message
   */
  void handle(Collection<FailMessage> failures, AdaptrisMessage msg) throws InterlokException;

  /**
   * Return a string representation of the failures.
   *
   * @param failures
   *          the failures representing the violations, non-null.
   * @return the string representation
   * @implNote The default implementation is return a new line separated list of the failures in the form {@code
   * '[line][column]:message'}
   */
  default String toString(Collection<FailMessage> failures) {
    StringWriter result = new StringWriter();
    try (PrintWriter pw = new PrintWriter(result)) {
      for (FailMessage m : failures) {
        pw.println(String.format("[%d][%d]:%s", m.getLineNumber(), m.getColumnIndex(), m.getMessage()));
      }
    }
    return result.toString();
  }

}
