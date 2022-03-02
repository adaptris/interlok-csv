package interlok.csv.schema;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.Collection;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;

/** If there are any warnings or errors present then throw an exception.
 *
 */
@XStreamAlias("csv-schema-fail-on-warnings-or-errors")
@NoArgsConstructor
@Slf4j
public class FailOnWarningOrError implements SchemaViolationHandler {

  @Override
  public void handle(Collection<FailMessage> failures, AdaptrisMessage msg) throws ServiceException {
    // Warnings and Failures, so size>0 == fail.
    if (failures.size() > 0) {
      throw new ServiceException(toString(failures));
    }
  }
}
