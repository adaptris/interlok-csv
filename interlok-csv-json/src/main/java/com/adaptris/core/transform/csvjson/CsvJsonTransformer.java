package com.adaptris.core.transform.csvjson;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

public interface CsvJsonTransformer {

  void transform(AdaptrisMessage msg) throws ServiceException;

}
