package com.adaptris.core.transform.csvjson;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

import org.supercsv.io.CsvMapReader;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.csv.OrderedCsvMapReader;
import com.adaptris.csv.PreferenceBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CsvJsonLinesTransformer implements CsvJsonTransformer {

  @Getter(AccessLevel.PRIVATE)
  private transient PreferenceBuilder preferenceBuilder;

  @Override
  public void transform(AdaptrisMessage msg) throws ServiceException {
    ObjectMapper mapper = new ObjectMapper();
    try (Reader reader = msg.getReader();
        CsvMapReader csvReader = new OrderedCsvMapReader(reader, getPreferenceBuilder().build());
        PrintWriter pw = new PrintWriter(msg.getWriter())) {
      String[] hdrs = csvReader.getHeader(true);
      for (Map<String, String> row; (row = csvReader.read(hdrs)) != null;) {
        String json = mapper.writeValueAsString(row);
        pw.println(json);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

}
