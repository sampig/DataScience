/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A utility for dealing with vectors.
 * 
 * @author Chenfeng Zhu
 *
 */
public class VectorUtils {

  /**
   * Used for sorting.
   * 
   * @author Chenfeng Zhu
   *
   */
  public enum SortBy {
    ASC, DESC
  }

  /**
   * Normalize the vector.
   * 
   * @param map
   * @return
   */
  public static Map<String, Double> normalizeVector(Map<String, Integer> map) {
    Map<String, Double> newmap = new HashMap<>(0);
    double length = 0;
    for (Entry<String, Integer> e : map.entrySet()) {
      length += e.getValue();
    }
    length = Math.sqrt(length);
    for (Entry<String, Integer> e : map.entrySet()) {
      newmap.put(e.getKey(), e.getValue() / length);
    }
    return newmap;
  }

  /**
   * Normalize the vector.
   * 
   * @param values
   * @return
   */
  public static double[] normalizeVector(double[] values) {
    double length = 0;
    for (double v : values) {
      length += v;
    }
    length = Math.sqrt(length);
    for (int i = 0; i < values.length; i++) {
      values[i] = values[i] / length;
    }
    return values;
  }

  /**
   * Sort the map by its value ordered by ascending(default) or descending.
   * 
   * @param unsortMap
   * @param by
   * @return
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap,
      final SortBy by) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        switch (by) {
          case ASC:
            return (o1.getValue()).compareTo(o2.getValue());
          case DESC:
            return (o2.getValue()).compareTo(o1.getValue());
        }
        return (o1.getValue()).compareTo(o2.getValue());
      }
    });
    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Get a list of random numbers.
   * 
   * @param number
   * @param size
   * @return
   */
  public static List<Integer> getRandomNumbers(int number, int size) {
    List<Integer> list = new ArrayList<>(0);
    List<Integer> all = new ArrayList<>(0);
    for (int i = 0; i < size; i++) {
      all.add(i);
    }
    for (int i = 0; i < number; i++) {
      int j = (int) (Math.random() * all.size());
      list.add(all.get(j));
      all.remove(j);
    }
    return list;
  }

}
