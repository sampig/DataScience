/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.PredicateUtils;

/**
 * According to the data exploration, only startDate, location, name, url, endDate, description will
 * be used. This program is used to calculate the number of events which:
 * <ul>
 * <li>contains both name and description. (xx1xx1)</li>
 * <li>contains name, description and location. (x11xx1)</li>
 * <li>contains both name and location. (x11xxx)</li>
 * <li>contains name, location, url and description. (x111x1)</li>
 * <li>other cases(there could be 64 situations).</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventAttributeRelation {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private String inputfile = DEFAULT_PATH + "/" + ConfigUtils.getEventDataFilename();

  private List<String> listAttributes =
      Arrays.asList("startDate", "location", "name", "url", "endDate", "description");
  // 111111

  private Map<String, List<String>> mapEvents = new HashMap<>(0);
  private Map<String, Integer> mapEventAttrs = new HashMap<>(0);

  private int[] countAttributeNum = new int[listAttributes.size() + 1];
  private Map<String, Integer> mapCountEventAttrs = new HashMap<>(0);

  /**
   * Parse the event data file.
   * 
   * @param inputfile
   */
  public void parseEventFile() {
    FileInputStream is;
    try {
      is = new FileInputStream(this.inputfile);
      // parse the file.
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + inputfile + "...");
      nxp.parse(is);
      // read every quad.
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
          continue;
        }
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        // save the events.
        if (!mapEvents.containsKey(nodeid)) {
          List<String> list = new ArrayList<>(0);
          mapEvents.put(nodeid, list);
        }
        String attr = PredicateUtils.getEventProperty(predicate);
        if (listAttributes.contains(attr) && !mapEvents.get(nodeid).contains(attr)) {
          mapEvents.get(nodeid).add(attr);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Calculate the number.
   */
  public void calculate() {
    System.out.println("\nCalculating...");
    for (Map.Entry<String, List<String>> entry : mapEvents.entrySet()) {
      List<String> list = entry.getValue();
      countAttributeNum[list.size()]++;
      int value = 0;
      for (int i = 0; i < listAttributes.size(); i++) {
        if (list.contains(listAttributes.get(i))) {
          int v = (int) Math.pow(10, (listAttributes.size() - 1 - i));
          value += v;
        }
      }
      mapEventAttrs.put(entry.getKey(), value);
    }
    for (Map.Entry<String, Integer> entry : mapEventAttrs.entrySet()) {
      String value = String.format("%06d", entry.getValue());
      Integer count = mapCountEventAttrs.get(value);
      mapCountEventAttrs.put(value, (count == null ? 1 : count + 1));
    }
  }

  /**
   * Print the result.
   */
  public void printResult() {
    System.out.println("\nResults:");
    for (int i = 0; i < countAttributeNum.length; i++) {
      System.out.println("Events with " + i + " attributes:" + countAttributeNum[i]);
    }
    System.out.println("Attributes: " + listAttributes);
    System.out.println(mapCountEventAttrs);
    // name and description. (xx1xx1)
    System.out.println("Name and description: ");
    Pattern pattern1 = Pattern.compile("\\d{2}1\\d{2}1");
    int count1 = 0;
    for (Map.Entry<String, Integer> entry : mapCountEventAttrs.entrySet()) {
      if (pattern1.matcher(entry.getKey()).matches()) {
        System.out.println(entry);
        count1 += entry.getValue();
      }
    }
    System.out.println("Total: " + count1);
    // name, description and location. (x11xx1)
    System.out.println("Name, description and location: ");
    Pattern pattern2 = Pattern.compile("\\d{1}11\\d{2}1");
    int count2 = 0;
    for (Map.Entry<String, Integer> entry : mapCountEventAttrs.entrySet()) {
      if (pattern2.matcher(entry.getKey()).matches()) {
        System.out.println(entry);
        count2 += entry.getValue();
      }
    }
    System.out.println("Total: " + count2);
    // name and location. (x11xxx)
    System.out.println("Name and location: ");
    Pattern pattern3 = Pattern.compile("\\d{1}11\\d{3}");
    int count3 = 0;
    for (Map.Entry<String, Integer> entry : mapCountEventAttrs.entrySet()) {
      if (pattern3.matcher(entry.getKey()).matches()) {
        System.out.println(entry);
        count3 += entry.getValue();
      }
    }
    System.out.println("Total: " + count3);
    // name, location, url and description. (x111x1)
    System.out.println("name, location, url and description: ");
    Pattern pattern4 = Pattern.compile("\\d{1}111\\d{1}1");
    int count4 = 0;
    for (Map.Entry<String, Integer> entry : mapCountEventAttrs.entrySet()) {
      if (pattern4.matcher(entry.getKey()).matches()) {
        System.out.println(entry);
        count4 += entry.getValue();
      }
    }
    System.out.println("Total: " + count4);
  }

  public static void main(String... strings) {
    EventAttributeRelation ear = new EventAttributeRelation();
    ear.parseEventFile();
    ear.calculate();
    ear.printResult();
  }

}
