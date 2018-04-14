/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2018 - L3S
 */
package de.l3s.d4um.classifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import de.l3s.d4um.feature.FeatureSpacePropertyGeneration;
import de.l3s.d4um.utils.ConfigUtils;
import weka.classifiers.AbstractClassifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

/**
 * 
 * @author Chenfeng Zhu
 *
 */
public class SDTypeClassifier extends AbstractClassifier {

  private static final long serialVersionUID = -5743705808226976692L;

  protected Instances m_Instances;
  protected int m_NumAttributes;
  protected int m_NumClasses;

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();
  protected String inputPositive = DEFAULT_PATH + "/eventPositive.nq";
  protected String inputNegative = DEFAULT_PATH + "/eventNegativeNew.nq";
  private List<Map<String, Double>> listEventPositive = new ArrayList<>(0);
  private List<Map<String, Double>> listEventNegative = new ArrayList<>(0);
  // private List<String> listProperties = new ArrayList<>(0);
  private Map<String, Double> mapPropertyPositive = new HashMap<>(0);
  private Map<String, Double> mapPropertyNegative = new HashMap<>(0);
  private Map<String, Double> mapFeatureSpaces = new HashMap<>(0);

  private double percentPositive = 0;
  private double percentNegative = 0;
  private double sumWeight = 0;

  /**
   * Get features.
   * 
   * @see de.l3s.d4um.feature.FeatureSpacePropertyGeneration
   */
  public void initFeatures() {
    FeatureSpacePropertyGeneration fspg = new FeatureSpacePropertyGeneration();
    fspg.setPositive(this.inputPositive);
    fspg.setNegative(this.inputNegative);
    fspg.readPropertiesFromFiles();
    fspg.generatePropertyFeatures();
    this.mapPropertyPositive = fspg.getPropertyPositive();
    this.mapPropertyNegative = fspg.getPropertyNegative();
    this.mapFeatureSpaces = fspg.getMapFeatures();
    for (Entry<String, Double> entry : mapFeatureSpaces.entrySet()) {
      sumWeight += entry.getValue();
    }
    System.out.println("Attributes size: " + this.mapFeatureSpaces.size());
    System.out.println("Sum weight: " + this.sumWeight);
  }

  public String globalInfo() {
    return "SD-Type algorithm by Heiko Paulheim";
  }

  @Override
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    // attributes
    // result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);
    // class
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    // instances
    result.setMinimumNumberInstances(0);
    return result;
  }

  @Override
  public void buildClassifier(Instances data) throws Exception {
    System.out.println("buildClassifier: " + data.size());
    m_Instances = new Instances(data);
    m_NumAttributes = data.numAttributes();
    m_NumClasses = data.numClasses();
    this.calculateWeight();
  }

  /**
   * Calculate the weight of every property.
   */
  public void calculateWeight() {
    Enumeration<Instance> enu = m_Instances.enumerateInstances();
    mapPropertyPositive = new HashMap<>(0);
    mapPropertyNegative = new HashMap<>(0);
    listEventPositive = new ArrayList<>(0);
    listEventNegative = new ArrayList<>(0);
    while (enu.hasMoreElements()) {
      Instance instance = (Instance) enu.nextElement();
      Map<String, Double> map = new HashMap<>(0);
      for (int i = 0; i < m_NumAttributes - 1; i++) {
        String property = instance.attribute(i).name();
        double tf = instance.value(i);
        map.put(property, tf);
        if (tf == 0) {
          continue;
        }
        if (instance.classValue() == 0) {
          Double num = mapPropertyPositive.get(property);
          mapPropertyPositive.put(property, (num == null) ? tf : num + tf);
        } else {
          Double num = mapPropertyNegative.get(property);
          mapPropertyNegative.put(property, (num == null) ? tf : num + tf);
        }
      }
      if (instance.classValue() == 0) {
        this.listEventPositive.add(map);
      } else {
        this.listEventNegative.add(map);
      }
    }
    // Calculate the percentages.
    double totalNumber = this.listEventPositive.size() + this.listEventNegative.size();
    this.percentPositive = this.listEventPositive.size() / totalNumber;
    this.percentNegative = this.listEventNegative.size() / totalNumber;
    // Calculate the weight for every property.
    for (int i = 0; i < m_NumAttributes - 1; i++) {
      String property = m_Instances.attribute(i).name();
      double numPositive =
          (mapPropertyPositive.get(property) == null) ? 0 : mapPropertyPositive.get(property);
      double numNegative =
          (mapPropertyNegative.get(property) == null) ? 0 : mapPropertyNegative.get(property);
      double numEvents = numPositive + numNegative;
      double weight = Math.pow((percentPositive - numPositive / numEvents), 2)
          + Math.pow((percentNegative - numNegative / numEvents), 2);
      mapFeatureSpaces.put(property, weight);
    }
    for (Entry<String, Double> entry : mapFeatureSpaces.entrySet()) {
      sumWeight += entry.getValue();
    }
    System.out.println("Data: " + listEventPositive.size() + ", " + listEventNegative.size());
    System.out.println("Feature spaces: " + mapFeatureSpaces);
    System.out.println("Sum weight: " + sumWeight);
  }

  // this method lets you to test the instance using the classifier
  public double classifyInstance(Instance instance) {
    double classValue = 0;
    double confPositive = calculateConfidence(instance, 0);
    double confNegative = calculateConfidence(instance, 1);
    if (confPositive >= confNegative) {
      classValue = 0;
    } else {
      classValue = 1;
    }
    return classValue;
  }

  /**
   * Calculate the confidence.
   * 
   * @param instance
   * @param typevalue
   * @return
   */
  public double calculateConfidence(Instance instance, double typevalue) {
    double value = 0;
    double v = 0;
    for (int i = 0; i < m_NumAttributes - 1; i++) {
      String property = instance.attribute(i).name();
      double tf = instance.value(i);
      if (tf == 0) {
        continue;
      }
      double numPositive =
          (mapPropertyPositive.get(property) == null) ? 0 : mapPropertyPositive.get(property);
      double numNegative =
          (mapPropertyNegative.get(property) == null) ? 0 : mapPropertyNegative.get(property);
      double numTotal = numPositive + numNegative;
      double weight = mapFeatureSpaces.get(property);
      if (typevalue == 0) {
        value += tf * weight * (numPositive / numTotal);
      } else if (typevalue == 1) {
        value += tf * weight * (numNegative / numTotal);
      } else {
      }
      v += tf * weight;
    }
    value = value / v;
    return value;
  }

  @Override
  public Enumeration<Option> listOptions() {
    Vector<Option> newVector = new Vector<Option>(3);
    newVector.addElement(new Option("\tSet positive data file.\n", "P", 0, "-P"));
    newVector.addElement(new Option("\tSet negative data file.\n", "N", 0, "-N"));
    newVector.addAll(Collections.list(super.listOptions()));
    return newVector.elements();
  }

  @Override
  public void setOptions(String[] options) throws Exception {
    super.setOptions(options);
    if (Utils.getOption('P', options) != null && !"".equals(Utils.getOption('P', options))) {
      this.inputPositive = Utils.getOption('P', options);
    }
    if (Utils.getOption('N', options) != null && !"".equals(Utils.getOption('N', options))) {
      this.inputNegative = Utils.getOption('N', options);
    }
    Utils.checkForRemainingOptions(options);
  }

  // this function is used to show the list of the parameter options
  @Override
  public String[] getOptions() {
    Vector<String> options = new Vector<String>();
    Collections.addAll(options, super.getOptions());
    options.add("-P");
    options.add("-N");
    return options.toArray(new String[0]);
  }

  public static void main(String[] argv) {
    SDTypeClassifier classifier = new SDTypeClassifier();
    try {
      boolean flag1 = false, flag2 = false;
      if (Utils.getOption('P', argv) != null && !"".equals(Utils.getOption('P', argv))) {
        classifier.inputPositive = Utils.getOption('P', argv);
        flag1 = true;
      }
      if (Utils.getOption('N', argv) != null && !"".equals(Utils.getOption('N', argv))) {
        classifier.inputNegative = Utils.getOption('N', argv);
        flag2 = false;
      }
      if (flag1 && flag2) {
        classifier.initFeatures();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    runClassifier(classifier, argv);
  }

}
