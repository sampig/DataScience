/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.data;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.TokenizationUtils.LanguageType;
import de.l3s.d4um.utils.VectorUtils;

/**
 * Extract the data with name and description in a given language only.
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventExtractor {

  private String filepath = ConfigUtils.getDataDirectory();
  // inputPositive
  private String datasetFilename = filepath + "/eventPositive.nq";
  private String outputFilename = filepath + "/eventPositiveExp.nq";

  private LanguageType langType = LanguageType.EN;
  private int size = 0;

  private List<String> listEventIds = new ArrayList<>(0);
  private Map<String, String> mapEventDataIds = new HashMap<String, String>(0);
  private Map<String, List<Node[]>> mapEvents = new HashMap<>(0);
  private List<String> listEventExtract = new ArrayList<>(0);

  public void setDataset(String dataset) {
    this.datasetFilename = dataset;
    System.out.println("Set dataset: " + this.datasetFilename);
  }

  public void setOutput(String output) {
    this.outputFilename = output;
    System.out.println("Set output: " + this.outputFilename);
  }

  public void setLang(LanguageType type) {
    this.langType = type;
    System.out.println("Set language: " + this.langType);
  }

  public void setSize(int size) {
    this.size = size;
    System.out.println("Set the size of new dataset: " + this.size);
  }

  /**
   * Parse the event data file.
   * 
   * @param inputfile
   */
  public void parseEventFile() {
    FileInputStream is;
    try {
      is = new FileInputStream(this.datasetFilename);
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + datasetFilename + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String obj = nx[2].toString();
        if (!mapEvents.containsKey(nodeid)) {
          List<Node[]> list = new ArrayList<>(0);
          mapEvents.put(nodeid, list);
          mapEventDataIds.put(nodeid, "");
        }
        mapEvents.get(nodeid).add(nx);
        if (predicate.endsWith("/name")) {
          boolean flag = false;
          if (this.langType == LanguageType.EN) {
            if (obj.contains("\"@") || obj.contains("\\\"@")) {
              String lang = nx[2].toString().substring(nx[2].toString().lastIndexOf("@"));
              if (lang.toLowerCase().contains("@en")) {
                flag = true;
              }
            } else {
              flag = true;
            }
          } else if (this.langType == LanguageType.DE) {
            if (obj.contains("\"@") || obj.contains("\\\"@")) {
              String lang = nx[2].toString().substring(nx[2].toString().lastIndexOf("@"));
              if (lang.toLowerCase().contains("@de")) {
                flag = true;
              }
            }
          }
          if (flag) {
            String value = mapEventDataIds.get(nodeid) + "name";
            mapEventDataIds.put(nodeid, value);
          }
        } else if (predicate.endsWith("/description")) {
          boolean flag = false;
          if (this.langType == LanguageType.EN) {
            if (obj.contains("\"@") || obj.contains("\\\"@")) {
              String lang = nx[2].toString().substring(nx[2].toString().lastIndexOf("@"));
              if (lang.toLowerCase().contains("@en")) {
                flag = true;
              }
            } else {
              flag = true;
            }
          } else if (this.langType == LanguageType.DE) {
            if (obj.contains("\"@") || obj.contains("\\\"@")) {
              String lang = nx[2].toString().substring(nx[2].toString().lastIndexOf("@"));
              if (lang.toLowerCase().contains("@de")) {
                flag = true;
              }
            }
          }
          if (flag) {
            String value = mapEventDataIds.get(nodeid) + "description";
            mapEventDataIds.put(nodeid, value);
          }
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    for (Map.Entry<String, String> entry : mapEventDataIds.entrySet()) {
      String value = entry.getValue();
      if (value != null && value.contains("name") && value.contains("description")) {
        String id = entry.getKey();
        if (!listEventIds.contains(id)) {
          listEventIds.add(id);
        }
      }
    }
    System.out.println("Number of events with name and description: " + listEventIds.size());
    if (size > 0) {
      List<Integer> listIdx = VectorUtils.getRandomNumbers(size, listEventIds.size());
      for (int i : listIdx) {
        listEventExtract.add(listEventIds.get(i));
      }
    } else {
      listEventExtract.addAll(listEventIds);
    }
  }

  /**
   * Save the data into a new file in n-quad format.
   */
  public void extractData() {
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(this.outputFilename);
      bWriter = new BufferedWriter(fWriter);
      System.out.println("Writing data into '" + outputFilename + "'...");
      for (Map.Entry<String, List<Node[]>> entry : mapEvents.entrySet()) {
        String id = entry.getKey();
        if (!listEventExtract.contains(id)) {
          continue;
        }
        for (Node[] nx : entry.getValue()) {
          bWriter.write(id + " " + nx[1] + " " + nx[2] + " " + nx[3] + " .\n");
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
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
    EventExtractor ee = new EventExtractor();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [input] [output] [lang] [size]";
        System.out.println("java " + ee.getClass().getName() + parameters);
        return;
      }
      ee.setDataset(strings[0]);
    }
    if (strings.length >= 2) {
      ee.setOutput(strings[1]);
    }
    if (strings.length >= 3) {
      if ("en".equalsIgnoreCase(strings[2])) {
        ee.setLang(LanguageType.EN);
      } else if ("de".equalsIgnoreCase(strings[2])) {
        ee.setLang(LanguageType.DE);
      }
    }
    if (strings.length >= 4) {
      int size = 0;
      try {
        size = Integer.parseInt(strings[3]);
      } catch (Exception e) {
        e.printStackTrace();
      }
      ee.setSize(size);
    }
    ee.parseEventFile();
    ee.extractData();
  }

}
