package com.adaptris.csv.jdbc;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.ArrayUtils;
import org.supercsv.io.CsvMapReader;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcMapInsert;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.OrderedCsvMapReader;
import com.adaptris.csv.PreferenceBuilder;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Convenience service for inserting a CSV file into a database.
 * 
 * <p>
 * This creates insert statements based on the CSV file. The actual insert statement will only be generated once based on the first
 * CSV line and executed the appropriate number of times.
 * </p>
 * <pre>
 * {@code 
   firstname, lastname, dob
   alice, bob, 2017-01-01
   bob, smith, 2017-01-01
   carol, smith,2017-01-01
   }
 * </pre> will effectively execute the following statement {@code INSERT INTO table (firstname,lastname,dob) VALUES (?,?,?)} 3
 * times; batching as required using {@link PreparedStatement#addBatch()} / {@link PreparedStatement#executeBatch()}.
 * </p>
 * <p>
 * Note that no parsing/assertion of the column names will be done, so if they are invalid SQL columns then it's going to be fail.
 * </p>
 * 
 * @config csv-jdbc-batch-insert
 * @since 3.6.5
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Insert a CSV file into a database", tag = "service,csv,jdbc", since = "3.6.5")
@XStreamAlias("csv-jdbc-batch-insert")
@DisplayOrder(order = {"table", "batchWindow"})
public class BatchInsertCSV extends JdbcMapInsert {

  private static final InheritableThreadLocal<AtomicInteger> counter = new InheritableThreadLocal<AtomicInteger>() {
    @Override
    protected synchronized AtomicInteger initialValue() {
      return new AtomicInteger();
    }
  };

  public static final int DEFAULT_BATCH_WINDOW = 1024;

  @AdvancedConfig
  @InputFieldDefault(value = "1024")
  private Integer batchWindow = null;
  @NotNull
  @AutoPopulated
  private PreferenceBuilder preferenceBuilder;

  public BatchInsertCSV() {
    setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.STANDARD_PREFERENCE));

  }


  /**
   * @return the formatBuilder
   */
  public PreferenceBuilder getPreferenceBuilder() {
    return preferenceBuilder;
  }

  /**
   * @param formatBuilder the formatBuilder to set
   */
  public void setPreferenceBuilder(PreferenceBuilder formatBuilder) {
    this.preferenceBuilder = Args.notNull(formatBuilder, "preference-builder");
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Connection conn = null;
    PreparedStatement stmt = null;
    int rowsAffected = 0;
    log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
    try (Reader reader = msg.getReader();
        CsvMapReader csvReader = new OrderedCsvMapReader(reader, getPreferenceBuilder().build())) {

      conn = getConnection(msg);
      String[] hdrs = csvReader.getHeader(true);
      InsertWrapper wrapper = null;
      for (Map<String, String> row; (row = csvReader.read(hdrs)) != null;) {
        if (wrapper == null) {
          wrapper = new InsertWrapper(table(msg), row);
          log.trace("Generated [{}]", wrapper.statement());
          stmt = prepareStatement(conn, wrapper.statement());
        }
        wrapper.addParams(stmt, row);
        rowsAffected += execute(stmt);
      }
      rowsAffected += finish(stmt);
      addUpdatedMetadata(rowsAffected, msg);
      JdbcUtil.commit(conn, msg);
    } catch (Exception e) {
      JdbcUtil.rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
      JdbcUtil.closeQuietly(stmt);
    }
  }


  private int execute(PreparedStatement insert) throws SQLException {
    int count = counter.get().incrementAndGet();
    insert.addBatch();
    if (count % batchWindow() == 0) {
      log.trace("BatchWindow reached, executeBatch()");
      return executeBatch(insert);
    }
    return 0;
  }

  private int finish(PreparedStatement insert) throws SQLException {
    int rowsAffected = executeBatch(insert);
    counter.set(new AtomicInteger());
    return rowsAffected;
  }

  private int executeBatch(PreparedStatement insert) throws SQLException {
    int[] rc = insert.executeBatch();
    return accumulate(rc);
  }

  protected static int accumulate(int[] rc) throws SQLException {
    int rowsAffected = 0;
    List<Integer> result = Arrays.asList(ArrayUtils.toObject(rc));
    if (result.contains(Statement.EXECUTE_FAILED)) {
      throw new SQLException("Batch Execution Failed.");
    }
    for (int i : rc) {
      // Not Statement.EXECUTE_FAILED or SUCCESS_NO_INFO
      if (i >= 0) {
        rowsAffected += i;
      }
    }
    return rowsAffected;
  }
  /**
   * @return the batchWindow
   */
  public Integer getBatchWindow() {
    return batchWindow;
  }

  /**
   * Set the batch window for operations.
   * 
   * @param i the batchWindow to set; default is {@value #DEFAULT_BATCH_WINDOW} if not specified.
   */
  public void setBatchWindow(Integer i) {
    this.batchWindow = i;
  }

  int batchWindow() {
    return NumberUtils.toIntDefaultIfNull(getBatchWindow(), DEFAULT_BATCH_WINDOW);
  }

}
