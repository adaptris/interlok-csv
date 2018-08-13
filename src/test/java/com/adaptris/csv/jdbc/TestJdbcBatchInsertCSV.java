package com.adaptris.csv.jdbc;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.jdbc.JdbcMapInsert;

public class TestJdbcBatchInsertCSV extends JdbcCSVInsertCase {


  public TestJdbcBatchInsertCSV(String arg0) {
    super(arg0);
  }

  public void testService() throws Exception {
    createDatabase();
    BatchInsertCSV service = configureForTests(createService());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CSV_CONTENT);
    execute(service, msg);
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

  protected BatchInsertCSV createService() {
    return new BatchInsertCSV();
  }
}
