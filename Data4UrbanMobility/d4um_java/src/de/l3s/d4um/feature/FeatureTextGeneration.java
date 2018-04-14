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
import java.util.List;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.NGramUtils;
import de.l3s.d4um.utils.PredicateUtils;
import de.l3s.d4um.utils.TokenizationUtils;

/**
 * Generate the features for text.<br/>
 * Input:
 * <ul>
 * <li>nquad_data_file(eventNameDescriptionDE.nq)</li>
 * </ul>
 * <br/>
 * Output:
 * <ul>
 * <li>feature_space_[attribute].out</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class FeatureTextGeneration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private List<String> listAttributes = Arrays.asList("name", "description");

  private int kName = 3;
  private String outputPrefix = "feature_space_";

  private String inputfile = DEFAULT_PATH + "/eventNameDescriptionDE.nq";
  private String[] outputFeatureFiles = null;

  private List<String> listEventIds = new ArrayList<>(0);
  private List<String>[] arrListFeatures = null;

  @SuppressWarnings("unchecked")
  public FeatureTextGeneration() {
    outputFeatureFiles = new String[listAttributes.size()];
    arrListFeatures = new List[listAttributes.size()];
    for (int i = 0; i < listAttributes.size(); i++) {
      outputFeatureFiles[i] = DEFAULT_PATH + "/" + outputPrefix + listAttributes.get(i) + ".out";
      arrListFeatures[i] = new ArrayList<String>(0);
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
   * Set the input file.
   * 
   * @param input
   */
  public void setInput(String input) {
    this.inputfile = input;
    System.out.println("Set input file: " + this.inputfile);
  }

  /**
   * Set the prefix for the output files.
   * 
   * @param prefix
   */
  public void setOutputPrefix(String prefix) {
    this.outputPrefix = prefix;
    System.out.println("Set output prefix: " + this.outputPrefix);
    this.resetOutput();
  }

  /**
   * Reset the output file.
   */
  private void resetOutput() {
    for (int i = 0; i < listAttributes.size(); i++) {
      outputFeatureFiles[i] = DEFAULT_PATH + "/" + outputPrefix + listAttributes.get(i) + ".out";
    }
  }

  /**
   * Generate the features.
   */
  public void generateFeatures() {
    FileInputStream is;
    try {
      is = new FileInputStream(this.inputfile);
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + inputfile + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String obj = nx[2].getLabel();// nx[2].toString();
        if (!PredicateUtils.isSchemaOrgEvent(predicate)) {
          continue;
        }
        predicate = PredicateUtils.fixSchemaOrg(predicate);
        if (!listEventIds.contains(nodeid)) {
          listEventIds.add(nodeid);
        }
        String attribute = PredicateUtils.getEventProperty(predicate);
        int index = listAttributes.indexOf(attribute);
        List<String> listVocabulary = new ArrayList<>(0);
        obj = NGramUtils.removePunctuaion(obj); // remove punctuation
        obj = obj.toLowerCase(); // case-insensitive
        if (index == 0) {
          listVocabulary = NGramUtils.getNGrams(obj, kName);
        } else if (index == 1) {
          listVocabulary = TokenizationUtils.tokenizeTextDE(obj);
        }
        for (String str : listVocabulary) {
          if (!arrListFeatures[index].contains(str)) {
            arrListFeatures[index].add(str);
          }
        }
      }
      System.out.println("Total number of events: " + listEventIds.size());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Write the features into files.
   */
  public void saveFeatureSpace() {
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      for (int i = 0; i < outputFeatureFiles.length; i++) {
        String output = outputFeatureFiles[i];
        List<String> listVocabulary = arrListFeatures[i];
        fWriter = new FileWriter(output);
        bWriter = new BufferedWriter(fWriter);
        System.out.println(listAttributes.get(i) + " vocabulary size: " + listVocabulary.size());
        System.out.println("Writing vocabularies into " + output + " ...");
        System.out.println("First feature: " + listVocabulary.get(0));
        for (String v : listVocabulary) {
          bWriter.write(v + "\n");
        }
        if (bWriter != null) {
          bWriter.close();
        }
        if (fWriter != null) {
          fWriter.close();
        }
        System.out.println("Last feature: " + listVocabulary.get(listVocabulary.size() - 1));
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
    FeatureTextGeneration ftg = new FeatureTextGeneration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [kName] [outputPrefix] [kDesc] [true|false]";
        System.out.println("java " + ftg.getClass().getName() + parameters);
        return;
      }
      ftg.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      int kName = 3;
      try {
        kName = Integer.parseInt(strings[1]);
      } catch (Exception e) {
        kName = 3;
      }
      ftg.setNameK(kName);
    }
    if (strings.length >= 3) {
      ftg.setOutputPrefix(strings[2]);
    }
    ftg.generateFeatures();
    ftg.saveFeatureSpace();
  }

}
