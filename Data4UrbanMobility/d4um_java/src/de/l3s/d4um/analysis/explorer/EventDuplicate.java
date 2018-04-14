/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.explorer;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;

/**
 * Compare the positive events and the negative events. Save the events which are in both positive
 * dataset and negative dataset.
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventDuplicate {

  private String filepath = ConfigUtils.getDataDirectory();

  private String filePositive = filepath + "/eventNameDescriptionDE_P.nq";
  private String fileNegative = filepath + "/eventNameDescriptionDE_N.nq";

  private String outputFile = filepath + "/eventDuplicate.out";

  private List<String> listEventPositive = new ArrayList<>(0);
  private List<String> listEventNegative = new ArrayList<>(0);
  private List<String> listEventDuplicated = new ArrayList<>(0);

  /**
   * Set positive file.
   * 
   * @param positive
   */
  public void setPositive(String positive) {
    this.filePositive = positive;
    System.out.println("Set positive file: " + this.filePositive);
  }

  /**
   * Set negative file.
   * 
   * @param negative
   */
  public void setNegative(String negative) {
    this.fileNegative = negative;
    System.out.println("Set negative file: " + this.fileNegative);
  }

  /**
   * Set output file.
   * 
   * @param output
   */
  public void setOutput(String output) {
    this.outputFile = output;
    System.out.println("Set output file: " + this.outputFile);
  }

  /**
   * Read events from positive file.
   */
  public void readPositive() {
    FileInputStream is;
    try {
      is = new FileInputStream(filePositive);
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + filePositive + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        if (!listEventPositive.contains(nodeid)) {
          listEventPositive.add(nodeid);
        }
      }
      System.out.println("The total number of lines: " + nxp.lineNumber());
      System.out.println("The total number of events: " + listEventPositive.size());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Read events from negative file.
   */
  public void readNegative() {
    FileInputStream is;
    try {
      is = new FileInputStream(fileNegative);
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + fileNegative + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        // String predicate = nx[1].getLabel();
        // predicate = PredicateUtils.fixSchemaOrg(predicate);
        if (!listEventPositive.contains(nodeid)) {
          if (!listEventNegative.contains(nodeid)) {
            listEventNegative.add(nodeid);
          }
        } else {
          if (!listEventDuplicated.contains(nodeid)) {
            listEventDuplicated.add(nodeid);
          }
        }
      }
      System.out.println("The total number of lines: " + nxp.lineNumber());
      System.out.println("The total number of events: " + listEventNegative.size());
      System.out.println("Events in both dataset: " + listEventDuplicated.size());
      if (listEventDuplicated.size() > 1) {
        System.out.println(listEventDuplicated.get(0));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Save the duplicate events.
   */
  public void saveDuplicate() {
    System.out.println("\nExtracting data into " + outputFile + " ...");
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(outputFile);
      bWriter = new BufferedWriter(fWriter);
      for (String id : this.listEventDuplicated) {
        bWriter.write(id + "\n");
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
    EventDuplicate ed = new EventDuplicate();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [filePositive] [fileNegative] [outputfile]";
        System.out.println("java " + ed.getClass().getName() + parameters);
        return;
      }
      ed.setPositive(strings[0]);
    }
    if (strings.length >= 2) {
      ed.setNegative(strings[1]);
    }
    if (strings.length >= 3) {
      ed.setOutput(strings[2]);
    }
    ed.readPositive();
    ed.readNegative();
    ed.saveDuplicate();
  }

}
