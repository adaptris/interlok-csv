package interlok.csv.schema;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Collection;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;

/** Ignore all failure messages from schema validation.
 *
 */
@XStreamAlias("csv-schema-ignore-violations")
@NoArgsConstructor
@Slf4j
public class IgnoreViolations implements SchemaViolationHandler {

  @Override
  public void handle(Collection<FailMessage> failures, AdaptrisMessage msg) throws ServiceException {
    log.trace("Ignoring {} failures/errors", failures.size());
  }
}
