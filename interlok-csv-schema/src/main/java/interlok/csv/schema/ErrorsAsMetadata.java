package interlok.csv.schema;

import java.util.Collection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;

/**
 * Record all CSV Schema validation error messages as metadata.
 * <p>
 * This will discard all {@code WarningMessage} objects; if you want to include warnings and errors, then use {@link FailuresAsMetadata}
 * instead.
 * </p>
 */
@Slf4j
@NoArgsConstructor
@XStreamAlias("csv-schema-errors-as-metadata")
public class ErrorsAsMetadata extends ViolationsAsMetadata {

  @Override
  public void handle(Collection<FailMessage> failures, AdaptrisMessage msg) throws InterlokException {
    Collection<FailMessage> errors = FailureType.Error.filter(failures);
    if (errors.size() > 0) {
      log.trace("Recording {} errors against [{}]", errors.size(), getMetadataKey());
      msg.addMessageHeader(getMetadataKey(), toString(errors));
    }
  }

}
