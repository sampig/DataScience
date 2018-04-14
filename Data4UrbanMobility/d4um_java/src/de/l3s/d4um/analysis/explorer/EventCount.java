/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.explorer;

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
 * Count the number of the events.<br/>
 * Save the events' ID.
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventCount {

  private String filepath = ConfigUtils.getDataDirectory();

  private String inputfile = filepath + "/eventNameDescriptionDE.nq";

  private String outputfile = filepath + "/ede_event_count.out";

  private Map<String, Integer> mapEvents = new HashMap<>(0);

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
   * Count the number of the events.
   */
  public void count() {
    if (inputfile == null) {
      return;
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
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        Integer count = mapEvents.get(nodeid);
        mapEvents.put(nodeid, (count == null ? 1 : count + 1));
      }
      System.out.println("The total number of lines: " + nxp.lineNumber());
      System.out.println("The total number of events: " + mapEvents.size());
      // for (String key : mapEvents.keySet()) {
      // if (mapEvents.get(key).intValue() != 2) {
      // System.out.println(key);
      // }
      // }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Save the events' ID.
   */
  public void saveEventID() {
    System.out.println("\nSaving id into " + outputfile + " ...");
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(outputfile);
      bWriter = new BufferedWriter(fWriter);
      for (Map.Entry<String, Integer> entry : mapEvents.entrySet()) {
        bWriter.write(entry.getKey() + "\n");
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
    EventCount ec = new EventCount();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputfile]";
        System.out.println("java " + EventCount.class.getName() + parameters);
        return;
      }
      ec.setInput(strings[0]);
    }
    ec.count();
    if (strings.length >= 2) {
      ec.setOutput(strings[1]);
      ec.saveEventID();
    }
  }

}
