package com.adaptris.csv.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.jdbc.JdbcConnection;
import com.adaptris.core.services.jdbc.JdbcDataQueryService;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.TimeInterval;

public class CsvResultSetTranslatorTest extends ServiceCase {

  private static final GuidGenerator GUID = new GuidGenerator();

  public CsvResultSetTranslatorTest(String name) {
    super(name);
  }


  public void setUp() throws Exception {
    super.setUp();
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    JdbcDataQueryService service = new JdbcDataQueryService();
    try {
      JdbcConnection connection = new JdbcConnection("jdbc:mysql://localhost:3306/mydatabase", "com.mysql.jdbc.Driver");
      connection.setConnectionAttempts(2);
      connection.setConnectionRetryInterval(new TimeInterval(3L, "SECONDS"));
      service.setConnection(connection);
      CsvResultSetTranslator rst = new CsvResultSetTranslator();
      rst.setExcludeColumns("some,columns,to,exclude");
      service.setResultSetTranslator(rst);
      service.setStatement("SELECT StringColumn1, StringColumn2 FROM tablename WHERE ID = 123");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return service;
  }

  public void testResultSetToCSV() throws Exception {
    String url = "jdbc:derby:memory:" + GUID.safeUUID() + ";create=true";
    populateDB(url, 10);
    JdbcConnection con = new JdbcConnection();
    con.setConnectUrl(url);
    con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");

    JdbcDataQueryService s = new JdbcDataQueryService();
    s.setConnection(con);
    s.setStatement("select * from data");
    s.setResultSetTranslator(new CsvResultSetTranslator());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(s, msg);
    List<String> lines = IOUtils.readLines(msg.getInputStream());
    assertEquals(11, lines.size());
  }
  
  public void testResultSetToCSVWith1Exclusion() throws Exception {
    String url = "jdbc:derby:memory:" + GUID.safeUUID() + ";create=true";
    populateDB(url, 10);
    JdbcConnection con = new JdbcConnection();
    con.setConnectUrl(url);
    con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");

    CsvResultSetTranslator rst = new CsvResultSetTranslator();
    rst.setExcludeColumns("ADAPTER_UNIQUE_ID");
    
    JdbcDataQueryService s = new JdbcDataQueryService();
    s.setConnection(con);
    s.setStatement("select * from data");
    s.setResultSetTranslator(rst);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(s, msg);
    List<String> lines = IOUtils.readLines(msg.getInputStream());
    assertEquals(11, lines.size());
    assertFalse(lines.get(0).contains("ADAPTER_UNIQUE_ID"));
    assertEquals(2, lines.get(1).split(",").length);
  }
  
  public void testResultSetToCSVWith2Exclusions() throws Exception {
    String url = "jdbc:derby:memory:" + GUID.safeUUID() + ";create=true";
    populateDB(url, 10);
    JdbcConnection con = new JdbcConnection();
    con.setConnectUrl(url);
    con.setDriverImp("org.apache.derby.jdbc.EmbeddedDriver");

    CsvResultSetTranslator rst = new CsvResultSetTranslator();
    rst.setExcludeColumns("ADAPTER_UNIQUE_ID,MESSAGE_TRANSLATOR_TYPE");
    
    JdbcDataQueryService s = new JdbcDataQueryService();
    s.setConnection(con);
    s.setStatement("select * from data");
    s.setResultSetTranslator(rst);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(s, msg);
    List<String> lines = IOUtils.readLines(msg.getInputStream());
    assertEquals(11, lines.size());
    assertFalse(lines.get(0).contains("ADAPTER_UNIQUE_ID"));
    assertFalse(lines.get(0).contains("MESSAGE_TRANSLATOR_TYPE"));
    assertEquals(1, lines.get(1).split(",").length);
  }

  @Override
  protected String createBaseFileName(Object o) {
    JdbcDataQueryService sc = (JdbcDataQueryService) o;
    String name = super.createBaseFileName(o) + "-" + sc.getResultSetTranslator().getClass().getSimpleName();
    return name;
  }

  private void populateDB(String url, int count) throws Exception {
    Connection c = null;
    Statement s = null;
    PreparedStatement insert = null;
    try {
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
      c = DriverManager.getConnection(url);
      c.setAutoCommit(true);
      s = c.createStatement();
      try {
        s.execute("DROP TABLE data");
      } catch (Exception e) {
        // Ignore exceptions from the drop
        ;
      }
      s.execute("CREATE TABLE data " + "(adapter_unique_id VARCHAR(128) NOT NULL, "
          + "adapter_version VARCHAR(128) NOT NULL, "
          + "message_translator_type VARCHAR(128) NOT NULL)");
      insert = c.prepareStatement(
          "INSERT INTO data "
          + "(adapter_unique_id, adapter_version, message_translator_type) " + "values (?, ?, ?)");
      for (int i = 0; i < count; i++) {
        insert.clearParameters();
        insert.setString(1, GUID.getUUID());
        insert.setString(2, GUID.getUUID());
        insert.setString(3, GUID.getUUID());
        insert.executeUpdate();
      }
    } finally {
      JdbcUtil.closeQuietly(s);
      JdbcUtil.closeQuietly(c);
      JdbcUtil.closeQuietly(insert);
    }
  }
}
