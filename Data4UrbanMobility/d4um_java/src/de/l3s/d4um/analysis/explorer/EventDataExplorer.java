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
 * Explore the event data.
 * <ul>
 * <li>Parse the event data file in N-Quads format.</li>
 * <li>Make a summary(Total quantity, distribution, etc.).</li>
 * <li>Write these results into files.</li>
 * </ul>
 * <br/>
 * Input:
 * <ul>
 * <li>Event data file. (N-Quads format)</li>
 * </ul>
 * <br/>
 * Output:
 * <ul>
 * <li>Predicate frequency.</li>
 * <li>Type frequency.</li>
 * <li>W3 Type frequency.</li>
 * <li>Property frequency.</li>
 * <li>Event attribute count.</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventDataExplorer extends BaseExplorer {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();
  // private String outputfilepath = DEFAULT_PATH;

  private String inputFilename = DEFAULT_PATH + "/test.nq";// + ConfigUtils.getEventDataFilename();
  private String outputPrefix = "ede_P_";

  private String outputAttrEvents = "attricount";
  private String outputAttrEventsUnique = "attricount_unique";
  private String outputPredicates = "predicates";
  private String outputProperties = "properties";
  private String outputEventtypes = "types";
  private String outputW3types = "w3types";
  private String outputW3typesevents = "w3typesevents";
  // private String outputOthertypes = filepath + "/ede_event_others.out";

  private Map<String, Map<String, String>> mapEvents = new HashMap<>(0);
  // private Map<String, Map<String, String>> mapOthers = new HashMap<>(0);
  private Map<String, Integer> mapAttrEvents = new HashMap<>(0);
  private Map<String, List<String>> mapAttrEventsUnique = new HashMap<>(0);
  private Map<String, Integer> mapEventTypes = new HashMap<>(0);
  private Map<String, Integer> mapPredicates = new HashMap<>(0);
  private Map<String, Integer> mapProperties = new HashMap<>(0);
  private Map<String, Integer> mapW3Types = new HashMap<>(0);
  private Map<String, Integer> mapW3TypesEvents = new HashMap<>(0);
  // private Map<String, Integer> mapOtherCount = new HashMap<>(0);
  private int count = 0;

  public EventDataExplorer() {
    System.out.println("EventDataExplorer constructor. Set default values: ");
    super.setInput(inputFilename);
    super.setPath(DEFAULT_PATH);
    System.out.println("==============");
  }

  public String getOutput(String name) {
    return super.getPath() + "/" + this.outputPrefix + name + ".out";
  }

  /**
   * Set the prefix for the output files.
   * 
   * @param prefix
   */
  public void setOutputPrefix(String prefix) {
    this.outputPrefix = prefix;
    System.out.println("Set output prefix: " + this.outputPrefix);
  }

  /**
   * Parse the event data file.
   * 
   * @param inputfile
   */
  public void parseEventFile() {
    FileInputStream is;
    try {
      is = new FileInputStream(super.getInput());
      // parse the file.
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + super.getInput() + "...");
      nxp.parse(is);
      // read every quad.
      for (Node[] nx : nxp) {
        count++;
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String obj = nx[2].getLabel();// nx[2].toString();
        // save other types.
        if (!nodeid.contains("http")) {
          nodeid += "_" + nx[3].toString();
        }
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
        String property = PredicateUtils.getNormalProperty(predicate);
        Integer countProperty = mapProperties.get(property);
        mapProperties.put(property, (countProperty == null ? 1 : countProperty + 1));
        // count the number of event types.
        String eventType = PredicateUtils.getEventType(predicate);
        Integer countEventType = mapEventTypes.get(eventType);
        mapEventTypes.put(eventType, (countEventType == null ? 1 : countEventType + 1));
        // count the number of W3 types.
        if (predicate.contains("22-rdf-syntax-ns#type")) {
          Integer countW3Type = mapW3Types.get(obj);
          mapW3Types.put(obj, (countW3Type == null ? 1 : countW3Type + 1));
          Integer countW3TypeEvent = mapW3TypesEvents.get(nodeid);
          mapW3TypesEvents.put(nodeid, (countW3TypeEvent == null ? 1 : countW3TypeEvent + 1));
        }
        // count distinct property
        if (!mapAttrEventsUnique.containsKey(nodeid)) {
          List<String> list = new ArrayList<>(0);
          mapAttrEventsUnique.put(nodeid, list);
        }
        if (!mapAttrEventsUnique.get(nodeid).contains(property)) {
          mapAttrEventsUnique.get(nodeid).add(property);
        }
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
  public void summary() {
    if (count <= 0) {
      this.parseEventFile();
    }
    System.out.println("The total number of quads: " + count);
    System.out.println("The total number of events: " + mapEvents.size());
    System.out.println("The total number of predicates: " + mapPredicates.size());
    System.out.println("The total number of event types: " + mapEventTypes.size());
    System.out.println("The total number of properties: " + mapProperties.size());
    System.out.println("The total number of w3-types: " + mapW3Types.size());
    System.out.println("Multiple W3 Types Events: " + mapW3TypesEvents.size());
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
    // this.writeDataToFile(outputPredicates, map1);
    FileUtils.writeMapdataToFile(this.getOutput(outputPredicates), map1);
    Map<String, Integer> map2 =
        PredicateUtils.sortByValue(mapProperties, PredicateUtils.SortBy.DESC);
    // this.writeDataToFile(outputProperties, map2);
    FileUtils.writeMapdataToFile(this.getOutput(outputProperties), map2);
    Map<String, Integer> map3 =
        PredicateUtils.sortByValue(mapAttrEvents, PredicateUtils.SortBy.DESC);
    // this.writeDataToFile(outputAttrEvents, map3);
    FileUtils.writeMapdataToFile(this.getOutput(outputAttrEvents), map3);
    Map<String, Integer> map4 =
        PredicateUtils.sortByValue(mapEventTypes, PredicateUtils.SortBy.DESC);
    // this.writeDataToFile(outputEventtypes, map4);
    FileUtils.writeMapdataToFile(this.getOutput(outputEventtypes), map4);
    Map<String, Integer> map5 = PredicateUtils.sortByValue(mapW3Types, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputW3types), map5);
    Map<String, Integer> map6 =
        PredicateUtils.sortByValue(mapW3TypesEvents, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputW3typesevents), map6);
  }

  /**
   * 
   */
  public void checkEventProperty() {
    System.out.println("Check duplicated properties: ");
    Map<String, Integer> map7 = new HashMap<>(0);
    for (Map.Entry<String, List<String>> entry : mapAttrEventsUnique.entrySet()) {
      map7.put(entry.getKey(), entry.getValue().size());
      if (entry.getValue().size() != mapAttrEvents.get(entry.getKey())) {
        System.out.println(entry.getKey());
      }
    }
    FileUtils.writeMapdataToFile(this.getOutput(outputAttrEventsUnique), map7);
  }

  public static void main(String... strings) {
    EventDataExplorer ede = new EventDataExplorer();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputpath] [outputprefix]";
        System.out.println("java " + EventDataExplorer.class.getName() + parameters);
        return;
      }
      ede.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      ede.setPath(strings[1]);
    }
    if (strings.length >= 3) {
      ede.setOutputPrefix(strings[2]);
    }
    ede.parseEventFile();
    ede.summary();
    ede.saveData();
    if (strings.length >= 4) {
      ede.checkEventProperty();
    }
  }

}
