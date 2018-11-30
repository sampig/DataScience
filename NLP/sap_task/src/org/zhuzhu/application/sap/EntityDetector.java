/*
 * 
 */
package org.zhuzhu.application.sap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.zhuzhu.application.sap.utils.Constants;
import org.zhuzhu.application.sap.utils.FileUtils;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

/**
 * <ul>
 * <li>Task 2.1: Entity recognition using Apache OpenNLP.</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EntityDetector {

  private static String namePerson = "Character";
  private static String nameLocation = "Location";

  private String inputFile = null;
  private String outputPath = null;

  private Map<String, List<String>> mapEntities = new HashMap<>(0);
  private String outputFilePerson = null;
  private String outputFileLocation = null;

  public EntityDetector() {
    mapEntities.put(namePerson, new ArrayList<String>(0));
    mapEntities.put(nameLocation, new ArrayList<String>(0));
  }

  public void setInput(String input) {
    this.inputFile = input;
    System.out.println("Set input file: " + this.inputFile);
  }

  public void setOutputPath(String path) {
    this.outputPath = path;
    System.out.println("Set output file path: " + this.outputPath);
    this.outputFilePerson = path + "/ne_person_apache.out";
    this.outputFileLocation = path + "/ne_location_apache.out";
  }

  public void parseDocument() {
    FileReader fileReader = null;
    InputStreamReader inStreamReader = null;
    BufferedReader bReader = null;
    List<String> listPerson = new ArrayList<>(0);
    List<String> listLocation = new ArrayList<>(0);
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
        if ("".equalsIgnoreCase(line)) {
          continue;
        }
        listPerson.addAll(this.findNamePerson(line));
        listLocation.addAll(this.findNameLocation(line));
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
    mapEntities.put(namePerson, new ArrayList<String>(0));
    mapEntities.put(nameLocation, new ArrayList<String>(0));
    for (String pname : listPerson) {
      if (!mapEntities.get(namePerson).contains(pname)) {
        mapEntities.get(namePerson).add(pname);
      }
    }
    for (String lname : listLocation) {
      if (!mapEntities.get(nameLocation).contains(lname)) {
        mapEntities.get(nameLocation).add(lname);
      }
    }
    this.printSummary();
  }

  public void printSummary() {
    System.out.println("========== Apache OpenNLP ===========");
    System.out.println("Number of character: " + mapEntities.get(namePerson).size());
    System.out.println("Number of location: " + mapEntities.get(nameLocation).size());
  }

  /**
   * 
   * @param text
   * @return
   */
  public List<String> findNamePerson(String text) {
    List<String> list = new ArrayList<>(0);
    InputStream inputStream = getClass().getResourceAsStream("/apache/opennlp/en-ner-person.bin");
    TokenNameFinderModel model = null;
    NameFinderME nameFinder = null;
    try {
      model = new TokenNameFinderModel(inputStream);
      nameFinder = new NameFinderME(model);
      String[] tokens = tokenize(text);
      Span nameSpans[] = nameFinder.find(tokens);
      for (Span span : nameSpans) {
        list.add(tokens[span.getStart()]);
        // System.out.println("Person: "+tokens[s.getStart()]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * 
   * @param text
   * @return
   */
  public List<String> findNameLocation(String text) {
    List<String> list = new ArrayList<>(0);
    InputStream inputStreamNameFinder =
        getClass().getResourceAsStream("/apache/opennlp/en-ner-location.bin");
    TokenNameFinderModel model = null;
    NameFinderME locFinder = null;
    try {
      model = new TokenNameFinderModel(inputStreamNameFinder);
      locFinder = new NameFinderME(model);
      String[] tokens = tokenize(text);
      Span nameSpans[] = locFinder.find(tokens);
      for (Span span : nameSpans) {
        list.add(tokens[span.getStart()]);
        // System.out.println(
        // "Location: " + span.toString() + " LocationName - " + tokens[span.getStart()]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * 
   * @param text
   * @return
   */
  public String[] tokenize(String text) {
    InputStream inputStreamTokenizer =
        getClass().getResourceAsStream("/apache/opennlp/en-token.bin");
    TokenizerModel tokenModel = null;
    TokenizerME tokenizer = null;
    try {
      tokenModel = new TokenizerModel(inputStreamTokenizer);
      tokenizer = new TokenizerME(tokenModel);
      return tokenizer.tokenize(text);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void saveData() {
    String textPerson = null;
    String textLocation = null;
    for (String pname : mapEntities.get(namePerson)) {
      textPerson += pname + "\n";
    }
    for (String lname : mapEntities.get(nameLocation)) {
      textLocation += lname + "\n";
    }
    FileUtils.writeToFile(outputFilePerson, textPerson);
    FileUtils.writeToFile(outputFileLocation, textLocation);
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
      EntityDetector ed = new EntityDetector();
      ed.setInput(inputFile);
      ed.setOutputPath(outputFilePath);
      ed.parseDocument();
      ed.printSummary();
      ed.saveData();
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }
  }

}
