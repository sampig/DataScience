/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.explorer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.FileUtils;
import de.l3s.d4um.utils.NGramUtils;
import de.l3s.d4um.utils.PredicateUtils;
import de.l3s.d4um.utils.VectorUtils;

/**
 * Count the length of name for events by characters.
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventNameLength extends BaseExplorer {

  private final static String ATTR = "name";
  private String outputPrefix = "enl_";

  private Map<String, Integer> mapEvents = new HashMap<>(0);
  private Map<String, Integer> mapLengths = new HashMap<>(0);

  public EventNameLength() {
    super.setInput(super.getPath() + "/eventNameDescriptionDE.nq");
    super.setOutput(super.getPath() + "/enl.out");
  }

  public void setOutputPrefix(String prefix) {
    this.outputPrefix = prefix;
    System.out.println("Set output prefix: " + this.outputPrefix);
  }

  public String getOutput(String name) {
    return super.getPath() + "/" + this.outputPrefix + name + ".out";
  }

  /**
   * Count the number of characters.
   */
  public void countByCharacter() {
    if (super.getInput() == null) {
      return;
    }
    FileInputStream is;
    try {
      is = new FileInputStream(super.getInput());
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + super.getInput() + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
          continue;
        }
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        if (!ATTR.equalsIgnoreCase(PredicateUtils.getEventProperty(predicate))) {
          continue;
        }
        String obj = nx[2].getLabel();
        int length = obj.length();
        Integer count = mapEvents.get(nodeid);
        mapEvents.put(nodeid, (count == null ? length : count + length));
        Integer countLength = mapLengths.get(String.valueOf(length));
        mapLengths.put(String.valueOf(length), (countLength == null ? 1 : countLength + 1));
      }
      System.out.println("The total number of lines: " + nxp.lineNumber());
      System.out.println("The total number of events: " + mapEvents.size());
      System.out.println("Start sorting mapEvents...");
      Map<String, Integer> mapEvent = VectorUtils.sortByValue(mapEvents, VectorUtils.SortBy.DESC);
      FileUtils.writeMapdataToFile(this.getOutput("event"), mapEvent);
      System.out.println("Start sorting mapLengths...");
      Map<String, Integer> mapLength = VectorUtils.sortByValue(mapLengths, VectorUtils.SortBy.DESC);
      FileUtils.writeMapdataToFile(this.getOutput("length"), mapLength);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Count the number of characters after replacing punctuation with spaces.
   */
  public void countByAlpha() {
    if (super.getInput() == null) {
      return;
    }
    FileInputStream is;
    try {
      is = new FileInputStream(super.getInput());
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + super.getInput() + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
          continue;
        }
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        if (!ATTR.equalsIgnoreCase(PredicateUtils.getEventProperty(predicate))) {
          continue;
        }
        String obj = nx[2].getLabel();
        obj = NGramUtils.removePunctuaion(obj);
        int length = obj.length();
        Integer count = mapEvents.get(nodeid);
        mapEvents.put(nodeid, (count == null ? length : count + length));
        Integer countLength = mapLengths.get(String.valueOf(length));
        mapLengths.put(String.valueOf(length), (countLength == null ? 1 : countLength + 1));
      }
      System.out.println("The total number of lines: " + nxp.lineNumber());
      System.out.println("The total number of events: " + mapEvents.size());
      System.out.println("Start sorting mapEvents...");
      Map<String, Integer> mapEvent = VectorUtils.sortByValue(mapEvents, VectorUtils.SortBy.DESC);
      FileUtils.writeMapdataToFile(this.getOutput("event"), mapEvent);
      System.out.println("Start sorting mapLengths...");
      Map<String, Integer> mapLength = VectorUtils.sortByValue(mapLengths, VectorUtils.SortBy.DESC);
      FileUtils.writeMapdataToFile(this.getOutput("length"), mapLength);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void main(String... strings) {
    EventNameLength edl = new EventNameLength();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputPrefix] [path]";
        System.out.println("java " + edl.getClass().getName() + parameters);
        return;
      }
      edl.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      edl.setOutputPrefix(strings[1]);
    }
    if (strings.length >= 3) {
      edl.setPath(strings[2]);
    }
    if (strings.length >= 4) {
      edl.countByAlpha();
    } else {
      edl.countByCharacter();
    }
  }

}
