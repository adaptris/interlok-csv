package com.adaptris.csv.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("csv-metadata-column-filter")
public class MetadataColumnFilter implements ColumnFilter {

  @XStreamImplicit(itemFieldName = "exclusion-key")
  @NotNull
  @AutoPopulated
  private List<String> exclusionKeys;

  @XStreamImplicit(itemFieldName = "inclusion-key")
  @NotNull
  @AutoPopulated
  private List<String> inclusionKeys;

  public MetadataColumnFilter() {
    setExclusionKeys(new ArrayList<String>());
    setInclusionKeys(new ArrayList<String>());
  }


  @Override
  public Set<String> getExcludeColumnNames(AdaptrisMessage msg) {
    Set<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    
    for(String key: getExclusionKeys()) {
      String value = msg.getMetadataValue(key);
      if(StringUtils.isNotEmpty(value)) {
        String[] columnNames = value.split(",");
        for(String column: columnNames) {
          result.add(column);
        }
      }
    }
    
    return result;
  }

  @Override
  public Set<String> getIncludeColumnNames(AdaptrisMessage msg) {
    Set<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    for (String key : getInclusionKeys()) {
      String value = msg.getMetadataValue(key);
      if (StringUtils.isNotEmpty(value)) {
        String[] columnNames = value.split(",");
        for (String column : columnNames) {
          result.add(column);
        }
      }
    }
    return result;
  }


  /**
   * @return the inclusionKeys
   */
  public List<String> getInclusionKeys() {
    return inclusionKeys;
  }

  /**
   * Set the keys that will be used to include columns in output.
   * 
   * @param keys the metadata keys to use for the include columns list. Each key may contain a comma separated list of
   *        column names.
   */
  public void setInclusionKeys(List<String> keys) {
    this.inclusionKeys = Args.notNull(keys, "inclusionKeys");
  }
  

  public List<String> getExclusionKeys() {
    return exclusionKeys;
  }

  /**
   * Set the keys that will be used to exclude columns from output.
   * 
   * @param keys the metadata keys to use for the filtered columns list. Each key may contain a comma separated list of
   *        column names to filter.
   */
  public void setExclusionKeys(List<String> keys) {
    this.exclusionKeys = Args.notNull(keys, "exclusionKeys");
  }
}
