/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.feature;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.l3s.d4um.data.model.EventModel;
import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.NGramUtils;
import de.l3s.d4um.utils.TokenizationUtils;
import de.l3s.d4um.utils.VectorUtils;
import de.l3s.d4um.utils.TokenizationUtils.LanguageType;
import de.l3s.d4um.utils.TokenizationUtils.TypeIDF;
import de.l3s.d4um.utils.TokenizationUtils.TypeTF;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

/**
 * Generate the vectors for events and save the ARFF file.
 * 
 * @author Chenfeng Zhu
 * @see de.l3s.d4um.feature.FeatureSpaceTextGeneration
 *
 */
public class VectorGeneration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private String inputPositive = DEFAULT_PATH + "/eventPositive.nq";
  private String inputNegative = DEFAULT_PATH + "/eventNegativeNew.nq";

  private String outputFile = DEFAULT_PATH + "/arff_text_sparse.arff";

  private LanguageType langType = LanguageType.EN;

  private int kName = 3;
  private int kDesc = 3;
  private boolean useWordName = false;
  private boolean useWordDesc = false;

  private TypeTF typeTFName = TypeTF.Raw;
  private TypeIDF typeIDFName = TypeIDF.Unary;
  private TypeTF typeTFDesc = TypeTF.Raw;
  private TypeIDF typeIDFDesc = TypeIDF.Unary;

  private Map<String, EventModel> mapEventPositive = new HashMap<>(0);
  private Map<String, EventModel> mapEventNegative = new HashMap<>(0);

  private Map<String, Map<String, Double>> mapFeatureSpaces = new HashMap<>(0);
  private List<String> listFeatures = new ArrayList<>(0);

  private String labelClass = "regional";
  private List<String> listClass = Arrays.asList("yes", "no");
  private ArrayList<Attribute> attributes = new ArrayList<>(0);
  private Instances dataSet;

  public void setPositive(String positive) {
    this.inputPositive = positive;
    System.out.println("Set positive input: " + this.inputPositive);
  }

  public void setNegative(String negative) {
    this.inputNegative = negative;
    System.out.println("Set negative input: " + this.inputNegative);
  }

  public void setOutput(String output) {
    this.outputFile = output;
    System.out.println("Set output file: " + this.outputFile);
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

  public void setTFName(TypeTF type) {
    this.typeTFName = type;
    System.out.println("Set type of TF for name: " + this.typeTFName);
  }

  public void setIDFName(TypeIDF type) {
    this.typeIDFName = type;
    System.out.println("Set type of IDF for name: " + this.typeIDFName);
  }

  public void setTFDesc(TypeTF type) {
    this.typeTFDesc = type;
    System.out.println("Set type of TF for description: " + this.typeTFDesc);
  }

  public void setIDFDesc(TypeIDF type) {
    this.typeIDFDesc = type;
    System.out.println("Set type of IDF for description: " + this.typeIDFDesc);
  }

  public VectorGeneration() {
    System.out.println("VectorGeneration constructuring...");
  }

  /**
   * Get features.
   * 
   * @see de.l3s.d4um.feature.FeatureSpaceTextGeneration
   */
  public void initFeatures() {
    FeatureSpaceTextGeneration fsg = new FeatureSpaceTextGeneration();
    fsg.setPositive(this.inputPositive);
    fsg.setNegative(this.inputNegative);
    fsg.setNameK(this.kName);
    fsg.setDescK(this.kDesc);
    fsg.setUseWordName(this.useWordName);;
    fsg.setUseWordDesc(this.useWordDesc);
    fsg.setLang(this.langType);
    fsg.init();
    fsg.readFeaturesFromFiles();
    fsg.generateTextFeatures();
    this.mapEventPositive = fsg.getEventPositive();
    System.out.println("Positive events size: " + this.mapEventPositive.size());
    this.mapEventNegative = fsg.getEventNegative();
    System.out.println("Negative events size: " + this.mapEventNegative.size());
    this.mapFeatureSpaces = fsg.getMapFeatureSpaces();
  }

  /**
   * Set attributes for
   */
  public void setAttributes() {
    System.out.println("Feature space size: ");
    for (Entry<String, Map<String, Double>> entry : this.mapFeatureSpaces.entrySet()) {
      String key = entry.getKey();
      System.out.println("\t" + key + ": " + entry.getValue().size());
      for (Entry<String, Double> feature : entry.getValue().entrySet()) {
        if (feature.getValue() == 0) {
          continue;
        }
        String f = key + "_" + feature.getKey();
        listFeatures.add(f);
        attributes.add(new Attribute(f));
      }
    }
    System.out.println("Attributes size: " + attributes.size());
    attributes.add(new Attribute(labelClass, listClass));
    System.out.println("Attributes with label size: " + attributes.size());
    dataSet = new Instances("EventRelation", attributes, 0);
  }

  /**
   * Generate data.
   */
  public void generateData() {
    System.out.println("Generating data...");
    if (this.typeTFName == TypeTF.Raw) {
      System.out.println("\tTF(name)=f...");
    } else if (this.typeTFName == TypeTF.TF) {
      System.out.println("\tTF(name)=f/sum(f)...");
    }
    if (this.typeTFDesc == TypeTF.Raw) {
      System.out.println("\tTF(desc)=f...");
    } else if (this.typeTFDesc == TypeTF.TF) {
      System.out.println("\tTF(desc)=f/sum(f)...");
    }
    if (this.typeIDFName == TypeIDF.Unary) {
      System.out.println("\tIDF(name)=1...");
    } else if (this.typeIDFName == TypeIDF.IDF) {
      System.out.println("\tIDF(name)=log(D/d)...");
    }
    if (this.typeIDFDesc == TypeIDF.Unary) {
      System.out.println("\tIDF(desc)=1...");
    } else if (this.typeIDFDesc == TypeIDF.IDF) {
      System.out.println("\tIDF(desc)=log(D/d)...");
    }
    this.generateData(this.mapEventPositive, 0);
    this.generateData(this.mapEventNegative, 1);
    System.out.println("Data size dataSet: " + dataSet.size());
    // System.out.println(dataSet.get(0));
    // System.out.println(dataSet.get(dataSet.size() - 1));
  }

  private void generateData(Map<String, EventModel> mapEvent, int label) {
    System.out.println("Map event size: " + mapEvent.size());
    for (Entry<String, EventModel> entry : mapEvent.entrySet()) {
      double[] values = new double[dataSet.numAttributes()];
      String name = entry.getValue().name;
      String desc = entry.getValue().desc;
      if (name != null) {
        name = NGramUtils.removePunctuaion(name);
        name = name.toLowerCase();
        List<String> nGramsName = new ArrayList<>(0);
        Map<String, Double> map = new HashMap<>(0);
        if (this.useWordName) { // use word N-Grams
          if (kName == 1) {
            if (this.langType == LanguageType.EN) {
              nGramsName = TokenizationUtils.tokenizeTextEN(name);
            } else if (this.langType == LanguageType.DE) {
              nGramsName = TokenizationUtils.tokenizeTextDE(name);
            }
          } else {
            nGramsName = NGramUtils.getNGramsByWord(name, kName);
          }
        } else { // use character N-Grams
          nGramsName = NGramUtils.getNGrams(name, kName);
        }
        for (String gram : nGramsName) {
          String gName = "name_" + gram;
          if (!listFeatures.contains(gName)) {
            continue;
          }
          Double v = map.get(gName);
          map.put(gName, (v == null) ? 1 : v + 1);
        }
        for (Entry<String, Double> value : map.entrySet()) {
          double tf = 0;
          double idf = 0;
          if (this.typeTFName == TypeTF.Raw) {
            tf = value.getValue();
          } else if (this.typeTFName == TypeTF.TF) {
            tf = value.getValue() / nGramsName.size();
          }
          if (this.typeIDFName == TypeIDF.Unary) {
            idf = 1;
          } else if (this.typeIDFName == TypeIDF.IDF) {
            idf = mapFeatureSpaces.get("name").get(value.getKey().substring(5));
          }
          values[listFeatures.indexOf(value.getKey())] = tf * idf;
        }
      }
      if (desc != null) {
        desc = NGramUtils.removePunctuaion(desc);
        desc = desc.toLowerCase();
        List<String> nGramsDesc = new ArrayList<>(0);
        Map<String, Double> map = new HashMap<>(0);
        if (this.useWordDesc) {// use word N-Grams
          if (kDesc == 1) {
            if (this.langType == LanguageType.EN) {
              nGramsDesc = TokenizationUtils.tokenizeTextEN(desc);
            } else if (this.langType == LanguageType.DE) {
              nGramsDesc = TokenizationUtils.tokenizeTextDE(desc);
            }
          } else {
            nGramsDesc = NGramUtils.getNGramsByWord(desc, kDesc);
          }
        } else { // use character N-Grams
          nGramsDesc = NGramUtils.getNGrams(desc, kDesc);
        }
        for (String gram : nGramsDesc) {
          String gDesc = "description_" + gram;
          if (!listFeatures.contains(gDesc)) {
            continue;
          }
          Double v = map.get(gDesc);
          map.put(gDesc, (v == null) ? 1 : v + 1);
        }
        for (Entry<String, Double> value : map.entrySet()) {
          double tf = 0;
          double idf = 0;
          if (this.typeTFDesc == TypeTF.Raw) {
            tf = value.getValue();
          } else if (this.typeTFDesc == TypeTF.TF) {
            tf = value.getValue() / nGramsDesc.size();
          }
          if (this.typeIDFDesc == TypeIDF.Unary) {
            idf = 1;
          } else if (this.typeIDFDesc == TypeIDF.IDF) {
            idf = mapFeatureSpaces.get("description").get(value.getKey().substring(12));
          }
          values[listFeatures.indexOf(value.getKey())] = tf * idf;
        }
      }
      values = VectorUtils.normalizeVector(values);
      values[dataSet.numAttributes() - 1] = label;
      dataSet.add(new DenseInstance(1.0, values));
    }
  }

  /**
   * Sparse the ARFF file.
   */
  public void sparse() {
    System.out.println("Sparsing ARFF file...");
    NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse();
    try {
      nonSparseToSparseInstance.setInputFormat(dataSet);
      Instances sparseDataset = Filter.useFilter(dataSet, nonSparseToSparseInstance);
      ArffSaver arffSaverInstance = new ArffSaver();
      arffSaverInstance.setInstances(sparseDataset);
      arffSaverInstance.setFile(new File(this.outputFile));
      arffSaverInstance.writeBatch();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Sparsed file is saved in " + this.outputFile);
  }

  public static void main(String... strings) {
    String parameters =
        " [positive] [negative] [outputFile] [kName,true|false,b|r|t|l,u|i|s|m|p] [kDesc,true|false,b|r|t|l,u|i|s|m|p] [en|de]";
    VectorGeneration vg = new VectorGeneration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        System.out.println("java " + vg.getClass().getName() + parameters);
        System.out.println("TF weight:");
        System.out.println("\tb - Boolean");
        System.out.println("\tr - Raw Count");
        System.out.println("\tt - Adjusted for document length");
        System.out.println("IDF weight:");
        System.out.println("\tu - Unary 1");
        System.out.println("\ti - Normal log(N/n)");
        return;
      }
      vg.setPositive(strings[0]);
    }
    if (strings.length >= 2) {
      vg.setNegative(strings[1]);
    }
    if (strings.length >= 3) {
      vg.setOutput(strings[2]);
    }
    if (strings.length >= 4) {
      // [kName,true|false,b|r|t|l,u|i|s|m|p]
      if (!strings[3].contains(",")) {
        System.out.println("java " + vg.getClass().getName() + parameters);
        return;
      }
      String[] strName = strings[3].split(",");
      if (strName.length < 4) {
        System.out.println("java " + vg.getClass().getName() + parameters);
        return;
      }
      // set K.
      int kName = 3;
      try {
        kName = Integer.parseInt(strName[0]);
      } catch (Exception e) {
        kName = 3;
      }
      vg.setNameK(kName);
      // set Use Word.
      if ("true".equalsIgnoreCase(strName[1])) {
        vg.setUseWordName(true);
      }
      // set TF weight.
      if ("r".equalsIgnoreCase(strName[2])) {
        vg.setTFName(TypeTF.Raw);
      } else if ("t".equalsIgnoreCase(strName[2])) {
        vg.setTFName(TypeTF.TF);
      }
      // set IDF weight.
      if ("u".equalsIgnoreCase(strName[3])) {
        vg.setIDFName(TypeIDF.Unary);
      } else if ("i".equalsIgnoreCase(strName[3])) {
        vg.setIDFName(TypeIDF.IDF);
      }
    }
    if (strings.length >= 5) {
      // [kDesc,true|false,b|r|t|l,u|i|s|m|p]
      if (!strings[4].contains(",")) {
        System.out.println("java " + vg.getClass().getName() + parameters);
        return;
      }
      String[] strDesc = strings[4].split(",");
      if (strDesc.length < 4) {
        System.out.println("java " + vg.getClass().getName() + parameters);
        return;
      }
      int kDesc = 3;
      try {
        kDesc = Integer.parseInt(strDesc[0]);
      } catch (Exception e) {
        kDesc = 3;
      }
      vg.setDescK(kDesc);
      if ("true".equalsIgnoreCase(strDesc[1])) {
        vg.setUseWordDesc(true);
      }
      if ("r".equalsIgnoreCase(strDesc[2])) {
        vg.setTFDesc(TypeTF.Raw);
      } else if ("t".equalsIgnoreCase(strDesc[2])) {
        vg.setTFDesc(TypeTF.TF);
      }
      if ("u".equalsIgnoreCase(strDesc[3])) {
        vg.setIDFDesc(TypeIDF.Unary);
      } else if ("i".equalsIgnoreCase(strDesc[3])) {
        vg.setIDFDesc(TypeIDF.IDF);
      }
    }
    if (strings.length >= 6) {
      if ("en".equalsIgnoreCase(strings[5])) {
        vg.setLang(LanguageType.EN);
      } else if ("de".equalsIgnoreCase(strings[5])) {
        vg.setLang(LanguageType.DE);
      }
    }
    vg.initFeatures();
    vg.setAttributes();
    vg.generateData();
    vg.sparse();
  }

}
