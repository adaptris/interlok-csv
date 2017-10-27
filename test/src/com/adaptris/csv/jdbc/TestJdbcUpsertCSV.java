package com.adaptris.csv.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcMapInsert;
import com.adaptris.core.util.JdbcUtil;

public class TestJdbcUpsertCSV extends JdbcCSVInsertCase {

  protected static final String UTC_0 = "1970-01-01";
  protected static final String SMITH = "smith";
  protected static final String ID_ELEMENT_VALUE = "firstname";
  protected static final String CAROL = "carol";
  protected static final String DOB = "2017-01-03";

  public TestJdbcUpsertCSV(String arg0) {
    super(arg0);
  }

  public void testService_Insert() throws Exception {
    createDatabase();
    JdbcUpsertCSV service = configureForTests(createService());
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_CONTENT);
    execute(service, msg);
    doAssert(3);
  }

  public void testService_Insert_InvalidColumn() throws Exception {
    createDatabase();
    JdbcUpsertCSV service = configureForTests(createService());
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMNS_CONTENT);
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testService_Update() throws Exception {
    createDatabase();
    populateDatabase();
    JdbcUpsertCSV service = configureForTests(createService());
    service.setIdField(ID_ELEMENT_VALUE);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_CONTENT);
    execute(service, msg);
    doAssert(3);
    checkDob(CAROL, DOB);
  }

  @Override
  protected JdbcMapInsert retrieveObjectForSampleConfig() {
    return configureForExamples(createService()).withId("id").withTable("myTable");
  }

  protected JdbcUpsertCSV createService() {
    return new JdbcUpsertCSV();
  }

  protected static void populateDatabase() throws Exception {
    Connection c = null;
    Statement s = null;
    try {
      c = createConnection();
      s = c.createStatement();
      s.execute(
          String.format("INSERT INTO %s (firstname, lastname, dob) VALUES ('%s', '%s' ,'%s')", TABLE_NAME, CAROL, SMITH, UTC_0));
    }
    finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }

  protected static void checkDob(String firstname, String dob) throws Exception {
    Connection c = null;
    Statement s = null;
    ResultSet rs = null;
    try {
      c = createConnection();
      s = c.createStatement();
      rs = s.executeQuery(String.format("SELECT * FROM %s WHERE firstname='%s'", TABLE_NAME, firstname));
      if (rs.next()) {
        assertEquals(dob, rs.getString("dob"));
      }
      else {
        fail("No Match for firstname: " + firstname);
      }
    }
    finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
    }
  }
}