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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.data.model.EventModel;
import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.NGramUtils;
import de.l3s.d4um.utils.ConfigUtils.ConfigConstant;
import de.l3s.d4um.utils.PredicateUtils;
import de.l3s.d4um.utils.TokenizationUtils;

/**
 * Get languages distribution based on <b>name</b> and <b>description</b> which are text attributes.
 * <ul>
 * <li>asdf</li>
 * <li>It also checks whether there are events which use different languages for name and
 * description.</li>
 * <li>asdf</li>
 * </ul>
 * <br/>
 * Input: eventNameDescription.nq <br/>
 * Output:
 * <ul>
 * <li>eventNameDescriptionDE.nq</li>
 * <li>event_attr_*.nq</li>
 * <li>event_text_*.out</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventTextLanguage {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private int lenValidDescription = 30;
  private int lenValidName = 30;

  private String inputfile =
      DEFAULT_PATH + "/" + ConfigUtils.getValue(ConfigConstant.DATA_EVENT_FILE_NAMEDESC);

  private String outputPrefix = "event_";
  private String outputfile =
      DEFAULT_PATH + "/" + ConfigUtils.getValue(ConfigConstant.DATA_EVENT_FILE_NAMEDESC_DE);

  private List<String> listAttributes = Arrays.asList("name", "description");

  private Map<String, List<Node[]>> mapEvents = new HashMap<>(0);
  private Map<String, EventModel> mapEventsUsed = new HashMap<>(0);
  private Map<String, List<Node[]>> mapEventsDE = new HashMap<>(0);

  private Map<String, Integer> mapCountLang = new HashMap<>(0);
  private Map<String, Integer> mapCountNameLang = new HashMap<>(0);
  private Map<String, Integer> mapCountDescLang = new HashMap<>(0);

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
   * Set the output file.
   * 
   * @param output
   */
  public void setOutput(String output) {
    this.outputfile = output;
    System.out.println("Set output file: " + this.outputfile);
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
   * Set the valid length for name.
   * 
   * @param len
   */
  public void setNameLength(int len) {
    this.lenValidName = len;
  }

  /**
   * Set the valid length for description.
   * 
   * @param len
   */
  public void setDescriptionLength(int len) {
    this.lenValidDescription = len;
  }

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
        // String obj = nx[2].getLabel();// nx[2].toString();
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
          continue;
        }
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        // save the events.
        if (!mapEvents.containsKey(nodeid)) {
          // Map<String, String> map = new HashMap<>(0);
          List<Node[]> list = new ArrayList<>(0);
          mapEvents.put(nodeid, list);
        }
        mapEvents.get(nodeid).add(nx);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    // save the events which will be used.
    int countDuplicate = 0, countName = 0, countDesc = 0;
    for (Map.Entry<String, List<Node[]>> entry : mapEvents.entrySet()) {
      if (entry.getValue().size() < 2) {
        continue;
      }
      EventModel e = new EventModel();
      for (Node[] nx : entry.getValue()) {
        String predicate = nx[1].getLabel();
        String attr = PredicateUtils.getEventProperty(predicate);
        if ("name".equals(attr)) {
          e.name = nx[2].getLabel();
        } else if ("description".equals(attr)) {
          e.desc = nx[2].getLabel();
        }
      }
      if (e.name != null && e.desc != null) {
        String nodeid = entry.getKey();
        if (!mapEventsUsed.containsKey(nodeid)) {
          mapEventsUsed.put(nodeid, e);
          if (entry.getValue().size() >= 3) {
            countDuplicate++;
          }
          for (Node[] nx : entry.getValue()) {
            String predicate = nx[1].getLabel();
            String attr = PredicateUtils.getEventProperty(predicate);
            String lang = "Unknown";
            if (nx[2].toString().contains("\"@")) {
              lang = nx[2].toString().substring(nx[2].toString().lastIndexOf("@"));
            }
            if ("name".equals(attr)) {
              e.name_lang = lang;
              Integer i = mapCountLang.get(lang);
              mapCountLang.put(lang, (i == null) ? 1 : i + 1);
              Integer iName = mapCountNameLang.get(lang);
              mapCountNameLang.put(lang, (iName == null) ? 1 : iName + 1);
            } else if ("description".equals(attr)) {
              e.desc_lang = lang;
              Integer i = mapCountLang.get(lang);
              mapCountLang.put(lang, (i == null) ? 1 : i + 1);
              Integer iDesc = mapCountDescLang.get(lang);
              mapCountDescLang.put(lang, (iDesc == null) ? 1 : iDesc + 1);
            }
          }
          if (e.name_lang != null && e.desc_lang != null
              && (!"".equalsIgnoreCase(e.name) && !"".equalsIgnoreCase(e.desc))
              && (e.name_lang.contains("de") || e.desc_lang.contains("de"))) {
            int lenName = e.name.length();
            int lenDesc = TokenizationUtils
                .tokenizeTextDE(NGramUtils.removePunctuaion(e.desc).toLowerCase()).size();
            // && !"null".equalsIgnoreCase(e.desc)
            if (lenName >= lenValidName && lenDesc >= lenValidDescription) {
              mapEventsDE.put(nodeid, entry.getValue());
              if (entry.getValue().size() >= 3) {
                System.out.println("German 3: " + nodeid);
              }
            }
          }
        }
      } else if (e.name == null) {
        countName++;
      } else if (e.desc == null) {
        countDesc++;
      }
    }
    System.out.println("Event that will be used: " + mapEventsUsed.size());
    System.out.println("More than 2 attributes: " + countDuplicate);
    System.out.println("Name null: " + countName);
    System.out.println("Desc null: " + countDesc);
  }

  /**
   * Analyze the data.
   */
  public void analyze() {
    System.out.println("\nAnalyzing...");
    int count3 = 0, count3n = 0, count3d = 0, countName = 0, countDesc = 0;
    for (Map.Entry<String, List<Node[]>> entry : mapEvents.entrySet()) {
      if (entry.getValue().size() < 2) {
        continue;
      }
      String nodeid = entry.getKey();
      EventModel e = new EventModel(nodeid);
      for (Node[] nx : entry.getValue()) {
        String predicate = nx[1].getLabel();
        String attr = PredicateUtils.getEventProperty(predicate);
        if ("name".equals(attr)) {
          e.name = nx[2].getLabel();
          if (nx[2].toString().contains("\"@")) {
            String lang = nx[2].toString().substring(nx[2].toString().lastIndexOf("@"));
            e.name_lang = lang;
          } else {
            e.name_lang = "Unknown";
          }
        } else if ("description".equals(attr)) {
          e.desc = nx[2].getLabel();
          if (nx[2].toString().contains("\"@")) {
            String lang = nx[2].toString().substring(nx[2].toString().lastIndexOf("@"));
            e.desc_lang = lang;
          } else {
            e.name_lang = "Unknown";
          }
        }
      }
      // Check duplicate attributes.
      if (entry.getValue().size() >= 3) {
        count3++;
        if (e.name == null) {
          System.out.println(entry.getValue().size() + " name: " + nodeid);
          count3n++;
        } else if (e.desc == null) {
          System.out.println(entry.getValue().size() + " desc: " + nodeid);
          count3d++;
        }
      } else if (entry.getValue().size() == 2) {
        if (e.name == null) {
          // System.out.println("Name: " + nodeid);
          countName++;
        } else if (e.desc == null) {
          System.out.println("Desc: " + nodeid);
          countDesc++;
        }
      }
      // Check different languages.
      if (e.name_lang != null && e.desc_lang != null && !e.name_lang.equals(e.desc_lang)) {
        System.out.println("Language: " + e.id + ": " + e.name_lang + ", " + e.desc_lang);
        if (e.name_lang.contains("de") || e.desc_lang.contains("de")) {
          System.out.println("German: " + e.id + ": " + e.name_lang + ", " + e.desc_lang);
        }
      }
      // if (!mapEventsUsed.containsKey(nodeid)) {
      // mapEventsUsed.put(nodeid, e);
      // }
    }
    System.out.println("more than 3: " + count3);
    System.out.println(count3n + "," + count3d);
    System.out.println("Name: " + countName);
    System.out.println("Desc: " + countDesc);
  }

  /**
   * Print the results.
   */
  public void printResult() {
    System.out.println("Results:");
    System.out.println("\nEvents have duplicate attributes:");
    int count = 0;
    for (Map.Entry<String, List<Node[]>> entry : mapEvents.entrySet()) {
      if (entry.getValue().size() > 2) {
        count++;
      }
    }
    System.out.println(count);
    System.out.println("\nEvents with name and desc: " + mapEventsUsed.size());
    System.out.println("\nEvents used: " + mapEventsDE.size());
    System.out.println("\nLanguages distribution:");
    System.out.println(mapCountLang.size() + ": " + mapCountLang);
    System.out.println(mapCountNameLang.size() + ": " + mapCountNameLang);
    System.out.println(mapCountDescLang.size() + ": " + mapCountDescLang);

    String nodeid =
        "_:node27e2d6769e6ab1bfc1ca4757b323d5a0_<http://www.stadtmagazin-events.de/events/st-patricksday/>";
    List<Node[]> list = mapEvents.get(nodeid);
    if (list != null && list.size() > 0) {
      System.out.println("\nSamples:");
      System.out.println(nodeid);
      for (Node[] nx : list) {
        System.out.println(nx[2].toString());
        System.out.println(nx[2].getLabel());
      }
    }
  }

  /**
   * Save the events which will be used into the files.
   */
  public void saveData() {
    System.out.println("\nSaving data into " + outputfile + " ...");
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    FileWriter[] fNqWriters = new FileWriter[listAttributes.size()];
    FileWriter[] fTextWriters = new FileWriter[listAttributes.size()];
    BufferedWriter[] bNqWriters = new BufferedWriter[listAttributes.size()];
    BufferedWriter[] bTextWriters = new BufferedWriter[listAttributes.size()];
    try {
      fWriter = new FileWriter(outputfile);
      bWriter = new BufferedWriter(fWriter);
      for (int i = 0; i < listAttributes.size(); i++) {
        fNqWriters[i] = new FileWriter(
            DEFAULT_PATH + "/" + outputPrefix + "attr_" + listAttributes.get(i) + ".nq");
        bNqWriters[i] = new BufferedWriter(fNqWriters[i]);
        fTextWriters[i] = new FileWriter(
            DEFAULT_PATH + "/" + outputPrefix + "text_" + listAttributes.get(i) + ".out");
        bTextWriters[i] = new BufferedWriter(fTextWriters[i]);
      }
      for (Map.Entry<String, List<Node[]>> entry : mapEventsDE.entrySet()) {
        for (Node[] nx : entry.getValue()) {
          bWriter.write(nx[0] + " " + nx[1] + " " + nx[2] + " " + nx[3] + "   .");
          bWriter.write("\n");
          // System.out.println(Arrays.toString(nx));
          String predicate = nx[1].getLabel();
          String attr = PredicateUtils.getEventProperty(predicate);
          int i = listAttributes.indexOf(attr);
          bNqWriters[i].write(nx[0] + " " + nx[1] + " " + nx[2] + " " + nx[3] + "   .");
          bNqWriters[i].write("\n");
          bTextWriters[i].write(nx[2].getLabel() + "\n");
        }
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
        for (int i = 0; i < listAttributes.size(); i++) {
          if (bNqWriters[i] != null) {
            bNqWriters[i].close();
          }
          if (fNqWriters[i] != null) {
            fNqWriters[i].close();
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public static void main(String... strings) {
    EventTextLanguage etl = new EventTextLanguage();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputfile] [outputPrefix] [lenName,lenDesc]";
        System.out.println("java " + etl.getClass().getName() + parameters);
        return;
      }
      etl.setInput(strings[0]);
    }
    etl.parseEventFile();
    etl.analyze();
    etl.printResult();
    if (strings.length >= 2) {
      etl.setOutput(strings[1]);
    }
    if (strings.length >= 3) {
      etl.setOutputPrefix(strings[2]);
    }
    if (strings.length >= 4) {
      String[] lens = strings[3].split(",");
      try {
        int lenName = Integer.parseInt(lens[0]);
        int lenDesc = Integer.parseInt(lens[1]);
        etl.setNameLength(lenName);
        etl.setDescriptionLength(lenDesc);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    etl.saveData();
  }

}
