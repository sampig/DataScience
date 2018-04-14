/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A utility for dealing with predicates.
 * 
 * @author Chenfeng Zhu
 *
 */
public class PredicateUtils {

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
   * The types of schema.org's events.
   */
  public final static List<String> EVENT_TYPES =
      Arrays.asList("Event", "BusinessEvent", "ChildrensEvent", "ComedyEvent", "CourseInstance",
          "DanceEvent", "DeliveryEvent", "EducationEvent", "ExhibitionEvent", "Festival",
          "FoodEvent", "LiteraryEvent", "MusicEvent", "PublicationEvent", "SaleEvent",
          "ScreeningEvent", "SocialEvent", "SportsEvent", "TheaterEvent", "VisualArtsEvent");

  public final static String TYPE_UNKNOWN = "unknown";

  private static final Pattern IPV4_ADDRESS_PATTERN = Pattern.compile("(?:\\d{1,3}\\.){3}\\d{1,3}");

  /**
   * Get the Pay-Level domain of the input URL.
   * 
   * @param url
   * @return
   */
  public static String getPLD(String url) {
    String pld = null;
    IPV4_ADDRESS_PATTERN.matcher(url);
    pld = url.substring(url.indexOf("://") + 3);
    pld = pld.substring(0, pld.indexOf("/"));
    return pld;
  }

  /**
   * Check whether the predicate is from schema.org or not.
   * 
   * @param predicate
   * @return
   */
  public static boolean isSchemaOrg(String predicate) {
    if (predicate == null || !predicate.contains("schema.org")) {
      return false;
    }
    return true;
  }

  /**
   * Get the type of the predicate using the schema.org vocabulary.
   * 
   * @param predicate
   * @return
   */
  public static String getSchemaOrgType(String predicate) {
    if (!isSchemaOrg(predicate)) {
      return null;
    }
    String type = null;
    type = predicate.substring(predicate.indexOf("/", 8) + 1);
    if (!type.contains("/")) {
      return TYPE_UNKNOWN;
    }
    type = type.substring(0, type.indexOf("/"));
    return type;
  }

  /**
   * Fix the errors about schema.org.
   * 
   * @param original
   * @return
   */
  public static String fixSchemaOrg(String original) {
    String newStr = null;
    newStr = original.replace("https://", "http://");
    newStr = newStr.replace("www.schema.org", "schema.org");
    return newStr;
  }

  /**
   * Check whether the predicate is event-related or not.
   * 
   * @param predicate
   * @return
   */
  public static boolean isSchemaOrgEvent(String predicate) {
    if (!isSchemaOrg(predicate) || !EVENT_TYPES.contains(getSchemaOrgType(predicate))) {
      return false;
    }
    return true;
  }

  /**
   * Get the attribute of the normal entity.
   * 
   * @param predicate
   * @return
   */
  public static String getNormalProperty(String predicate) {
    if (predicate == null || !predicate.contains("/")) {
      return null;
    }
    return predicate.substring(predicate.lastIndexOf("/") + 1);
  }

  /**
   * Get the attribute of the event.
   * 
   * @param predicate
   * @return
   */
  public static String getEventProperty(String predicate) {
    if (!isSchemaOrgEvent(predicate)) {
      return null;
    }
    return predicate.substring(predicate.lastIndexOf("/") + 1);
  }

  /**
   * Get the type of the event.
   * 
   * @param predicate
   * @return
   */
  public static String getEventType(String predicate) {
    if (!isSchemaOrgEvent(predicate)) {
      return null;
    }
    String temp = predicate.substring(0, predicate.lastIndexOf("/"));
    return temp.substring(temp.lastIndexOf("/") + 1);
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

}
