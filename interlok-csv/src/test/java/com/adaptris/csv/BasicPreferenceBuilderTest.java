package com.adaptris.csv;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class BasicPreferenceBuilderTest {

  @Test
  public void testBuild() throws Exception {
    for (BasicPreferenceBuilder.Style s : BasicPreferenceBuilder.Style.values()) {
      BasicPreferenceBuilder b = new BasicPreferenceBuilder(s);
      assertNotNull(b.build());
    }
  }

}
