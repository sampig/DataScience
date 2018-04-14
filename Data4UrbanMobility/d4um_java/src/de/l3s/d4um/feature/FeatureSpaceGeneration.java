/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.feature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.NGramUtils;

/**
 * Generate the features.<br/>
 * Input:
 * <ul>
 * <li>event_text_*.out</li>
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
public class FeatureSpaceGeneration {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private List<String> listAttributes = Arrays.asList("name", "description");

  private String inputFilename = DEFAULT_PATH + "/eventsIdsNewYorkPositive.tsv";

  private int kName = 3;
  private int kDesc = 3;
  private boolean useWord = false;

  private String[] arrTextFiles = null;
  // {DEFAULT_PATH + "/event_text_name.out"};
  // , DEFAULT_PATH + "/event_text_description.out"

  private String[] arrFeatureFiles = null;
  // DEFAULT_PATH + "/feature_space_name.out";
  // DEFAULT_PATH + "/feature_space_description.out";

  private List<String> arrFeatureList[] = null;

  public void setInput(String input) {
    this.inputFilename = input;
    System.out.println("Set input: " + this.inputFilename);
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

  @SuppressWarnings("unchecked")
  public FeatureSpaceGeneration() {
    arrTextFiles = new String[listAttributes.size()];
    arrFeatureFiles = new String[listAttributes.size()];
    arrFeatureList = new ArrayList[listAttributes.size()];
    for (int i = 0; i < listAttributes.size(); i++) {
      arrTextFiles[i] = DEFAULT_PATH + "/event_text_" + listAttributes.get(i) + ".out";
      arrFeatureFiles[i] = DEFAULT_PATH + "/feature_space_" + listAttributes.get(i) + ".out";
      arrFeatureList[i] = new ArrayList<String>(0);
    }
  }

  /**
   * Generate the features.
   */
  public void generateTextFeatures() {
    try {
      for (int i = 0; i < arrTextFiles.length; i++) {
        String input = arrTextFiles[i];
        List<String> listVocabulary = arrFeatureList[i];
        System.out.println("Reading " + input + " ...");
        File file = new File(input);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        if (i == 0) {// name
          while ((line = bufferedReader.readLine()) != null) {
            line = NGramUtils.removePunctuaion(line); // remove punctuation
            line = line.toLowerCase(); // case-insensitive
            List<String> nGrams = NGramUtils.getNGrams(line, kName);
            for (String gram : nGrams) {
              if (listVocabulary.contains(gram)) {
                continue;
              }
              listVocabulary.add(gram);
            }
          }
        } else if (i == 1) {// description
          if (this.useWord) {
            while ((line = bufferedReader.readLine()) != null) {
              line = NGramUtils.removePunctuaion(line); // remove punctuation
              line = line.toLowerCase(); // case-insensitive
              List<String> nGrams = NGramUtils.getNGramsByWord(line, kDesc);
              for (String gram : nGrams) {
                if (listVocabulary.contains(gram)) {
                  continue;
                }
                listVocabulary.add(gram);
              }
            }
          } else {
            while ((line = bufferedReader.readLine()) != null) {
              line = NGramUtils.removePunctuaion(line); // remove punctuation
              line = line.toLowerCase(); // case-insensitive
              List<String> nGrams = NGramUtils.getNGrams(line, kDesc);
              for (String gram : nGrams) {
                if (listVocabulary.contains(gram)) {
                  continue;
                }
                listVocabulary.add(gram);
              }
            }
          }
        }
        bufferedReader.close();
        fileReader.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * write the features into files.
   */
  public void writeFeatureSpace() {
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      for (int i = 0; i < arrFeatureFiles.length; i++) {
        String output = arrFeatureFiles[i];
        List<String> listVocabulary = arrFeatureList[i];
        fWriter = new FileWriter(output);
        bWriter = new BufferedWriter(fWriter);
        System.out.println(listAttributes.get(i) + " vocabulary size: " + listVocabulary.size());
        System.out.println("Writing N-Grams vocabularies into " + output + " ...");
        for (String v : listVocabulary) {
          bWriter.write(v + "\n");
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

  public List<String>[] getListVocabulary() {
    return arrFeatureList;
  }

  public static void main(String... strings) {
    FeatureSpaceGeneration fsg = new FeatureSpaceGeneration();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [kName,kDesc] [true|false]";
        System.out.println("java " + fsg.getClass().getName() + parameters);
        return;
      }
      if (!strings[0].contains(",")) {
        String parameters = " [kName,kDesc] [true|false]";
        System.out.println("java " + fsg.getClass().getName() + parameters);
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
      fsg.setNameK(kName);
      fsg.setDescK(kDesc);
    }
    if (strings.length >= 2) {
      if ("true".equalsIgnoreCase(strings[1])) {
        fsg.setUseWord(true);
      }
    }
    fsg.generateTextFeatures();
    fsg.writeFeatureSpace();
  }

}
