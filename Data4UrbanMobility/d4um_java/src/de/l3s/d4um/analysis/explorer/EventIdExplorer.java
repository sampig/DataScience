/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.explorer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.l3s.d4um.utils.FileUtils;
import de.l3s.d4um.utils.VectorUtils;

/**
 * Explore the event IDs.<br/>
 * Input:
 * <ul>
 * <li>Event id file. (TSV format)</li>
 * </ul>
 * <br/>
 * Output:
 * <ul>
 * <li>Basic summary(Total number of entities, unique ids, unique urls).</li>
 * <li>All entities(id+url).</li>
 * <li>All ids.</li>
 * <li>All urls.</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventIdExplorer extends BaseExplorer {

  private String inputFilename = super.getPath() + "/eventsIdsNewYorkPositive.tsv";

  private String outputPrefix = "eie_P_";
  private String outputId = "id";
  private String outputUrl = "url";
  private String outputEventid = "eventid";

  private List<String> listEvents = new ArrayList<>(0);
  private List<String> listEventIds = new ArrayList<>(0);
  private Map<String, Integer> mapEvents = new HashMap<>(0);
  private Map<String, Integer> mapEventIds = new HashMap<>(0);
  private Map<String, Integer> mapEventUrls = new HashMap<>(0);
  private Map<String, Integer> mapEventIdTypes = new HashMap<>(0);

  public EventIdExplorer() {
    System.out.println("EventIdExplorer constructor. Set default values: ");
    super.setInput(inputFilename);
    System.out.println("==============");
  }

  public void setOutputPrefix(String prefix) {
    this.outputPrefix = prefix;
    System.out.println("Set output prefix: " + this.outputPrefix);
  }

  public String getOutput(String name) {
    return super.getPath() + "/" + this.outputPrefix + name + ".out";
  }

  public List<String> getListEventIds() {
    return this.listEventIds;
  }

  /**
   * Read the IDs.
   */
  public void readEventIds() {
    System.out.println("Reading the data from '" + super.getInput() + "'...");
    FileReader fileReader = null;
    BufferedReader bReader = null;
    try {
      fileReader = new FileReader(super.getInput());
      bReader = new BufferedReader(fileReader);
      String line = null;
      while ((line = bReader.readLine()) != null) {
        if (!line.contains("\t")) {
          System.out.println(line);
          continue;
        }
        String[] str = line.split("\t");
        if (str.length >= 3) {
          System.out.println(line);
          continue;
        }
        String url = str[0];
        String id = str[1];
        String newId = id.contains(url) ? id : id + "_" + url;
        String type = id.substring(0, 3);
        Integer countId = mapEventIds.get(id);
        mapEventIds.put(id, (countId == null ? 1 : countId + 1));
        Integer countUrl = mapEventUrls.get(url);
        mapEventUrls.put(url, (countUrl == null ? 1 : countUrl + 1));
        Integer count = mapEvents.get(newId);
        mapEvents.put(newId, (count == null ? 1 : count + 1));
        Integer countType = mapEventIdTypes.get(type);
        mapEventIdTypes.put(type, (countType == null ? 1 : countType + 1));
        listEvents.add(newId);
        listEventIds.add(str[1]);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (bReader != null) {
          bReader.close();
        }
        if (fileReader != null) {
          fileReader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Print the summary.
   */
  public void printSummary() {
    System.out.println("Number of events: " + listEventIds.size());
    System.out.println("Number of unique id+url: " + mapEvents.size());
    System.out.println("Number of unique ids: " + mapEventIds.size());
    System.out.println("Number of unique urls: " + mapEventUrls.size());
    System.out.println("Id Types: " + mapEventIdTypes);
  }

  /**
   * Save the data.
   */
  public void saveData() {
    Map<String, Integer> mapIds = VectorUtils.sortByValue(mapEventIds, VectorUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputId), mapIds);
    Map<String, Integer> mapUrls = VectorUtils.sortByValue(mapEventUrls, VectorUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputUrl), mapUrls);
    Map<String, Integer> map = VectorUtils.sortByValue(mapEvents, VectorUtils.SortBy.DESC);
    FileUtils.writeMapdataToFile(this.getOutput(outputEventid), map);
  }

  public static void main(String... strings) {
    EventIdExplorer eie = new EventIdExplorer();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [path] [outputprefix]";
        System.out.println("java " + eie.getClass().getName() + parameters);
        return;
      }
      eie.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      eie.setPath(strings[1]);
    }
    if (strings.length >= 3) {
      eie.setOutputPrefix(strings[2]);
    }
    eie.readEventIds();
    eie.printSummary();
    eie.saveData();
  }

}
