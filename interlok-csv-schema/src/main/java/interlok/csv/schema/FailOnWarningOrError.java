package interlok.csv.schema;

import java.util.Collection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.NoArgsConstructor;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;

/**
 * If there are any warnings or errors present then throw an exception.
 *
 */
@XStreamAlias("csv-schema-fail-on-warnings-or-errors")
@NoArgsConstructor
public class FailOnWarningOrError implements SchemaViolationHandler {

  @Override
  public void handle(Collection<FailMessage> failures, AdaptrisMessage msg) throws ServiceException {
    // Warnings and Failures, so size>0 == fail.
    if (failures.size() > 0) {
      throw new ServiceException(toString(failures));
    }
  }

}
