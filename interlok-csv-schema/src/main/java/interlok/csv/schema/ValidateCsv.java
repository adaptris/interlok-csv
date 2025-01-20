package interlok.csv.schema;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import com.adaptris.core.fs.FsHelper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.nationalarchives.csv.validator.api.java.CsvValidator;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;
import uk.gov.nationalarchives.csv.validator.api.java.Substitution;

/**
 * Validate a CSV file against a schema as defined by <a href="https://digital-preservation.github.io/csv-schema">CSV Schema</a>.
 */
@ComponentProfile(summary = "Validate a CSV file against a schema", since = "4.4.0", tag = "csv,schema")
@XStreamAlias("validate-csv-against-schema")
@DisplayOrder(order = { "schemaFile", "violationHandler", "failFast", "additionalDebug", "substitutions", "caseSensitive" })
@NoArgsConstructor
public class ValidateCsv extends ServiceImp {

  /**
   * The schema.
   */
  @Getter
  @Setter
  @NotBlank(message = "CSV Schema may not be blank")
  @InputFieldHint(expression = true)
  private String schemaFile;

  /**
   * What to do when the CSV file is considered invalid.
   *
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "fail on errors")
  @Valid
  private SchemaViolationHandler violationHandler;

  /**
   * Whether or not we fail immediately on the first error or report on all errors.
   * <p>
   * Using failFast implies that you will fail on the first {@code ErrorMessage}, warnings will still be emitted but only up until the first
   * error.
   * </p>
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "false")
  @AdvancedConfig
  private Boolean failFast;

  /**
   * Enable 'trace' mode when validating.
   * <p>
   * This actually causes a lot of output on stdout rather than delegating to a logger or similar, so for that reason we make it private for
   * now to avoid std-out clutter.
   * </p>
   */
  @Getter(AccessLevel.PRIVATE)
  @Setter(AccessLevel.PRIVATE)
  // @AdvancedConfig
  // @InputFieldDefault(value = "false")
  private Boolean additionalDebug;

  /**
   * Enable case sensitivity when performing external expression resolution.
   * <p>
   * Useful if you are <a href="https://digital-preservation.github.io/csv-schema/csv-schema-1.1.html#external-single-expressions">accessing
   * external resources</a> as part of your schema.
   * </p>
   */
  @Getter
  @Setter
  @AdvancedConfig()
  @InputFieldDefault(value = "false")
  private Boolean caseSensitive;

  /**
   * The Path substitutions to apply.
   *
   * <p>
   * Useful if you are <a href="https://digital-preservation.github.io/csv-schema/csv-schema-1.1.html#external-single-expressions">accessing
   * external resources</a> as part of your schema. This ultimately resolves to a list of {@code Substitution} objects which is passed into
   * the {@code CsvValidator#validate()} method. The 'key' is the from, with the 'value' being the to.
   * </p>
   */
  @Getter
  @Setter
  @AdvancedConfig
  private KeyValuePairSet substitutions;

  private transient List<Substitution> substitutionList;

  @Override
  protected void initService() throws CoreException {
    substitutionList = new ArrayList<>();
    substitutions().forEach((k) -> substitutionList.add(new Substitution(k.getKey(), k.getValue())));
  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      String schemaToUse = Args.notBlank(msg.resolve(getSchemaFile()), "csv schema");
      List<FailMessage> failures;
      // Option to "cache" the schema if it's a fixed one, i.e. Read it all into memory, and always
      // create a StringReader or something on it.
      // c.f. InputFieldExpression.isExpression()
      log.trace("Validating against {}", schemaToUse);
      File schemaFile = FsHelper.toFile(schemaToUse, new File(schemaToUse));
      try (Reader data = msg.getReader(); FileReader schema = new FileReader(schemaFile, StandardCharsets.UTF_8)) {
        failures = CsvValidator.validate(data, schema, failFast(), substitutionList, sensitivePathChecks(), trace());
      }
      violationHandler().handle(failures, msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  public ValidateCsv withSchema(String s) {
    setSchemaFile(s);
    return this;
  }

  public ValidateCsv withViolationHandler(SchemaViolationHandler handler) {
    setViolationHandler(handler);
    return this;
  }

  private KeyValuePairSet substitutions() {
    return ObjectUtils.defaultIfNull(getSubstitutions(), new KeyValuePairSet());
  }

  private boolean failFast() {
    return BooleanUtils.toBooleanDefaultIfNull(getFailFast(), false);
  }

  private boolean trace() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalDebug(), false);
  }

  private boolean sensitivePathChecks() {
    return BooleanUtils.toBooleanDefaultIfNull(getCaseSensitive(), false);
  }

  private SchemaViolationHandler violationHandler() {
    return ObjectUtils.defaultIfNull(getViolationHandler(), new FailOnError());
  }

}
