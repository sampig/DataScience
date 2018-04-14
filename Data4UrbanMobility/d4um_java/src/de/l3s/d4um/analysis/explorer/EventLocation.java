/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.explorer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.FileUtils;
import de.l3s.d4um.utils.PredicateUtils;

/**
 * Find the relationship between events and their properties.
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventLocation {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();
  private String inputfile = DEFAULT_PATH + "/" + ConfigUtils.getEventDataFilename();
  private String outputfile = DEFAULT_PATH + "/ede_event_duplicate_location.out";

  private String strLocation = "location";

  private Map<String, List<String[]>> mapEvents = new HashMap<>(0);
  private Map<String, Map<String, Integer>> mapEventPropertyCount = new HashMap<>(0);
  // private Map<String, Integer> mapEventLocation = new HashMap<>(0);
  private Map<String, String> mapEventLocation = new HashMap<>(0);

  /**
   * Parse the event data file.
   */
  public void parse() {
    FileInputStream is;
    try {
      is = new FileInputStream(inputfile);
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + inputfile + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String obj = nx[2].toString(); // .getLabel();
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
          continue;
        }
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        String property = PredicateUtils.getEventProperty(predicate).toLowerCase();
        // save the events.
        if (!mapEventPropertyCount.containsKey(nodeid)) {
          Map<String, Integer> map = new HashMap<>(0);
          mapEventPropertyCount.put(nodeid, map);
        }
        if (!mapEvents.containsKey(nodeid)) {
          List<String[]> list = new ArrayList<>(0);
          mapEvents.put(nodeid, list);
        }
        List<String[]> list = mapEvents.get(nodeid);
        list.add(new String[] {property, obj});
        Map<String, Integer> map = mapEventPropertyCount.get(nodeid);
        Integer count = map.get(property);
        map.put(property, (count == null ? 1 : count + 1));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Find the duplication location properties.
   */
  public void findDuplicateLocation() {
    if (mapEventPropertyCount.size() <= 0) {
      this.parse();
    }
    System.out.println("Finding duplicate locations...");
    for (Map.Entry<String, Map<String, Integer>> entry : mapEventPropertyCount.entrySet()) {
      Map<String, Integer> map = entry.getValue();
      if (!map.containsKey(strLocation)) {
        continue;
      }
      Integer count = map.get(strLocation);
      if (count > 1) {
        // System.out.println(entry.getKey());
        String str = count.toString() + ": ";
        List<String[]> list = mapEvents.get(entry.getKey());
        for (String[] ss : list) {
          if (ss[0].equalsIgnoreCase(strLocation)) {
            str += ss[1] + "; ";
          }
        }
        mapEventLocation.put(entry.getKey(), str);
      }
    }
    FileUtils.writeToFile(outputfile, mapEventLocation);
  }

  public static void main(String... strings) {
    EventLocation el = new EventLocation();
    el.parse();
    el.findDuplicateLocation();
  }

}
