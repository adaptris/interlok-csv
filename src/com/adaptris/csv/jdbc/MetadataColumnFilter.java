package com.adaptris.csv.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("csv-metadata-column-filter")
public class MetadataColumnFilter implements ColumnFilter {

  @XStreamImplicit(itemFieldName = "metadata-key")
  private List<String> metadataKeys;

  public MetadataColumnFilter() {
    setMetadataKeys(new ArrayList<String>());
  }
  
  public List<String> getMetadataKeys() {
    return metadataKeys;
  }

  /**
   * @param metadataKeys the metadata keys to use for the filtered columns list. Each key may contain a comma separated list of column names to filter.
   */
  public void setMetadataKeys(List<String> metadataKeys) {
    this.metadataKeys = metadataKeys;
  }

  @Override
  public List<String> getFilteredColumnNames(AdaptrisMessage msg) {
    List<String> result = new ArrayList<String>();
    
    for(String key: getMetadataKeys()) {
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
  

}
