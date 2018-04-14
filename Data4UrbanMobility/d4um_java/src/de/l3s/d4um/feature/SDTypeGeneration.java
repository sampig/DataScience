/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2018 - L3S
 */
package de.l3s.d4um.feature;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.l3s.d4um.utils.ConfigUtils;
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
public class SDTypeGeneration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private String inputPositive = DEFAULT_PATH + "/eventPositive.nq";
  private String inputNegative = DEFAULT_PATH + "/eventNegativeNew.nq";

  private String outputFile = DEFAULT_PATH + "/arff_text_sparse_sdtype.arff";

  private Map<String, Map<String, Double>> mapEventPositive = new HashMap<>(0);
  private Map<String, Map<String, Double>> mapEventNegative = new HashMap<>(0);

  private List<String> listProperties = new ArrayList<>(0);
  // private Map<String, Double> mapFeatureSpaces = new HashMap<>(0);
  // private double sumWeight = 0;

  private String labelClass = "regional";
  private List<String> listClass = Arrays.asList("yes", "no"); // yes:0, no:1
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
   * Get features.
   * 
   * @see de.l3s.d4um.feature.FeatureSpacePropertyGeneration
   */
  public void initFeatures() {
    FeatureSpacePropertyGeneration fspg = new FeatureSpacePropertyGeneration();
    fspg.setPositive(this.inputPositive);
    fspg.setNegative(this.inputNegative);
    // fspg.setOutputPath();
    // fspg.setOutputPrefix();
    fspg.readPropertiesFromFiles();
    fspg.generatePropertyFeatures();
    this.mapEventPositive = fspg.getEventPositive();
    this.mapEventNegative = fspg.getEventNegative();
    this.listProperties = fspg.getListProperties();
    // this.mapFeatureSpaces = fspg.getMapFeatures();
    // for (Entry<String, Double> entry : mapFeatureSpaces.entrySet()) {
    // sumWeight += entry.getValue();
    // }
  }

  /**
   * Set attributes for
   */
  public void setAttributes() {
    System.out.println("Feature space size: " + this.listProperties.size());
    for (String property : listProperties) {
      attributes.add(new Attribute(property));
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
    System.out.println("Generating data (tf)..."); // * weight
    this.generateData(this.mapEventPositive, 0);
    this.generateData(this.mapEventNegative, 1);
    System.out.println("Data size dataSet: " + dataSet.size());
  }

  private void generateData(Map<String, Map<String, Double>> mapEvent, int label) {
    System.out.println("Map event size: " + mapEvent.size());
    for (Entry<String, Map<String, Double>> entry : mapEvent.entrySet()) {
      double[] values = new double[dataSet.numAttributes()];
      for (Entry<String, Double> event : entry.getValue().entrySet()) {
        values[this.listProperties.indexOf(event.getKey())] = event.getValue();
        // * this.mapFeatureSpaces.get(event.getKey());
      }
      // values = VectorUtils.normalizeVector(values);
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
    String parameters = " [positive] [negative] [outputFile]";
    SDTypeGeneration stg = new SDTypeGeneration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        System.out.println("java " + stg.getClass().getName() + parameters);
        return;
      }
      stg.setPositive(strings[0]);
    }
    if (strings.length >= 2) {
      stg.setNegative(strings[1]);
    }
    if (strings.length >= 3) {
      stg.setOutput(strings[2]);
    }
    stg.initFeatures();
    stg.setAttributes();
    stg.generateData();
    stg.sparse();
  }

}
