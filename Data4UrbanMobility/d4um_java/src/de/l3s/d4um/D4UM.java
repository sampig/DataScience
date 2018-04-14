/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um;

import de.l3s.d4um.data.DatasetExtractor;
import de.l3s.d4um.data.DatasetExtractorNegative;
import de.l3s.d4um.feature.FeatureSpaceTextGeneration;
import de.l3s.d4um.feature.VectorGeneration;
import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.TokenizationUtils.LanguageType;
import de.l3s.d4um.utils.TokenizationUtils.TypeIDF;
import de.l3s.d4um.utils.TokenizationUtils.TypeTF;

/**
 * The main program.
 * 
 * @author Chenfeng Zhu
 *
 */
public class D4UM {

  private String path = ConfigUtils.getDataDirectory();

  private String inputDataPositive = path + "/eventsQuadsNewYorkPositive.nq";
  private String inputIdPositive = path + "/eventsIdsNewYorkPositive.tsv";
  private String inputDataNegative = path + "/eventsQuadsNewYorkNegative.nq";
  private String inputIdNegative = path + "/eventsIdsNewYorkNegative.tsv";
  private String outputDataPositive = path + "/eventPositive.nq";
  private String outputDataNegative = path + "/eventNegativeNew.nq";
  private int size = 12420;

  private String dataPositive = outputDataPositive;
  private String dataNegative = outputDataNegative;
  private LanguageType langType = LanguageType.EN;
  private int kName = 3;
  private int kDesc = 3;
  private boolean useWord = false;
  private String outputPrefix = "feature_space_";

  private String outputARFF = path + "/arff_text_sparse.arff";
  private TypeTF typeTF = TypeTF.Raw;
  private TypeIDF typeIDF = TypeIDF.Unary;

  public void setPath(String path) {
    if (path == null) {
      return;
    } else if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    this.path = path;
    System.out.println("Set path: " + this.path);
  }

  public void setInputDataPositive(String inputDataPositive) {
    this.inputDataPositive = inputDataPositive;
    System.out.println("Set input data positive: " + this.inputDataPositive);
  }

  public void setInputIdPositive(String inputIdPositive) {
    this.inputIdPositive = inputIdPositive;
    System.out.println("Set input id positive: " + this.inputIdPositive);
  }

  public void setInputDataNegative(String inputDataNegative) {
    this.inputDataNegative = inputDataNegative;
    System.out.println("Set input data negative: " + this.inputDataNegative);
  }

  public void setInputIdNegative(String inputIdNegative) {
    this.inputIdNegative = inputIdNegative;
    System.out.println("Set input id negative: " + this.inputIdNegative);
  }

  public void setOutputDataPositive(String outputDataPositive) {
    this.outputDataPositive = outputDataPositive;
    System.out.println("Set output data positive: " + this.outputDataPositive);
  }

  public void setOutputDataNegative(String outputDataNegative) {
    this.outputDataNegative = outputDataNegative;
    System.out.println("Set output data negative: " + this.outputDataNegative);
  }

  public void setDataPositive(String dataPositive) {
    this.dataPositive = dataPositive;
  }

  public void setDataNegative(String dataNegative) {
    this.dataNegative = dataNegative;
  }

  public void setLangType(LanguageType langType) {
    this.langType = langType;
    System.out.println("Set language: " + this.langType);
  }

  public void setNameK(int kName) {
    this.kName = kName;
    System.out.println("Set k for name: " + this.kName);
  }

  public void setDescK(int kDesc) {
    this.kDesc = kDesc;
    System.out.println("Set k for description: " + this.kDesc);
  }

  public void setUseWord(boolean useWord) {
    this.useWord = useWord;
    System.out.println("Use " + (this.useWord ? "word" : "character") + " " + kDesc + "-Gram.");
  }

  public void setOutputPrefix(String outputPrefix) {
    this.outputPrefix = outputPrefix;
    System.out.println("Set output prefix: " + this.outputPrefix);
  }

  public void setOutputARFF(String outputARFF) {
    this.outputARFF = outputARFF;
    System.out.println("Set output ARFF file: " + this.outputARFF);
  }

  public void setTypeTF(TypeTF typeTF) {
    this.typeTF = typeTF;
    System.out.println("Set type of TF: " + this.typeTF);
  }

  public void setTypeIDF(TypeIDF typeIDF) {
    this.typeIDF = typeIDF;
    System.out.println("Set type of IDF: " + this.typeIDF);
  }

  /**
   * Extract positive and negative events.
   */
  public void extract() {
    String tmpNegative = "/eventNegativeTemp.nq";
    // Extract dataset.
    DatasetExtractor de = new DatasetExtractor();
    de.setId(inputIdPositive);
    de.setDataset(inputDataPositive);
    de.setOutput(outputDataPositive);
    de.readEventIds();
    de.extractData();
    size = de.getListEventIds().size();
    de = new DatasetExtractor();
    de.setId(inputIdNegative);
    de.setDataset(inputDataNegative);
    de.setOutput(path + tmpNegative);
    de.readEventIds();
    de.extractData();

    // Extract negative dataset.
    DatasetExtractorNegative den = new DatasetExtractorNegative();
    den.setIdNegative(inputIdNegative);
    den.setDataset(path + tmpNegative);
    den.setOutput(outputDataNegative);
    den.setSize(size);
    den.readEventIds();
    den.extractData();
  }

  /**
   * Save the features.
   */
  public void saveFeatures() {
    FeatureSpaceTextGeneration fsg = new FeatureSpaceTextGeneration();
    fsg.setPositive(this.dataPositive);
    fsg.setNegative(this.dataNegative);
    fsg.setOutputPath(this.path);
    fsg.setOutputPrefix(this.outputPrefix);
    fsg.setNameK(this.kName);
    fsg.setDescK(this.kDesc);
    fsg.setUseWordDesc(this.useWord);
    fsg.setLang(this.langType);
    fsg.init();
    fsg.readFeaturesFromFiles();
    fsg.generateTextFeatures();
    fsg.writeFeatureSpace();
  }

  /**
   * Generate the ARFF file.
   */
  public void generateARFFFile() {
    VectorGeneration vg = new VectorGeneration();
    vg.setPositive(this.dataPositive);
    vg.setNegative(this.dataNegative);
    vg.setOutput(this.outputARFF);
    vg.setNameK(this.kName);
    vg.setDescK(this.kDesc);
    vg.setUseWordDesc(this.useWord);
    vg.setLang(this.langType);
    vg.setTFDesc(this.typeTF);
    vg.setIDFDesc(this.typeIDF);
    vg.initFeatures();
    vg.setAttributes();
    vg.generateData();
    vg.sparse();
  }

  public static void main(String... strings) {
    String parameters = " [command]";
    // [path] [inputP] [inputN] [outputP] [outputN] [kName,kDesc] [true|false]
    String commands = "extract|features|arff";
    String strExtract = "[path] [inIdP] [inDataP] [outP] [inIdN] [inDataN] [outN]";
    String strFeature =
        " [positive] [negative] [outputPath] [outputPrefix] [kName,kDesc] [true|false] [en|de]";
    String strArff =
        " [positive] [negative] [outputFile] [kName,kDesc] [true|false] [en|de] [b|r|t|l] [u|i|s|m|p]";
    D4UM d4um = new D4UM();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        System.out.println("java " + d4um.getClass().getName() + parameters);
        System.out.println("\tcommands: " + commands);
        System.out.println("\t\textract " + strExtract);
        System.out.println("\t\tfeature " + strFeature);
        System.out.println("\t\tarff " + strArff);
        return;
      } else if (strings[0].contains("extract")) { // extract data
        if (strings.length < 8) {
          System.out.println("\t\textract: " + strExtract);
          return;
        }
        d4um.setPath(strings[1]);
        d4um.setInputIdPositive(strings[2]);
        d4um.setInputDataPositive(strings[3]);
        d4um.setOutputDataPositive(strings[4]);
        d4um.setInputIdNegative(strings[5]);
        d4um.setInputDataNegative(strings[6]);
        d4um.setOutputDataNegative(strings[7]);
        d4um.extract();
      } else if (strings[0].contains("features")) { // generate features
        if (strings.length < 8) {
          System.out.println("\t\tfeature " + strFeature);
          return;
        }
        d4um.setDataPositive(strings[1]);
        d4um.setDataNegative(strings[2]);
        d4um.setPath(strings[3]);
        d4um.setOutputPrefix(strings[4]);
        String[] strK = strings[5].split(",");
        int kName = 3;
        int kDesc = 3;
        try {
          kName = Integer.parseInt(strK[0]);
          kDesc = Integer.parseInt(strK[1]);
        } catch (Exception e) {
          kName = 3;
          kDesc = 3;
        }
        d4um.setNameK(kName);
        d4um.setDescK(kDesc);
        if ("true".equalsIgnoreCase(strings[6])) {
          d4um.setUseWord(true);
        }
        if ("en".equalsIgnoreCase(strings[7])) {
          d4um.setLangType(LanguageType.EN);
        } else if ("de".equalsIgnoreCase(strings[7])) {
          d4um.setLangType(LanguageType.DE);
        }
        d4um.saveFeatures();
      } else if (strings[0].contains("arff")) { // generate ARFF file
        if (strings.length < 8) {
          System.out.println("\t\tarff " + strArff);
          return;
        }
        d4um.setDataPositive(strings[1]);
        d4um.setDataNegative(strings[2]);
        d4um.setOutputARFF(strings[3]);
        String[] strK = strings[4].split(",");
        int kName = 3;
        int kDesc = 3;
        try {
          kName = Integer.parseInt(strK[0]);
          kDesc = Integer.parseInt(strK[1]);
        } catch (Exception e) {
          kName = 3;
          kDesc = 3;
        }
        d4um.setNameK(kName);
        d4um.setDescK(kDesc);
        if ("true".equalsIgnoreCase(strings[5])) {
          d4um.setUseWord(true);
        }
        if ("en".equalsIgnoreCase(strings[6])) {
          d4um.setLangType(LanguageType.EN);
        } else if ("de".equalsIgnoreCase(strings[6])) {
          d4um.setLangType(LanguageType.DE);
        }
        if (strings.length >= 8) {
          if ("r".equalsIgnoreCase(strings[7])) {
            d4um.setTypeTF(TypeTF.Raw);
          } else if ("t".equalsIgnoreCase(strings[7])) {
            d4um.setTypeTF(TypeTF.TF);
          }
        }
        if (strings.length >= 9) {
          if ("u".equalsIgnoreCase(strings[8])) {
            d4um.setTypeIDF(TypeIDF.Unary);
          } else if ("i".equalsIgnoreCase(strings[8])) {
            d4um.setTypeIDF(TypeIDF.IDF);
          }
        }
        d4um.generateARFFFile();
      }
    } else {
      d4um.extract();
    }
  }

}
