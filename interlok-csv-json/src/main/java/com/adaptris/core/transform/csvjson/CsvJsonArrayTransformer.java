package com.adaptris.core.transform.csvjson;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.supercsv.io.CsvMapReader;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.csv.OrderedCsvMapReader;
import com.adaptris.csv.PreferenceBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CsvJsonArrayTransformer implements CsvJsonTransformer {

  @Getter(AccessLevel.PRIVATE)
  private transient PreferenceBuilder preferenceBuilder;

  @Override
  public void transform(AdaptrisMessage msg) throws ServiceException {
    ObjectMapper mapper = new ObjectMapper();
    try (Reader reader = msg.getReader();
        CsvMapReader csvReader = new OrderedCsvMapReader(reader, getPreferenceBuilder().build());
        Writer w = msg.getWriter();
        JsonGenerator generator = mapper.getFactory().createGenerator(w).useDefaultPrettyPrinter()) {
      String[] hdrs = csvReader.getHeader(true);
      generator.writeStartArray();
      for (Map<String, String> row; (row = csvReader.read(hdrs)) != null;) {
        generator.writeObject(row);
      }
      generator.writeEndArray();
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

}
