package interlok.csv.schema;

import java.util.Collection;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;

/**
 * Record all CSV Schema validation failure messages as metadata.
 *
 */
@Slf4j
@NoArgsConstructor
@XStreamAlias("csv-schema-failures-as-metadata")
public class FailuresAsMetadata extends ViolationsAsMetadata {

  @Override
  public void handle(Collection<FailMessage> failures, AdaptrisMessage msg) throws InterlokException {
    if (failures.size() > 0) {
      log.trace("Recording {} failures against [{}]", failures.size(), getMetadataKey());
      msg.addMessageHeader(getMetadataKey(), toString(failures));
    }
  }

}
