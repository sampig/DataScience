/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2018 - L3S
 */
package de.l3s.d4um.feature;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.PredicateUtils;

/**
 * Generate the feature space.<br/>
 * Input:
 * <ul>
 * <li>eventPositive.nq</li>
 * <li>eventNegative.nq</li>
 * </ul>
 * <br/>
 * Output:
 * <ul>
 * <li>sdtype_feature_space_property.out</li>
 * <li></li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class FeatureSpacePropertyGeneration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private String inputPositive = DEFAULT_PATH + "/eventPositive.nq";
  private String inputNegative = DEFAULT_PATH + "/eventNegativeNew.nq";

  private String outputPath = DEFAULT_PATH;

  private String outputPrefix = "sdtype_feature_space_";

  private Map<String, Map<String, Double>> mapEventPositive = new HashMap<>(0);
  private Map<String, Map<String, Double>> mapEventNegative = new HashMap<>(0);

  private List<String> listProperties = new ArrayList<>(0);
  private Map<String, Double> mapPropertyPositive = new HashMap<>(0);
  private Map<String, Double> mapPropertyNegative = new HashMap<>(0);
  private Map<String, Double> mapFeatureSpaces = new HashMap<>(0);

  private double percentPositive = 0;
  private double percentNegative = 0;

  public void setPositive(String positive) {
    this.inputPositive = positive;
    System.out.println("Set positive input: " + this.inputPositive);
  }

  public void setNegative(String negative) {
    this.inputNegative = negative;
    System.out.println("Set negative input: " + this.inputNegative);
  }

  public void setOutputPath(String path) {
    this.outputPath = path;
    System.out.println("Set output path: " + this.outputPath);
  }

  public void setOutputPrefix(String prefix) {
    this.outputPrefix = prefix;
    System.out.println("Set output prefix: " + this.outputPrefix);
  }

  public void readPropertiesFromFiles() {
    FileInputStream is;
    // Read positive.
    try {
      is = new FileInputStream(this.inputPositive);
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + inputPositive + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        if (!mapEventPositive.containsKey(nodeid)) {
          mapEventPositive.put(nodeid, new HashMap<String, Double>(0));
        }
        String predicate = nx[1].getLabel();
        String property = PredicateUtils.getNormalProperty(predicate);
        if (!listProperties.contains(property)) {
          listProperties.add(property);
        }
        Map<String, Double> map = mapEventPositive.get(nodeid);
        Double num = map.get(property);
        map.put(property, (num == null) ? 1 : num + 1);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    System.out.println("Positive data size: " + mapEventPositive.size());
    System.out.println("Sample: " + mapEventPositive.values().iterator().next());
    // Read negative.
    try {
      is = new FileInputStream(this.inputNegative);
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + inputNegative + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        if (!mapEventNegative.containsKey(nodeid)) {
          mapEventNegative.put(nodeid, new HashMap<String, Double>(0));
        }
        String predicate = nx[1].getLabel();
        String property = PredicateUtils.getNormalProperty(predicate);
        if (!listProperties.contains(property)) {
          listProperties.add(property);
        }
        Map<String, Double> map = mapEventNegative.get(nodeid);
        Double num = map.get(property);
        map.put(property, (num == null) ? 1 : num + 1);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    System.out.println("Negative data size: " + mapEventNegative.size());
    System.out.println("Sample: " + mapEventNegative.values().iterator().next());
    // Calculate percentages.
    double totalNumber = this.mapEventPositive.size() + this.mapEventNegative.size();
    this.percentPositive = this.mapEventPositive.size() / totalNumber;
    this.percentNegative = this.mapEventNegative.size() / totalNumber;
  }

  public void generatePropertyFeatures() {
    System.out.println("Calculating features from positive...");
    for (Entry<String, Map<String, Double>> entry : mapEventPositive.entrySet()) {
      Map<String, Double> map = entry.getValue();
      for (Entry<String, Double> p : map.entrySet()) {
        Double num = mapPropertyPositive.get(p.getKey());
        mapPropertyPositive.put(p.getKey(), (num == null) ? 1 : num + 1);
      }
    }
    System.out.println("Postive properties: " + this.mapPropertyPositive);
    System.out.println("Calculating features from negative...");
    for (Entry<String, Map<String, Double>> entry : mapEventNegative.entrySet()) {
      Map<String, Double> map = entry.getValue();
      for (Entry<String, Double> p : map.entrySet()) {
        Double num = mapPropertyNegative.get(p.getKey());
        mapPropertyNegative.put(p.getKey(), (num == null) ? 1 : num + 1);
      }
    }
    System.out.println("Postive properties: " + this.mapPropertyNegative);
    System.out.println("Properties size: " + listProperties.size());
    for (String property : listProperties) {
      double numPositive =
          (mapPropertyPositive.get(property) == null) ? 0 : mapPropertyPositive.get(property);
      double numNegative =
          (mapPropertyNegative.get(property) == null) ? 0 : mapPropertyNegative.get(property);
      double numEvents = numPositive + numNegative;
      double weight = Math.pow((percentPositive - numPositive / numEvents), 2)
          + Math.pow((percentNegative - numNegative / numEvents), 2);
      mapFeatureSpaces.put(property, weight);
    }
    System.out.println("Features size: " + mapFeatureSpaces.size());
    System.out.println("Features: " + mapFeatureSpaces);
  }

  /**
   * write the features into files.
   */
  public void writeFeatureSpace() {
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      String output = outputPath + "/" + outputPrefix + "property.out";
      fWriter = new FileWriter(output);
      bWriter = new BufferedWriter(fWriter);
      System.out.println("Writing properties into " + output + " ...");
      for (Entry<String, Double> entry : mapFeatureSpaces.entrySet()) {
        String property = entry.getKey();
        double numPositive =
            (mapPropertyPositive.get(property) == null) ? 0 : mapPropertyPositive.get(property);
        double numNegative =
            (mapPropertyNegative.get(property) == null) ? 0 : mapPropertyNegative.get(property);
        bWriter.write(
            property + "\t" + entry.getValue() + "\t" + numPositive + "\t" + numNegative + "\n");
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

  public Map<String, Map<String, Double>> getEventPositive() {
    return mapEventPositive;
  }

  public Map<String, Map<String, Double>> getEventNegative() {
    return mapEventNegative;
  }

  public Map<String, Double> getPropertyPositive() {
    return mapPropertyPositive;
  }

  public Map<String, Double> getPropertyNegative() {
    return mapPropertyNegative;
  }

  public List<String> getListProperties() {
    return this.listProperties;
  }

  public Map<String, Double> getMapFeatures() {
    return this.mapFeatureSpaces;
  }

  public static void main(String... strings) {
    String parameters = " [positive] [negative] [outputPath] [outputPrefix]";
    FeatureSpacePropertyGeneration fspg = new FeatureSpacePropertyGeneration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        System.out.println("java " + fspg.getClass().getName() + parameters);
        return;
      }
      fspg.setPositive(strings[0]);
    }
    if (strings.length >= 2) {
      fspg.setNegative(strings[1]);
    }
    if (strings.length >= 3) {
      fspg.setOutputPath(strings[2]);
    }
    if (strings.length >= 4) {
      fspg.setOutputPrefix(strings[3]);
    }
    fspg.readPropertiesFromFiles();
    fspg.generatePropertyFeatures();
    fspg.writeFeatureSpace();
  }

}
