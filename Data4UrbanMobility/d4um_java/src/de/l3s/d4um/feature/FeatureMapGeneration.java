/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.feature;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.ConfigUtils.ConfigConstant;
import de.l3s.d4um.utils.FileUtils;
import de.l3s.d4um.utils.PredicateUtils;
import de.l3s.d4um.utils.URLUtils;

/**
 * Generate the feature map for the non-text properties such as event type and URL.
 * 
 * @author Chenfeng Zhu
 *
 */
public class FeatureMapGeneration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();
  private String outputfilepath = DEFAULT_PATH;

  private String inputfile =
      DEFAULT_PATH + "/" + ConfigUtils.getValue(ConfigConstant.DATA_EVENT_FILE_NAMEDESC_DE);

  private String outputPrefix = "fm_event_";
  private String outputEventtypes = DEFAULT_PATH + "/" + outputPrefix + "types.out";
  private String outputEventTLDs = DEFAULT_PATH + "/" + outputPrefix + "tlds.out";
  private String outputEventPLDs = DEFAULT_PATH + "/" + outputPrefix + "plds.out";

  private Map<String, Map<String, String>> mapEvents = new HashMap<>(0);
  private Map<String, Integer> mapEventTypes = new HashMap<>(0);
  private Map<String, Integer> mapEventTLDs = new HashMap<>(0);
  private Map<String, Integer> mapEventPLDs = new HashMap<>(0);

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
    this.outputEventtypes = outputfilepath + "/" + outputPrefix + "types.out";
    this.outputEventTLDs = outputfilepath + "/" + outputPrefix + "tlds.out";
    this.outputEventPLDs = outputfilepath + "/" + outputPrefix + "plds.out";
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
    this.outputEventtypes = outputfilepath + "/" + outputPrefix + "types.out";
    this.outputEventTLDs = outputfilepath + "/" + outputPrefix + "tlds.out";
    this.outputEventPLDs = outputfilepath + "/" + outputPrefix + "plds.out";
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
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String obj = nx[2].getLabel();// nx[2].toString();
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
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
        // count the number of event types.
        String eventType = PredicateUtils.getEventType(predicate);
        Integer countEventType = mapEventTypes.get(eventType);
        mapEventTypes.put(eventType, (countEventType == null ? 1 : countEventType + 1));
        // count the number of event TLDs.
        String url = URLUtils.getURL(nodeid);
        String tld = URLUtils.getTLD(url);
        Integer countTLD = mapEventTLDs.get(tld);
        mapEventTLDs.put(tld, (countTLD == null ? 1 : countTLD + 1));
        String pld = URLUtils.getPLD(url);
        Integer countPLD = mapEventPLDs.get(pld);
        mapEventPLDs.put(pld, (countPLD == null ? 1 : countPLD + 1));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Print the summary of the data file after parsing.
   */
  public void summary() {
    System.out.println("The total number of events: " + mapEvents.size());
    System.out.println("The total number of event types: " + mapEventTypes.size());
    System.out.println("The total number of tlds: " + mapEventTLDs.size());
    System.out.println("The total number of plds: " + mapEventPLDs.size());
  }

  /**
   * Save the data into files.
   */
  public void saveData() {
    Map<String, Integer> map1 =
        PredicateUtils.sortByValue(mapEventTypes, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(outputEventtypes, map1);
    Map<String, Integer> map2 =
        PredicateUtils.sortByValue(mapEventTLDs, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(outputEventTLDs, map2);
    Map<String, Integer> map3 =
        PredicateUtils.sortByValue(mapEventPLDs, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(outputEventPLDs, map3);
  }

  public static void main(String... strings) {
    FeatureMapGeneration fmg = new FeatureMapGeneration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputpath] [outputprefix]";
        System.out.println("java " + FeatureMapGeneration.class.getName() + parameters);
        return;
      }
      fmg.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      fmg.setOutputPath(strings[1]);
    }
    if (strings.length >= 3) {
      fmg.setOutputPrefix(strings[2]);
    }
    fmg.parseEventFile(null);
    fmg.summary();
    fmg.saveData();
  }

}
