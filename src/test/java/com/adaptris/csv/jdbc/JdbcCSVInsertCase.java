package com.adaptris.csv.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.services.jdbc.JdbcMapInsert;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.adaptris.util.TimeInterval;

public abstract class JdbcCSVInsertCase extends ExampleServiceCase {


  protected static final String CSV_CONTENT =
      "firstname, lastname, dob\n" +
          "alice,smith,2017-01-01\n" +
      "bob,smith,2017-01-02\n" +
      "carol,smith,2017-01-03";

  protected static final String INVALID_COLUMNS_CONTENT =
      "$firstname, $lastname, dob\n" +
          "alice,smith,2017-01-01\n" +
      "bob,smith,2017-01-02\n" +
      "carol,smith,2017-01-03";

  protected static final String CSV_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  protected static final String CSV_JDBC_URL = "jdbc:derby:memory:CSV_DB;create=true";
  protected static final String TABLE_NAME = "people";


  @Test
  public void testService_Init() throws Exception {
    JdbcMapInsert service = createService();
    try {
      LifecycleHelper.init(service);
      fail();
    } catch (CoreException expected) {

    }
    service.setTable("hello");
    LifecycleHelper.init(service);
  }


  protected static void doAssert(int expectedCount) throws Exception {
    Connection c = null;
    PreparedStatement p = null;
    try {
      c = createConnection();
      p = c.prepareStatement(String.format("SELECT * FROM %s", TABLE_NAME));
      ResultSet rs = p.executeQuery();
      int count = 0;
      while (rs.next()) {
        count++;
        assertEquals("smith", rs.getString("lastname"));
      }
      assertEquals(expectedCount, count);
      JdbcUtil.closeQuietly(rs);
    } finally {
      JdbcUtil.closeQuietly(p);
      JdbcUtil.closeQuietly(c);
    }
  }


  protected static Connection createConnection() throws Exception {
    Connection c = null;
    Class.forName(CSV_JDBC_DRIVER);
    c = DriverManager.getConnection(CSV_JDBC_URL);
    c.setAutoCommit(true);
    return c;
  }

  protected static void createDatabase() throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      c = createConnection();
      s = c.createStatement();
      executeQuietly(s, String.format("DROP TABLE %s", TABLE_NAME));
      s.execute(String.format("CREATE TABLE %s (firstname VARCHAR(128) NOT NULL, lastname VARCHAR(128) NOT NULL, dob VARCHAR(128))",
          TABLE_NAME));
    } finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected static void executeQuietly(Statement s, String sql) {
    try {
      s.execute(sql);
    } catch (Exception e) {
      ;
    }
  }

  protected abstract JdbcMapInsert createService();

  protected static <T> T configureForTests(T t) {
    JdbcMapInsert service = (JdbcMapInsert) t;
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl(CSV_JDBC_URL);
    connection.setDriverImp(CSV_JDBC_DRIVER);
    service.setConnection(connection);
    service.setTable(TABLE_NAME);
    return t;
  }

  protected static <T> T configureForExamples(T t) {
    JdbcMapInsert service = ((JdbcMapInsert) t).withTable("myTable");
    JdbcConnection connection = new JdbcConnection();
    connection.setConnectUrl("jdbc:mysql://localhost:3306/mydatabase");
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
    service.setConnection(connection);
    return t;
  }

}
