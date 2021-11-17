package com.adaptris.csv;

import javax.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvListWriter;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.csv.BasicPreferenceBuilder.Style;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.util.CloseableIterable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Render a list of {@link RemoteBlob} as a CSV
 * 
 * @config remote-blob-list-as-csv
 */
@XStreamAlias("remote-blob-list-as-csv")
@ComponentProfile(summary = "Render a list of RemoteBlob objects as a CSV", since = "3.9.2", tag = "cloud,aws,jclouds,blob")
public class CsvBlobListRenderer implements BlobListRenderer {

  @Valid
  @InputFieldDefault(value = "csv-basic-preference-builder")
  private PreferenceBuilder preferenceBuilder;

  public CsvBlobListRenderer() {
  }

  @Override
  public void render(Iterable<RemoteBlob> blobs, InterlokMessage msg) throws InterlokException {
    boolean first = true;
    try (CloseableIterable<RemoteBlob> list = CloseableIterable.ensureCloseable(blobs);
        CsvListWriter csvWriter = new CsvListWriter(msg.getWriter(), preferenceBuilder().build())) {
      for (RemoteBlob blob : list) {
        if (first) {
          csvWriter.writeHeader("bucket", "name", "size", "lastModified");
          first = false;
        }
        csvWriter.write(StringUtils.defaultIfBlank(blob.getBucket(), ""), blob.getName(), blob.getSize(), blob.getLastModified());
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  /**
   * @return the formatBuilder
   */
  public PreferenceBuilder getPreferenceBuilder() {
    return preferenceBuilder;
  }

  /**
   * @param prefs the CSV Preferences to set
   */
  public void setPreferenceBuilder(PreferenceBuilder prefs) {
    this.preferenceBuilder = prefs;
  }

  private PreferenceBuilder preferenceBuilder() {
    return ObjectUtils.defaultIfNull(getPreferenceBuilder(), new BasicPreferenceBuilder(Style.STANDARD_PREFERENCE));
  }

  public CsvBlobListRenderer withPreferenceBuilder(PreferenceBuilder prefs) {
    setPreferenceBuilder(prefs);
    return this;
  }
}
