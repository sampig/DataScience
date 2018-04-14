/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.data;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.l3s.d4um.utils.ConfigUtils;

/**
 * Explore the data of file in N-Quads format. Make a summary.
 * 
 * @author Chenfeng Zhu
 *
 */
@Deprecated
public class DataExplorationExtract {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private List<String> attributesName = Arrays.asList("name", "description", "url");
  // , "location", "subEvent"
  private String inputfile = DEFAULT_PATH + "/" + ConfigUtils.getEventDataFilename();
  private String outputfile = DEFAULT_PATH + "/event_vector.out";

  private Map<String, Map<String, String>> mapEvents = new HashMap<>(0);
  private int count = 0;

  public void exploreEvent() {
    FileInputStream is;
    try {
      is = new FileInputStream(inputfile);

      NxParser nxp = new NxParser();
      System.out.println("Parsing " + inputfile + "...");
      nxp.parse(is);

      for (Node[] nx : nxp) {
        count++;
        String nodeid = nx[0].toString();
        String predict = nx[1].getLabel();
        String attr = predict.substring(predict.lastIndexOf("/") + 1);
        String obj = nx[2].getLabel();// nx[2].toString();
        if (!mapEvents.containsKey(nodeid)) {
          Map<String, String> map = new HashMap<>(0);
          String source = nodeid.substring(nodeid.indexOf("_<") + 2, nodeid.lastIndexOf(">"));
          map.put("source", source);
          mapEvents.put(nodeid, map);
        }
        if (attributesName.contains(attr)) {
          Map<String, String> map = mapEvents.get(nodeid);
          map.put(predict, obj);
        }
        // System.out.println(nx.length);
        // System.out.println(nx[0]);
        // System.out.println(nx[1]);
        // System.out.println(nx[2]);
        // System.out.println(nx[3]);
        // for (Node n : nx) {
        // System.out.println(n.getLabel() + ": " + n);
        // }
        // break;
      }
      System.out.println("The total number of quads: " + nxp.lineNumber());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void summary() {
    System.out.println("The total number of quads: " + count);
    System.out.println("The total number of events: " + mapEvents.size());
  }

  public void write() {
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(outputfile);
      bWriter = new BufferedWriter(fWriter);
      ObjectMapper mapper = new ObjectMapper();
      for (String nodeid : mapEvents.keySet()) {
        Map<String, String> map = mapEvents.get(nodeid);
        bWriter.write(nodeid + ": ");
        bWriter.write(mapper.writeValueAsString(map));
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
    DataExplorationExtract de = new DataExplorationExtract();
    de.exploreEvent();
    de.summary();
    de.write();
  }

}
