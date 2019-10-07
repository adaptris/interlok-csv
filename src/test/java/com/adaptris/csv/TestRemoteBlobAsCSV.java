package com.adaptris.csv;

import static org.junit.Assert.assertEquals;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.csv.BasicPreferenceBuilder.Style;
import com.adaptris.interlok.cloud.RemoteBlob;

public class TestRemoteBlobAsCSV {

  @Test
  public void testRender() throws Exception {
    CsvBlobListRenderer render = new CsvBlobListRenderer();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    Collection<RemoteBlob> blobs = createBlobs(10);
    render.render(blobs, msg);
    System.err.println(msg.getContent());
    // Has a header row.
    assertEquals(11, IOUtils.readLines(new StringReader(msg.getContent())).size());
  }

  @Test(expected = CoreException.class)
  public void testRender_Fail() throws Exception {
    CsvBlobListRenderer render =
        new CsvBlobListRenderer().withPreferenceBuilder(new BasicPreferenceBuilder(Style.STANDARD_PREFERENCE));
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.OUTPUT).newMessage();
    Collection<RemoteBlob> blobs = createBlobs(10);
    render.render(blobs, msg);
  }

  private static Collection<RemoteBlob> createBlobs(int count) {
    List<RemoteBlob> result = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      result.add(new RemoteBlob.Builder().setBucket("bucket").setLastModified(new Date().getTime()).setName("File_" + i)
          .setSize(10L).build());
    }
    return result;
  }
}
