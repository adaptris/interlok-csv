package com.adaptris.csv;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class BasicPreferenceBuilderTest {

  @Test
  public void testBuild() throws Exception {
    for (BasicPreferenceBuilder.Style s : BasicPreferenceBuilder.Style.values()) {
      BasicPreferenceBuilder b = new BasicPreferenceBuilder(s);
      assertNotNull(b.build());
    }
  }
}
