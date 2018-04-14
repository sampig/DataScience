/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.feature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.ConfigUtils.ConfigConstant;
import de.l3s.d4um.utils.NGramUtils;
import de.l3s.d4um.utils.PredicateUtils;
import de.l3s.d4um.utils.VectorUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

/**
 * 
 * @author Chenfeng Zhu
 *
 */
public class ARFFGeneration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private String inputfile =
      DEFAULT_PATH + "/" + ConfigUtils.getValue(ConfigConstant.DATA_EVENT_FILE_NAMEDESC_DE);
  private String[] arrFeatureFiles = null;
  // {DEFAULT_PATH + "/feature_name_vocabulary.out",
  // DEFAULT_PATH + "/feature_description_vocabulary.out"};
  // DEFAULT_PATH + "/feature_text_vocabulary.out";

  private String outputfile = DEFAULT_PATH + "/arff_text.arff";
  private String outputfileSparse = DEFAULT_PATH + "/arff_text_sparse.arff";

  private int kName = 3;
  private int kDesc = 3;
  private boolean useWord = false;

  // private String labelPLD = "PLD";
  // private List<String> listPLD =
  // Arrays.asList("www.coolibri.de", "www.freizeit.de", "www.esl.de", "www.ue30-party-portal.de",
  // "www.shmf.de", "termine.svz.de", "www.museumsfeldbahn.de", "museumsfeldbahn.de");
  private String labelClass = "regional";
  private List<String> listClass = Arrays.asList("yes", "no");

  private List<String> attributesName = Arrays.asList("name", "description");
  // private Map<String, List<String>> mapVocabularyList = new HashMap<>(0);
  private List<String> listFeatures = new ArrayList<>(0);

  private Map<String, Map<String, String>> mapEvents = new HashMap<>(0);

  public ARFFGeneration() {
    arrFeatureFiles = new String[attributesName.size()];
    for (int i = 0; i < attributesName.size(); i++) {
      arrFeatureFiles[i] = DEFAULT_PATH + "/feature_space_" + attributesName.get(i) + ".out";
    }
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
   * Set to use word or character n-gram.
   * 
   * @param use
   */
  public void setUseWord(boolean use) {
    this.useWord = use;
    System.out.println("Use " + (this.useWord ? "word" : "character") + " " + kDesc + "-Gram.");
  }

  private ArrayList<Attribute> attributes = new ArrayList<>(0);
  // private FastVector attsRel;
  // private FastVector attVals;
  // private FastVector attValsRel;
  private Instances dataSet;
  // private Instances dataRel;

  public void parseEventFile(String inputfile) {
    if (inputfile == null) {
      inputfile = this.inputfile;
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
        String obj = nx[2].getLabel();// nx[2].toString();
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
          continue;
        }
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        String attr = PredicateUtils.getEventProperty(predicate);
        if (!attributesName.contains(attr)) {
          continue;
        }
        // save the events.
        if (!mapEvents.containsKey(nodeid)) {
          Map<String, String> map = new HashMap<>(0);
          mapEvents.put(nodeid, map);
        }
        Map<String, String> map = mapEvents.get(nodeid);
        map.put(attr, obj);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void setAttributes() {
    try {
      for (int i = 0; i < arrFeatureFiles.length; i++) {
        String input = arrFeatureFiles[i];
        System.out.println("Reading " + input + " ...");
        File file = new File(input);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          line = line.replace("\n", "");
          line = attributesName.get(i) + "_" + line;
          if (listFeatures.contains(line)) {
            continue;
          }
          listFeatures.add(line);
        }
        bufferedReader.close();
        fileReader.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (String v : listFeatures) {
      attributes.add(new Attribute(v));
    }
    System.out.println("Attributes size: " + attributes.size());
    // attributes.add(new Attribute(labelPLD));
    attributes.add(new Attribute(labelClass, listClass));
    // attributes.add(new Attribute("Regional"));
    System.out.println("Attributes with label size: " + attributes.size());
    dataSet = new Instances("EventRelation", attributes, 0);
  }

  public void generate() {
    System.out.println("Generating data...");
    double[] values;
    int count = -1;
    for (Map.Entry<String, Map<String, String>> entry : mapEvents.entrySet()) {
      count++;
      // if (count >= 5000) {
      // break;
      // }
      Map<String, String> map = entry.getValue();
      String name = map.get("name");
      String desc = map.get("description");
      values = new double[dataSet.numAttributes()];
      if (name != null) {
        name = NGramUtils.removePunctuaion(name);
        name = name.toLowerCase();
        List<String> nGrams = NGramUtils.getNGrams(name, kName);
        for (String gram : nGrams) {
          String gName = "name_" + gram;
          if (!listFeatures.contains(gName)) {
            continue;
          }
          values[listFeatures.indexOf(gName)]++;
        }
      }
      if (desc != null) {
        desc = NGramUtils.removePunctuaion(desc);
        desc = desc.toLowerCase();
        if (this.useWord) {
          List<String> nGrams = NGramUtils.getNGramsByWord(desc, kDesc);
          for (String gram : nGrams) {
            String gDesc = "description_" + gram;
            if (!listFeatures.contains(gDesc)) {
              continue;
            }
            values[listFeatures.indexOf(gDesc)]++;
          }
        } else {
          List<String> nGrams = NGramUtils.getNGrams(desc, kDesc);
          for (String gram : nGrams) {
            String gDesc = "description_" + gram;
            if (!listFeatures.contains(gDesc)) {
              continue;
            }
            values[listFeatures.indexOf(gDesc)]++;
          }
        }
      }
      values = VectorUtils.normalizeVector(values);
      if (count < mapEvents.size() / 2) {
        values[dataSet.numAttributes() - 1] = 0;
      } else {
        values[dataSet.numAttributes() - 1] = 1;
      }
      // System.out.println(entry.getKey() + ": " + entry.getValue());
      dataSet.add(new DenseInstance(1.0, values));
    }
    System.out.println("Data size count: " + count);
    System.out.println("Data size dataSet: " + dataSet.size());
    System.out.println(dataSet.get(0));
    System.out.println(dataSet.get(dataSet.size() - 1));
  }

  public void sparse() {
    System.out.println("Sparsing...");
    NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse();
    try {
      nonSparseToSparseInstance.setInputFormat(dataSet);
      Instances sparseDataset = Filter.useFilter(dataSet, nonSparseToSparseInstance);

      // System.out.println(sparseDataset);

      ArffSaver arffSaverInstance = new ArffSaver();
      arffSaverInstance.setInstances(sparseDataset);
      arffSaverInstance.setFile(new File(outputfileSparse));
      arffSaverInstance.writeBatch();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Sparsed file is saved in " + outputfileSparse);
  }

  public void save() {
    System.out.println("Saving...");
    // System.out.println(dataSet);
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(outputfile);
      bWriter = new BufferedWriter(fWriter);
      bWriter.write(dataSet.toString());
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
    ARFFGeneration ag = new ARFFGeneration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [kName,kDesc] [true|false]";
        System.out.println("java " + ag.getClass().getName() + parameters);
        return;
      }
      if (!strings[0].contains(",")) {
        String parameters = " [kName,kDesc] [true|false]";
        System.out.println("java " + ag.getClass().getName() + parameters);
        return;
      }
      String[] strK = strings[0].split(",");
      int kName = 3;
      int kDesc = 3;
      try {
        kName = Integer.parseInt(strK[0]);
        kDesc = Integer.parseInt(strK[1]);
      } catch (Exception e) {
        kName = 3;
        kDesc = 3;
      }
      ag.setNameK(kName);
      ag.setDescK(kDesc);
    }
    if (strings.length >= 2) {
      if ("true".equalsIgnoreCase(strings[1])) {
        ag.setUseWord(true);
      }
    }
    ag.parseEventFile(null);
    ag.setAttributes();
    ag.generate();
    ag.sparse();
    // ag.save();
  }

}
