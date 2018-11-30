/*
 * 
 */
package org.zhuzhu.application.sap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.zhuzhu.application.sap.utils.Constants;
import org.zhuzhu.application.sap.utils.FileUtils;
import org.zhuzhu.application.sap.utils.NGramUtils;
import org.zhuzhu.application.sap.utils.TokenizationUtils;
import org.zhuzhu.application.sap.utils.VectorUtils;

/**
 * <ul>
 * <li>Task 1.1: Words Distribution.</li>
 * <li>Task 1.2: Phrases Distribution.</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class DistributionApplication {

  private String inputFile = null;
  private String outputPath = null;

  private long countTotalWords = 0;
  private Map<String, Integer> mapWordsRaw = new HashMap<>(0);
  private String outputFileWordsRaw = null;
  private Map<String, Integer> mapWordsWithoutPunct = new HashMap<>(0);
  private String outputFileWordsWithoutPunct = null;
  private Map<String, Integer> mapWordsWithoutPunct2 = new HashMap<>(0);
  private String outputFileWordsWithoutPunct2 = null;
  private Map<String, Integer> mapWords = new HashMap<>(0);
  private String outputFileWords = null;

  private Map<String, Integer> mapPhrases2WP = new HashMap<>(0);
  private String outputFilePhrases2WP = null;
  private Map<String, Integer> mapPhrases3WP = new HashMap<>(0);
  private String outputFilePhrases3WP = null;
  private Map<String, Integer> mapPhrases4WP = new HashMap<>(0);
  private String outputFilePhrases4WP = null;

  private Map<String, Integer> mapPhrasesSkip2WP = new HashMap<>(0);
  private String outputFilePhrasesSkip2WP = null;
  private Map<String, Integer> mapPhrasesSkip3WP = new HashMap<>(0);
  private String outputFilePhrasesSkip3WP = null;

  private int threshold = 2;
  private Map<String, Integer> mapPhrases = new HashMap<>(0);
  private String outputFilePhrases = null;

  public DistributionApplication() {}

  public DistributionApplication(String input) {
    this.setInput(input);
  }

  public void setInput(String input) {
    this.inputFile = input;
    System.out.println("Set input file: " + this.inputFile);
  }

  public void setOutputPath(String path) {
    this.outputPath = path;
    System.out.println("Set output file path: " + this.outputPath);
    // words
    this.outputFileWordsRaw = path + "/words_frequency_raw.out";
    this.outputFileWordsWithoutPunct = path + "/words_frequency_without_punct.out";
    this.outputFileWordsWithoutPunct2 = path + "/words_frequency_without_punct2.out";
    this.outputFileWords = path + "/words_frequency.out";
    // phrases
    this.outputFilePhrases2WP = path + "/phrases2_frequency_without_punct.out";
    this.outputFilePhrases3WP = path + "/phrases3_frequency_without_punct.out";
    this.outputFilePhrases4WP = path + "/phrases4_frequency_without_punct.out";
    this.outputFilePhrasesSkip2WP = path + "/phrases_skip2_frequency_without_punct.out";
    this.outputFilePhrasesSkip3WP = path + "/phrases_skip3_frequency_without_punct.out";
    this.outputFilePhrases = path + "/phrases_frequency.out";
  }

  public void setOutputFileWords(String output1, String output2, String output3) {
    this.outputFileWordsRaw = output1;
    this.outputFileWordsWithoutPunct = output2;
    this.outputFileWordsWithoutPunct2 = outputFileWordsWithoutPunct.replaceAll("\\.", "2.");
    this.outputFileWords = output3;
  }

  public void setOutputFilePhrases(String output1, String output2) {
    this.outputFilePhrases2WP = output1;
    this.outputFilePhrases3WP = output2;
  }

  /**
   * 
   */
  public void parseDocument() {
    FileReader fileReader = null;
    InputStreamReader inStreamReader = null;
    BufferedReader bReader = null;
    try {
      if (inputFile == null || "".equalsIgnoreCase(inputFile)) {
        System.out.println("Reading the text from local '" + Constants.TEXT_FILE_NAME + "'...");
        inStreamReader = new InputStreamReader(DistributionApplication.class.getClassLoader()
            .getResourceAsStream(Constants.TEXT_FILE_NAME));
        bReader = new BufferedReader(inStreamReader);
      } else {
        System.out.println("Reading the text from '" + inputFile + "'...");
        fileReader = new FileReader(inputFile);
        bReader = new BufferedReader(fileReader);
      }
      String line = null;
      while ((line = bReader.readLine()) != null) {
        // Deal with raw text.
        for (String word : line.split(" ")) {
          if (word == null || "".equalsIgnoreCase(word)) {
            continue;
          }
          countTotalWords++;
          Integer count = mapWordsRaw.get(word);
          mapWordsRaw.put(word, (count == null ? 1 : count + 1));
        }
        // Remove simple punctuation.
        String newline = TokenizationUtils.removePunctuationSimple(line.toLowerCase());
        // For words, do NOT consider stop words.
        for (String word : newline.split(" ")) {
          if (word == null || "".equalsIgnoreCase(word)) {
            continue;
          }
          if (!TokenizationUtils.LIST_STOP_WORDS_EN_MIN.contains(word)) {
            Integer count = mapWordsWithoutPunct.get(word);
            mapWordsWithoutPunct.put(word, (count == null ? 1 : count + 1));
          }
          if (!TokenizationUtils.LIST_STOP_WORDS_EN.contains(word)) {
            Integer count = mapWordsWithoutPunct2.get(word);
            mapWordsWithoutPunct2.put(word, (count == null ? 1 : count + 1));
          }
        }
        // After tokenization using Lucene.
        newline = TokenizationUtils.removePunctuation(line);
        for (String word : TokenizationUtils.tokenizeTextEN(newline)) {
          if (word == null || "".equalsIgnoreCase(word)) {
            continue;
          }
          Integer count = mapWords.get(word);
          mapWords.put(word, (count == null ? 1 : count + 1));
        }
        // Sentences:
        for (String sentence : line.split("\\. ")) {
          sentence = TokenizationUtils.removePunctuationSimple(sentence.toLowerCase());
          if (sentence == null || "".equalsIgnoreCase(sentence)) {
            continue;
          }
          for (String phrase : NGramUtils.getNGramsByWord(sentence, 2)) {
            Integer count = mapPhrases2WP.get(phrase);
            mapPhrases2WP.put(phrase, (count == null ? 1 : count + 1));
          }
          for (String phrase : NGramUtils.getNGramsByWord(sentence, 3)) {
            Integer count = mapPhrases3WP.get(phrase);
            mapPhrases3WP.put(phrase, (count == null ? 1 : count + 1));
          }
          for (String phrase : NGramUtils.getNGramsByWord(sentence, 4)) {
            Integer count = mapPhrases4WP.get(phrase);
            mapPhrases4WP.put(phrase, (count == null ? 1 : count + 1));
          }
          for (String phrase : NGramUtils.getSkipGramsByWord(sentence, 2)) {
            Integer count = mapPhrasesSkip2WP.get(phrase);
            mapPhrasesSkip2WP.put(phrase, (count == null ? 1 : count + 1));
          }
          for (String phrase : NGramUtils.getSkipGramsByWord(sentence, 3)) {
            Integer count = mapPhrasesSkip3WP.get(phrase);
            mapPhrasesSkip3WP.put(phrase, (count == null ? 1 : count + 1));
          }
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (bReader != null) {
          bReader.close();
        }
        if (fileReader != null) {
          fileReader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println("Read text: done.");
    }
    this.addPhrase(mapPhrases, mapPhrases2WP, false);
    this.addPhrase(mapPhrases, mapPhrases3WP, false);
    this.addPhrase(mapPhrases, mapPhrases4WP, false);
    this.addPhrase(mapPhrases, mapPhrasesSkip2WP, true);
    this.addPhrase(mapPhrases, mapPhrasesSkip3WP, true);
    this.printSummary();
  }

  public void addPhrase(Map<String, Integer> map, Map<String, Integer> map2, boolean skip) {
    if (!skip) {
      for (Entry<String, Integer> entry : map2.entrySet()) {
        String[] words = entry.getKey().split(" ");
        boolean flag = false;
        for (String word : words) {
          if (TokenizationUtils.LIST_STOP_WORDS_EN_MIN.contains(word)) {
            flag = true;
            break;
          }
        }
        if (flag) {
          continue;
        } else if (entry.getValue() > this.threshold) {
          map.put(entry.getKey(), entry.getValue());
        }
      }
    } else {
      for (Entry<String, Integer> entry : map2.entrySet()) {
        String[] words = entry.getKey().split("-");
        boolean flag = false;
        for (String word : words) {
          if (TokenizationUtils.LIST_STOP_WORDS_EN_MIN.contains(word)) {
            flag = true;
            break;
          }
        }
        if (flag) {
          continue;
        } else if (entry.getValue() > this.threshold) {
          map.put(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  /**
   * 
   */
  public void printSummary() {
    System.out.println("========== Words ==========");
    System.out.println("Number of total words: " + countTotalWords);
    System.out.println("Number of distinct raw words: " + mapWordsRaw.size());
    System.out.println("Number of words: " + mapWordsWithoutPunct.size());
    System.out.println("Number of meaningful words: " + mapWords.size());
    long count = 0;
    for (Entry<String, Integer> entry : mapWordsRaw.entrySet()) {
      count += entry.getValue();
    }
    System.out.println("Count: " + count);

    System.out.println("\n========== Phrases ==========");
    System.out.println("Number of phrases with 2-gram: " + mapPhrases2WP.size());
    System.out.println("Number of phrases with 3-gram: " + mapPhrases3WP.size());
    System.out.println("Number of phrases with 4-gram: " + mapPhrases4WP.size());
    System.out.println("Number of phrases with skip-2-gram: " + mapPhrasesSkip2WP.size());
    System.out.println("Number of phrases with skip-3-gram: " + mapPhrasesSkip3WP.size());
    System.out.println("Number of phrases: " + mapPhrases.size());
    System.out.println();
  }

  /**
   * 
   */
  public void saveData() {
    System.out.println("Saving data...");
    // Words.
    this.saveData(outputFileWordsRaw, mapWordsRaw);
    this.saveData(outputFileWordsWithoutPunct, mapWordsWithoutPunct);
    this.saveData(outputFileWordsWithoutPunct2, mapWordsWithoutPunct2);
    this.saveData(outputFileWords, mapWords);
    // Phrases.
    this.saveData(outputFilePhrases2WP, mapPhrases2WP);
    this.saveData(outputFilePhrases3WP, mapPhrases3WP);
    this.saveData(outputFilePhrases4WP, mapPhrases4WP);
    this.saveData(outputFilePhrasesSkip2WP, mapPhrasesSkip2WP);
    this.saveData(outputFilePhrasesSkip3WP, mapPhrasesSkip3WP);
    this.saveData(outputFilePhrases, mapPhrases);
    System.out.println("Save data: done.");
  }

  /**
   * 
   * @param output
   * @param map
   */
  private void saveData(String output, Map<String, Integer> map) {
    if (output != null && !"".equalsIgnoreCase(output)) {
      Map<String, Integer> map1 = VectorUtils.sortByValue(map, VectorUtils.SortBy.DESC);
      FileUtils.writeMapdataToFile(output, map1);
    }
  }

  /**
   * 
   */
  public void generateImage() {
    FileUtils.writeDataIntoWordCloud(outputFileWordsWithoutPunct + ".png", mapWordsWithoutPunct);
    FileUtils.writeDataIntoWordCloud(outputFileWordsWithoutPunct2 + ".png", mapWordsWithoutPunct2);
    FileUtils.writeDataIntoWordCloud(outputFilePhrases + ".png", mapPhrases);
  }

  public Map<String, Integer> getMapWordsRaw() {
    return mapWordsRaw;
  }

  public Map<String, Integer> getMapWordsWithoutPunct() {
    return mapWordsWithoutPunct;
  }

  public Map<String, Integer> getMapWordsWithoutPunct2() {
    return mapWordsWithoutPunct2;
  }

  public Map<String, Integer> getMapWords() {
    return mapWords;
  }

  public Map<String, Integer> getMapPhrases2WP() {
    return mapPhrases2WP;
  }

  public Map<String, Integer> getMapPhrases3WP() {
    return mapPhrases3WP;
  }

  public Map<String, Integer> getMapPhrases4WP() {
    return mapPhrases4WP;
  }

  public Map<String, Integer> getMapPhrasesSkip2WP() {
    return mapPhrasesSkip2WP;
  }

  public Map<String, Integer> getMapPhrasesSkip3WP() {
    return mapPhrasesSkip3WP;
  }

  public static void main(String... strings) {
    Options options = new Options();
    Option input = new Option("i", "input", true, "input file");
    input.setRequired(true);
    options.addOption(input);
    Option output = new Option("op", "outputpath", true, "output file path");
    output.setRequired(true);
    options.addOption(output);
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, strings);
      String inputFile = cmd.getOptionValue("input");
      String outputFilePath = cmd.getOptionValue("outputpath");
      DistributionApplication da = new DistributionApplication();
      da.setInput(inputFile);
      da.setOutputPath(outputFilePath);
      da.parseDocument();
      da.saveData();
      da.generateImage();
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }
  }

}
