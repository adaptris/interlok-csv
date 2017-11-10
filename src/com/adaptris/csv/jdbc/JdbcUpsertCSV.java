package com.adaptris.csv.jdbc;

import java.io.Reader;
import java.sql.Connection;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.supercsv.io.CsvMapReader;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcMapUpsert;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.csv.BasicPreferenceBuilder;
import com.adaptris.csv.OrderedCsvMapReader;
import com.adaptris.csv.PreferenceBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Convenience service for inserting/updating records in a database based on a CSV file.
 * 
 * <p>
 * <pre>
 * {@code 
   firstname, lastname, dob,id
   alice, smith, 2017-01-01,1234
   }
 * </pre> will effectively execute the following statement {@code INSERT INTO table (firstname,lastname,dob,id) VALUES (?,?,?,?)} or
 * {@code UPDATE table SET firstname=?, lastname=?, dob=? WHERE id = ?;} if {@code 1234} already exists as a row.
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
@ComponentProfile(summary = "Insert/Update a CSV file into a database", tag = "service,csv,jdbc", since = "3.6.5")
@XStreamAlias("csv-jdbc-upsert")
@DisplayOrder(order = {"table", "idField"})
public class JdbcUpsertCSV extends JdbcMapUpsert {

  @NotNull
  @AutoPopulated
  private PreferenceBuilder preferenceBuilder;

  public JdbcUpsertCSV() {
    setPreferenceBuilder(new BasicPreferenceBuilder(BasicPreferenceBuilder.Style.STANDARD_PREFERENCE));

  }

  /**
   * @return the PreferenceBuilder
   */
  public PreferenceBuilder getPreferenceBuilder() {
    return preferenceBuilder;
  }

  /**
   * @param b the PreferenceBuilder to set
   */
  public void setPreferenceBuilder(PreferenceBuilder b) {
    this.preferenceBuilder = Args.notNull(b, "preference-builder");
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Connection conn = null;
    log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
    try (Reader reader = msg.getReader();
        CsvMapReader csvReader = new OrderedCsvMapReader(reader, getPreferenceBuilder().build())) {
      conn = getConnection(msg);
      String[] hdrs = csvReader.getHeader(true);
      InsertWrapper wrapper = null;
      for (Map<String, String> row; (row = csvReader.read(hdrs)) != null;) {
        handleUpsert(conn, row);
      }
      commit(conn, msg);
    } catch (Exception e) {
      rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }


}
