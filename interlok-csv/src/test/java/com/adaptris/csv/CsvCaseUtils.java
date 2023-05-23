package com.adaptris.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.interlok.util.CloseableIterable;

/**
 * @author mwarman
 */
public class CsvCaseUtils {

  private CsvCaseUtils() {
  }

  public static <E> List<E> toList(Iterable<E> iter) {
    if (iter instanceof List) {
      return (List<E>) iter;
    }
    List<E> result = new ArrayList<>();
    try (CloseableIterable<E> messages = CloseableIterable.ensureCloseable(iter)) {
      for (E msg : messages) {
        result.add(msg);
      }
    } catch (IOException e) {
    }
    return result;
  }

}
