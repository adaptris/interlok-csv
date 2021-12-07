package interlok.csv.schema;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public abstract class ViolationsAsMetadata implements SchemaViolationHandler {

  /** The metadata key to store failures against.
   *
   */
  @NotBlank(message = "metadata key may not be blank")
  @Getter
  @Setter
  private String metadataKey;

  @SuppressWarnings("unchecked")
  public <T extends ViolationsAsMetadata> T withMetadataKey(String key) {
    setMetadataKey(key);
    return (T) this;
  }
}
