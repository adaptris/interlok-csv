package com.adaptris.csv.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcMapInsert;

public class TestJdbcBatchInsertCSV extends JdbcCSVInsertCase {


  public TestJdbcBatchInsertCSV(String arg0) {
    super(arg0);
  }

  public void testAccumulate() throws Exception {
    int[] rc = {1, 2, Statement.EXECUTE_FAILED};
    try {
      BatchInsertCSV.accumulate(rc);
    } catch (SQLException expected) {

    }
    int[] rc2 = {1, 2, Statement.SUCCESS_NO_INFO};
    assertEquals(3, BatchInsertCSV.accumulate(rc2));
  }

  public void testService() throws Exception {
    createDatabase();
    BatchInsertCSV service = configureForTests(createService()).withRowsAffectedMetadataKey("rowsAffected");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_CONTENT);
    execute(service, msg);
    assertTrue(msg.headersContainsKey("rowsAffected"));
    assertEquals("3", msg.getMetadataValue("rowsAffected"));
    doAssert(3);
  }


  public void testService_InvalidColumns() throws Exception {
    createDatabase();
    BatchInsertCSV service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_COLUMNS_CONTENT);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
  }

  @Override
  protected JdbcMapInsert retrieveObjectForSampleConfig() {
    return configureForExamples(createService()).withTable("myTable");
  }

  public void testService_LowBatchWindow() throws Exception {
    createDatabase();
    BatchInsertCSV service = configureForTests(createService());
    service.setBatchWindow(1);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_CONTENT);
    execute(service, msg);
    doAssert(3);
  }

  @Override
  protected BatchInsertCSV createService() {
    return new BatchInsertCSV();
  }
}
