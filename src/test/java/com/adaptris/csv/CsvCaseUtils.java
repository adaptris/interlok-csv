package com.adaptris.csv;

import com.adaptris.core.util.CloseableIterable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author mwarman
 */
public class CsvCaseUtils {

  private CsvCaseUtils(){}

  public static <E>  List<E> toList(Iterable<E> iter) {
    if (iter instanceof List) {
      return (List<E>) iter;
    }
    List<E> result = new ArrayList<E>();
    try (CloseableIterable<E> messages = ensureCloseable(iter)) {
      for (E msg : messages) {
        result.add(msg);
      }
    } catch (IOException e) {
    }
    return result;
  }

  private static <E> CloseableIterable<E> ensureCloseable(final Iterable<E> iter) {
    if (iter instanceof CloseableIterable) {
      return (CloseableIterable<E>) iter;
    }

    return new CloseableIterable<E>() {
      @Override
      public void close() throws IOException {
        // No-op
      }

      @Override
      public Iterator<E> iterator() {
        return iter.iterator();
      }
    };
  }
}
