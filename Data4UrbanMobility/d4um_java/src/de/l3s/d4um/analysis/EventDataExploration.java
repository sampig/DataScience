/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.PredicateUtils;

/**
 * Explore the event data.
 * <ul>
 * <li>Parse the event data file in N-Quads format.</li>
 * <li>Make a summary(Total quantity, distribution, etc.).</li>
 * <li>Write these results into files.</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventDataExploration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();
  private String outputfilepath = DEFAULT_PATH;

  private String inputfile = DEFAULT_PATH + "/" + ConfigUtils.getEventDataFilename();
  private String outputPrefix = "ede_event_";

  private String outputAttrEvents = outputfilepath + "/" + outputPrefix + "attricount.out";
  private String outputPredicates = outputfilepath + "/" + outputPrefix + "predicates.out";
  private String outputProperties = outputfilepath + "/" + outputPrefix + "properties.out";
  private String outputEventtypes = outputfilepath + "/" + outputPrefix + "types.out";
  // private String outputOthertypes = filepath + "/ede_event_others.out";

  private Map<String, Map<String, String>> mapEvents = new HashMap<>(0);
  // private Map<String, Map<String, String>> mapOthers = new HashMap<>(0);
  private Map<String, Integer> mapAttrEvents = new HashMap<>(0);
  private Map<String, Integer> mapEventTypes = new HashMap<>(0);
  private Map<String, Integer> mapPredicates = new HashMap<>(0);
  private Map<String, Integer> mapProperties = new HashMap<>(0);
  private Map<String, Integer> mapOtherCount = new HashMap<>(0);
  private int count = 0;

  /**
   * Set the input file.
   * 
   * @param input
   */
  public void setInput(String input) {
    this.inputfile = input;
    System.out.println("Set input file: " + this.inputfile);
  }

  /**
   * Set the prefix for the output files.
   * 
   * @param prefix
   */
  public void setOutputPrefix(String prefix) {
    this.outputPrefix = prefix;
    System.out.println("Set output prefix: " + this.outputPrefix);
    this.outputAttrEvents = outputfilepath + "/" + outputPrefix + "attricount.out";
    this.outputPredicates = outputfilepath + "/" + outputPrefix + "predicates.out";
    this.outputProperties = outputfilepath + "/" + outputPrefix + "properties.out";
    this.outputEventtypes = outputfilepath + "/" + outputPrefix + "types.out";
  }

  /**
   * Set the path for the output files.
   * 
   * @param path
   */
  public void setOutputPath(String path) {
    if (path != null && path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    this.outputfilepath = path;
    System.out.println("Set output path: " + this.outputfilepath);
    this.outputAttrEvents = outputfilepath + "/" + outputPrefix + "attricount.out";
    this.outputPredicates = outputfilepath + "/" + outputPrefix + "predicates.out";
    this.outputProperties = outputfilepath + "/" + outputPrefix + "properties.out";
    this.outputEventtypes = outputfilepath + "/" + outputPrefix + "types.out";
  }

  /**
   * Parse the event data file.
   * 
   * @param inputfile
   */
  public void parseEventFile(String inputfile) {
    if (inputfile == null) {
      inputfile = this.inputfile;
    }
    FileInputStream is;
    try {
      is = new FileInputStream(inputfile);
      // parse the file.
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + inputfile + "...");
      nxp.parse(is);
      // read every quad.
      for (Node[] nx : nxp) {
        count++;
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String obj = nx[2].getLabel();// nx[2].toString();
        // save other types.
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
          // if (!mapOthers.containsKey(nodeid)) {
          // Map<String, String> map = new HashMap<>(0);
          // mapOthers.put(nodeid, map);
          // }
          // Map<String, String> map = mapOthers.get(nodeid);
          // map.put(predicate, obj);
          Integer countOther = mapOtherCount.get(nodeid);
          mapOtherCount.put(nodeid, (countOther == null ? 1 : countOther + 1));
          continue;
        }
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        // save the events.
        if (!mapEvents.containsKey(nodeid)) {
          Map<String, String> map = new HashMap<>(0);
          mapEvents.put(nodeid, map);
        }
        Map<String, String> map = mapEvents.get(nodeid);
        map.put(predicate, obj);
        // count the number of attributes of each event.
        Integer countAttr = mapAttrEvents.get(nodeid);
        mapAttrEvents.put(nodeid, (countAttr == null ? 1 : countAttr + 1));
        // count the number of predicates.
        Integer countPredicate = mapPredicates.get(predicate);
        mapPredicates.put(predicate, (countPredicate == null ? 1 : countPredicate + 1));
        // count the number of properties.
        String attr = PredicateUtils.getEventProperty(predicate);
        Integer countProperty = mapProperties.get(attr);
        mapProperties.put(attr, (countProperty == null ? 1 : countProperty + 1));
        // count the number of event types.
        String eventType = PredicateUtils.getEventType(predicate);
        Integer countEventType = mapEventTypes.get(eventType);
        mapEventTypes.put(eventType, (countEventType == null ? 1 : countEventType + 1));
      }
      System.out.println("The total number of lines: " + nxp.lineNumber());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Print the summary of the data file after parsing.
   * 
   * @param inputfile
   */
  public void summary(String inputfile) {
    if (count <= 0) {
      this.parseEventFile(inputfile);
    }
    System.out.println("The total number of quads: " + count);
    System.out.println("The total number of events: " + mapEvents.size());
    System.out.println("The total number of event types: " + mapEventTypes.size());
    System.out.println("The total number of predicates: " + mapPredicates.size());
    System.out.println("The total number of properties: " + mapProperties.size());
    System.out.println("The total number of other types: " + mapOtherCount.size());
    //
    System.out.println("Samples:");
    int count = 0;
    for (Map.Entry<String, Map<String, String>> entry : mapEvents.entrySet()) {
      if (count >= 3) {
        break;
      }
      System.out.println(entry.getKey() + ": " + entry.getValue());
      count++;
    }
  }

  /**
   * Save the data into files.
   */
  public void saveData() {
    Map<String, Integer> map1 =
        PredicateUtils.sortByValue(mapPredicates, PredicateUtils.SortBy.DESC);
    this.writeDataToFile(outputPredicates, map1);
    Map<String, Integer> map2 =
        PredicateUtils.sortByValue(mapProperties, PredicateUtils.SortBy.DESC);
    this.writeDataToFile(outputProperties, map2);
    Map<String, Integer> map3 =
        PredicateUtils.sortByValue(mapAttrEvents, PredicateUtils.SortBy.DESC);
    this.writeDataToFile(outputAttrEvents, map3);
    Map<String, Integer> map4 =
        PredicateUtils.sortByValue(mapEventTypes, PredicateUtils.SortBy.DESC);
    this.writeDataToFile(outputEventtypes, map4);
    //
    // this.writeDataToFile(outputOthertypes, mapOtherCount);
  }

  /**
   * Save the data in the map into the output file.
   * 
   * @param outputfile
   * @param map
   */
  protected void writeDataToFile(String outputfile, Map<String, Integer> map) {
    if (outputfile == null) {
      return;
    }
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(outputfile);
      bWriter = new BufferedWriter(fWriter);
      for (String key : map.keySet()) {
        bWriter.write(key + "," + map.get(key).intValue());
        bWriter.write("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (bWriter != null) {
          bWriter.close();
        }
        if (fWriter != null) {
          fWriter.close();
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public static void main(String... strings) {
    EventDataExploration ede = new EventDataExploration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputpath] [outputprefix]";
        System.out.println("java " + EventDataExploration.class.getName() + parameters);
        return;
      }
      ede.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      ede.setOutputPath(strings[1]);
    }
    if (strings.length >= 3) {
      ede.setOutputPrefix(strings[2]);
    }
    ede.parseEventFile(null);
    ede.summary(null);
    ede.saveData();
  }

}
