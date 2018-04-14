/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.feature;

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
import java.util.Map.Entry;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.data.model.EventModel;
import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.NGramUtils;
import de.l3s.d4um.utils.TokenizationUtils;
import de.l3s.d4um.utils.TokenizationUtils.LanguageType;

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
 * <li>feature_space_*.out</li>
 * <li></li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class FeatureSpaceTextGeneration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private List<String> listAttributes = Arrays.asList("name", "description");

  private String inputPositive = DEFAULT_PATH + "/eventPositive.nq";
  private String inputNegative = DEFAULT_PATH + "/eventNegativeNew.nq";

  private String outputPath = DEFAULT_PATH;

  private LanguageType langType = LanguageType.EN;

  private int kName = 3;
  private int kDesc = 3;
  private boolean useWordName = false;
  private boolean useWordDesc = false;

  private String outputPrefix = "feature_space_";
  private String[] arrOutputFeatureFiles = null;
  // DEFAULT_PATH + "/feature_space_name.out";
  // DEFAULT_PATH + "/feature_space_description.out";
  private Map<String, EventModel> mapEventPositive = new HashMap<>(0);
  private Map<String, EventModel> mapEventNegative = new HashMap<>(0);

  private Map<String, Map<String, Double>> mapFeatureSpaces = new HashMap<>(0);

  // private int countName0 = 0;
  // private int countDesc0 = 0;

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

  /**
   * Set N-Gram for name.
   * 
   * @param k
   */
  public void setNameK(int k) {
    this.kName = k;
    System.out.println("Set k for name: " + this.kName);
  }

  /**
   * Set N-Gram for description.
   * 
   * @param k
   */
  public void setDescK(int k) {
    this.kDesc = k;
    System.out.println("Set k for description: " + this.kDesc);
  }

  /**
   * Set to use word or character n-gram for name.
   * 
   * @param use
   */
  public void setUseWordName(boolean use) {
    this.useWordName = use;
    System.out.println(
        "Use " + (this.useWordName ? "word" : "character") + " " + kName + "-Gram for name.");
  }

  /**
   * Set to use word or character n-gram for description.
   * 
   * @param use
   */
  public void setUseWordDesc(boolean use) {
    this.useWordDesc = use;
    System.out.println("Use " + (this.useWordDesc ? "word" : "character") + " " + kDesc
        + "-Gram for description.");
  }

  public void setLang(LanguageType type) {
    this.langType = type;
    System.out.println("Set language: " + this.langType);
  }

  public FeatureSpaceTextGeneration() {
    this.init();
  }

  public void init() {
    System.out.println("FeatureSpaceTextGeneration init...");
    arrOutputFeatureFiles = new String[listAttributes.size()];
    mapFeatureSpaces.clear();
    for (int i = 0; i < listAttributes.size(); i++) {
      arrOutputFeatureFiles[i] = DEFAULT_PATH + "/" + outputPrefix + listAttributes.get(i) + ".out";
      mapFeatureSpaces.put(listAttributes.get(i), new HashMap<String, Double>(0));
    }
  }

  /**
   * Read files.
   */
  public void readFeaturesFromFiles() {
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
          mapEventPositive.put(nodeid, new EventModel(nodeid));
        }
        String predicate = nx[1].getLabel();
        EventModel event = mapEventPositive.get(nodeid);
        if (predicate != null && predicate.endsWith("/name")) {
          String name = nx[2].toString();
          if (event.name == null || event.name.length() < name.length()) {
            event.name = name;
          }
        }
        if (predicate != null && predicate.endsWith("/description")) {
          String description = nx[2].toString();
          if (event.desc == null || event.desc.length() < description.length()) {
            event.desc = description;
          }
        }
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
          mapEventNegative.put(nodeid, new EventModel(nodeid));
        }
        String predicate = nx[1].getLabel();
        EventModel event = mapEventNegative.get(nodeid);
        if (predicate != null && predicate.endsWith("/name")) {
          String name = nx[2].toString();
          if (event.name == null || event.name.length() < name.length()) {
            event.name = name;
          }
        }
        if (predicate != null && predicate.endsWith("/description")) {
          String description = nx[2].toString();
          if (event.desc == null || event.desc.length() < description.length()) {
            event.desc = description;
          }
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    System.out.println("Negative data size: " + mapEventNegative.size());
    System.out.println("Sample: " + mapEventNegative.values().iterator().next());
  }

  /**
   * Generate the features.
   */
  public void generateTextFeatures() {
    System.out.println("Generating features from positive...");
    this.generateTextFeatures(mapEventPositive);
    System.out.println("Generating features from negative...");
    this.generateTextFeatures(mapEventNegative);
    int sizePositive = mapEventPositive.size();
    int sizeNegative = mapEventNegative.size();
    int sizeDocument = sizePositive + sizeNegative;
    System.out.println("Updating features using IDF(=log(D/d))...");
    for (Entry<String, Map<String, Double>> entry : mapFeatureSpaces.entrySet()) {
      Map<String, Double> map = entry.getValue();
      for (String key : map.keySet()) {
        double value = Math.log(1.0 * sizeDocument / map.get(key));
        map.put(key, value);
      }
    }
  }

  private void generateTextFeatures(Map<String, EventModel> mapEvent) {
    for (Entry<String, EventModel> entry : mapEvent.entrySet()) {
      // 1. generate features from name:
      Map<String, Double> mapVocabularyName = mapFeatureSpaces.get(listAttributes.get(0));
      String name = NGramUtils.removePunctuaion(entry.getValue().name); // remove punctuation
      name = name.toLowerCase(); // case-insensitive
      List<String> nGramsName = new ArrayList<>(0);
      if (this.useWordName) { // use word N-Grams
        if (kName == 1) {
          if (this.langType == LanguageType.EN) {
            for (String d : TokenizationUtils.tokenizeTextEN(name)) {
              if (!nGramsName.contains(d) && d != null && !"".equalsIgnoreCase(d)) {
                nGramsName.add(d);
              }
            }
          } else if (this.langType == LanguageType.DE) {
            for (String d : TokenizationUtils.tokenizeTextDE(name)) {
              if (!nGramsName.contains(d) && d != null && !"".equalsIgnoreCase(d)) {
                nGramsName.add(d);
              }
            }
          }
        } else {
          for (String d : NGramUtils.getNGramsByWord(name, kName)) {
            if (!nGramsName.contains(d) && d != null && !"".equalsIgnoreCase(d)) {
              nGramsName.add(d);
            }
          }
        }
      } else { // use character N-Grams
        for (String n : NGramUtils.getNGrams(name, kName)) {
          if (!nGramsName.contains(n) && n != null && !"".equalsIgnoreCase(n)) {
            nGramsName.add(n);
          }
        }
      }
      for (String n : nGramsName) {
        Double countName = mapVocabularyName.get(n);
        mapVocabularyName.put(n, (countName == null) ? 1 : countName + 1);
      }
      // 2. generate features from description:
      Map<String, Double> mapVocabularyDesc = mapFeatureSpaces.get(listAttributes.get(1));
      String desc = NGramUtils.removePunctuaion(entry.getValue().desc); // remove punctuation
      desc = desc.toLowerCase(); // case-insensitive
      List<String> nGramsDesc = new ArrayList<>(0);
      if (this.useWordDesc) { // use word N-Grams
        if (kDesc == 1) {
          if (this.langType == LanguageType.EN) {
            for (String d : TokenizationUtils.tokenizeTextEN(desc)) {
              if (!nGramsDesc.contains(d) && d != null && !"".equalsIgnoreCase(d)) {
                nGramsDesc.add(d);
              }
            }
          } else if (this.langType == LanguageType.DE) {
            for (String d : TokenizationUtils.tokenizeTextDE(desc)) {
              if (!nGramsDesc.contains(d) && d != null && !"".equalsIgnoreCase(d)) {
                nGramsDesc.add(d);
              }
            }
          }
        } else {
          for (String d : NGramUtils.getNGramsByWord(desc, kDesc)) {
            if (!nGramsDesc.contains(d) && d != null && !"".equalsIgnoreCase(d)) {
              nGramsDesc.add(d);
            }
          }
        }
      } else { // use character N-Grams
        for (String d : NGramUtils.getNGrams(desc, kDesc)) {
          if (!nGramsDesc.contains(d) && d != null && !"".equalsIgnoreCase(d)) {
            nGramsDesc.add(d);
          }
        }
      }
      for (String d : nGramsDesc) {
        Double countDesc = mapVocabularyName.get(d);
        mapVocabularyDesc.put(d, (countDesc == null) ? 1 : countDesc + 1);
      }
    }
  }

  /**
   * write the features into files.
   */
  public void writeFeatureSpace() {
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      for (int i = 0; i < arrOutputFeatureFiles.length; i++) {
        String output = arrOutputFeatureFiles[i];
        Map<String, Double> mapVocabulary = mapFeatureSpaces.get(listAttributes.get(i));
        fWriter = new FileWriter(output);
        bWriter = new BufferedWriter(fWriter);
        System.out.println(listAttributes.get(i) + " vocabulary size: " + mapVocabulary.size());
        System.out.println("Writing N-Grams vocabularies into " + output + " ...");
        for (Entry<String, Double> entry : mapVocabulary.entrySet()) {
          bWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        if (bWriter != null) {
          bWriter.close();
        }
        if (fWriter != null) {
          fWriter.close();
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
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public Map<String, EventModel> getEventPositive() {
    return mapEventPositive;
  }

  public Map<String, EventModel> getEventNegative() {
    return mapEventNegative;
  }

  public Map<String, Map<String, Double>> getMapFeatureSpaces() {
    return mapFeatureSpaces;
  }

  public static void main(String... strings) {
    String parameters =
        " [positive] [negative] [outputPath] [outputPrefix] [kName,true|false] [kDesc,true|false] [en|de]";
    FeatureSpaceTextGeneration fsg = new FeatureSpaceTextGeneration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        System.out.println("java " + fsg.getClass().getName() + parameters);
        return;
      }
      fsg.setPositive(strings[0]);
    }
    if (strings.length >= 2) {
      fsg.setNegative(strings[1]);
    }
    if (strings.length >= 3) {
      fsg.setOutputPath(strings[2]);
    }
    if (strings.length >= 4) {
      fsg.setOutputPrefix(strings[3]);
    }
    if (strings.length >= 5) {
      if (!strings[4].contains(",")) {
        System.out.println("java " + fsg.getClass().getName() + parameters);
        return;
      }
      String[] strName = strings[4].split(",");
      int kName = 3;
      try {
        kName = Integer.parseInt(strName[0]);
      } catch (Exception e) {
        kName = 3;
      }
      fsg.setNameK(kName);
      if ("true".equalsIgnoreCase(strName[1])) {
        fsg.setUseWordName(true);
      }
    }
    if (strings.length >= 6) {
      if (!strings[5].contains(",")) {
        System.out.println("java " + fsg.getClass().getName() + parameters);
        return;
      }
      String[] strDesc = strings[5].split(",");
      int kDesc = 3;
      try {
        kDesc = Integer.parseInt(strDesc[0]);
      } catch (Exception e) {
        kDesc = 3;
      }
      fsg.setDescK(kDesc);
      if ("true".equalsIgnoreCase(strDesc[1])) {
        fsg.setUseWordDesc(true);
      }
    }
    if (strings.length >= 7) {
      if ("en".equalsIgnoreCase(strings[6])) {
        fsg.setLang(LanguageType.EN);
      } else if ("de".equalsIgnoreCase(strings[6])) {
        fsg.setLang(LanguageType.DE);
      } else {
        fsg.setLang(LanguageType.EN);
      }
    }
    fsg.init();
    fsg.readFeaturesFromFiles();
    fsg.generateTextFeatures();
    fsg.writeFeatureSpace();
  }

}
