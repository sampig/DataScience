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

import de.l3s.d4um.utils.FileUtils;
import de.l3s.d4um.utils.PredicateUtils;
import de.l3s.d4um.utils.URLUtils;

/**
 * Display the URL-related information.
 * <ul>
 * <li></li>
 * </ul>
 * <br/>
 * Input:
 * <ul>
 * <li>Event sub-dataset file. (N-Quads format)</li>
 * </ul>
 * <br/>
 * Output:
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventURLExplorer extends BaseExplorer {

  private String outputPrefix = "eue_P_";

  private String outputUrl = "url";
  private String outputIdUrl = "url_id";
  private String outputObjUrl = "url_object";
  private String outputUrlEvent = "url_event";
  private String outputObjUrlEvent = "url_object_event";
  private String outputTLDEvent = "tld_event";
  private String outputObjTLDEvent = "tld_object_event";
  private String outputPLDEvent = "pld_event";
  private String outputObjPLDEvent = "pld_object_event";

  private Map<String, String> mapEventUrl = new HashMap<>(0);
  private Map<String, String> mapEventUrlObj = new HashMap<>(0);

  private Map<String, Integer> mapUrls = new HashMap<>(0);
  private Map<String, Integer> mapIdUrls = new HashMap<>(0);
  private Map<String, Integer> mapObjUrls = new HashMap<>(0);
  private Map<String, Integer> mapUrlsEvent = new HashMap<>(0);
  private Map<String, Integer> mapObjUrlsEvent = new HashMap<>(0);

  private Map<String, Integer> mapTLDsEvent = new HashMap<>(0);
  private Map<String, Integer> mapObjTLDsEvent = new HashMap<>(0);
  private Map<String, Integer> mapPLDsEvent = new HashMap<>(0);
  private Map<String, Integer> mapObjPLDsEvent = new HashMap<>(0);

  private List<String> listNotExist = new ArrayList<>(0);
  private List<String> listNotEqual = new ArrayList<>(0);

  public EventURLExplorer() {
    super.setInput(super.getPath() + "/eventPositive.nq");
    super.setOutput(super.getPath() + "/eue.out");
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
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + super.getInput() + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String obj = nx[2].getLabel();
        String url = nx[3].getLabel();
        // from url:
        Integer countUrl = mapUrls.get(url);
        mapUrls.put(url, (countUrl == null ? 1 : countUrl + 1));
        if (!mapEventUrl.containsKey(nodeid)) {
          mapEventUrl.put(nodeid, url);
        }
        // from id:
        if (nodeid.contains("http")) {
          Integer countIdUrl = mapIdUrls.get(URLUtils.getURL(nodeid));
          mapIdUrls.put(URLUtils.getURL(nodeid), (countIdUrl == null ? 1 : countIdUrl + 1));
        } else {
          System.out.println("id: " + nodeid);
        }
        // from object:
        if (predicate.toLowerCase().contains("url")) {
          Integer countObjUrl = mapObjUrls.get(obj);
          mapObjUrls.put(obj, (countObjUrl == null ? 1 : countObjUrl + 1));
          if (!mapEventUrlObj.containsKey(nodeid)) {
            mapEventUrlObj.put(nodeid, obj);
          }
        }
      }
      System.out.println("The total number of lines: " + nxp.lineNumber());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Print some basic summary information.
   */
  public void summary() {
    System.out.println("Total URL in quads: " + mapUrls.size());
    System.out.println("Total URL in ids: " + mapIdUrls.size());
    System.out.println("Total URL in objects: " + mapObjUrls.size());
    for (Map.Entry<String, String> entry : mapEventUrl.entrySet()) {
      String key = entry.getKey();
      String url = entry.getValue();
      if (!mapEventUrlObj.containsKey(key)) {
        listNotExist.add(key);
      } else {
        if (!url.equalsIgnoreCase(mapEventUrlObj.get(key))) {
          listNotEqual.add(key);
        }
      }
    }
    System.out.println("Without URL predicate: " + listNotExist.size());
    System.out.println("Not equal: " + listNotEqual.size());
    for (Map.Entry<String, String> entry : mapEventUrl.entrySet()) {
      String url = entry.getValue();
      Integer countUrl = mapUrlsEvent.get(url);
      mapUrlsEvent.put(url, (countUrl == null ? 1 : countUrl + 1));
      String pld = URLUtils.getPLD(url);
      Integer countPLD = mapPLDsEvent.get(pld);
      mapPLDsEvent.put(pld, (countPLD == null ? 1 : countPLD + 1));
      String tld = URLUtils.getTLD(url);
      Integer countTLD = mapTLDsEvent.get(tld);
      mapTLDsEvent.put(tld, (countTLD == null ? 1 : countTLD + 1));
    }
    for (Map.Entry<String, String> entry : mapEventUrlObj.entrySet()) {
      String url = entry.getValue();
      Integer countUrl = mapObjUrlsEvent.get(url);
      mapObjUrlsEvent.put(url, (countUrl == null ? 1 : countUrl + 1));
      String pld = URLUtils.getPLD(url);
      Integer countPLD = mapObjPLDsEvent.get(pld);
      mapObjPLDsEvent.put(pld, (countPLD == null ? 1 : countPLD + 1));
      String tld = URLUtils.getTLD(url);
      Integer countTLD = mapObjTLDsEvent.get(tld);
      mapObjTLDsEvent.put(tld, (countTLD == null ? 1 : countTLD + 1));
    }
  }

  /**
   * Save the data into files.
   */
  public void saveData() {
    Map<String, Integer> map1 = PredicateUtils.sortByValue(mapUrls, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputUrl), map1);
    Map<String, Integer> map2 = PredicateUtils.sortByValue(mapIdUrls, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputIdUrl), map2);
    Map<String, Integer> map3 = PredicateUtils.sortByValue(mapObjUrls, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputObjUrl), map3);
    Map<String, Integer> map4 =
        PredicateUtils.sortByValue(mapUrlsEvent, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputUrlEvent), map4);
    Map<String, Integer> map5 =
        PredicateUtils.sortByValue(mapObjUrlsEvent, PredicateUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputObjUrlEvent), map5);
    // PLD:
    FileUtils.writeMapdataToFile(this.getOutput(outputPLDEvent),
        PredicateUtils.sortByValue(mapPLDsEvent, PredicateUtils.SortBy.DESC));
    FileUtils.writeMapdataToFile(this.getOutput(outputObjPLDEvent),
        PredicateUtils.sortByValue(mapObjPLDsEvent, PredicateUtils.SortBy.DESC));
    // TLD:
    FileUtils.writeMapdataToFile(this.getOutput(outputTLDEvent),
        PredicateUtils.sortByValue(mapTLDsEvent, PredicateUtils.SortBy.DESC));
    FileUtils.writeMapdataToFile(this.getOutput(outputObjTLDEvent),
        PredicateUtils.sortByValue(mapObjTLDsEvent, PredicateUtils.SortBy.DESC));
  }

  public static void main(String... strings) {
    EventURLExplorer eue = new EventURLExplorer();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputpath] [outputprefix]";
        System.out.println("java " + EventURLExplorer.class.getName() + parameters);
        return;
      }
      eue.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      eue.setPath(strings[1]);
    }
    if (strings.length >= 3) {
      eue.setOutputPrefix(strings[2]);
    }
    eue.parseEventFile();
    eue.summary();
    eue.saveData();
  }

}
