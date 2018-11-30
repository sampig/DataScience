/*
 * 
 */
package org.zhuzhu.application.sap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.zhuzhu.application.sap.utils.Constants;
import org.zhuzhu.application.sap.utils.FileUtils;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * <ul>
 * <li>Task 2.2: Entity recognition using Stanford NLP.</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EntityDetector2 {

  private static String namePerson = "Character";
  private static String nameLocation = "Location";

  private String inputFile = null;
  private String outputPath = null;

  private Map<String, List<String>> mapEntities = new HashMap<>(0);
  private List<String> listEntities = new ArrayList<>(0);
  private String outputFile = null;
  // private String outputFilePerson = null;
  // private String outputFileLocation = null;

  public EntityDetector2() {
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
    this.outputFile = path + "/ne_all_stanford.out";
    // this.outputFilePerson = path + "/ne_person_stanford.out";
    // this.outputFileLocation = path + "/ne_location_stanford.out";
  }

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
        if ("".equalsIgnoreCase(line)) {
          continue;
        }
        for (Entry<String, String> entry : this.findEntities(line).entrySet()) {
          listEntities.add(entry.getKey() + "," + entry.getValue());
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
    this.printSummary();
  }

  public Map<String, String> findEntities(String text) {
    Map<String, String> map = new HashMap<>(0);
    // set up pipeline properties
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
    // disable fine grained ner
    // props.setProperty("ner.applyFineGrained", "false");
    // customize fine grained ner
    // props.setProperty("ner.fine.regexner.mapping", "example.rules");
    // props.setProperty("ner.fine.regexner.ignorecase", "true");
    // add additional rules
    // props.setProperty("ner.additional.regexner.mapping", "example.rules");
    // props.setProperty("ner.additional.regexner.ignorecase", "true");
    // add 2 additional rules files ; set the first one to be case-insensitive
    // props.setProperty("ner.additional.regexner.mapping",
    // "ignorecase=true,example_one.rules;example_two.rules");
    // set up pipeline
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    // make an example document
    CoreDocument doc = new CoreDocument(text);
    // annotate the document
    pipeline.annotate(doc);
    // view results
    // System.out.println("---");
    // System.out.println("entities found");
    for (CoreEntityMention em : doc.entityMentions()) {
      map.put(em.text(), em.entityType());
      // System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
    }
    // System.out.println("---");
    // System.out.println("tokens and ner tags");
    // String tokensAndNERTags =
    // doc.tokens().stream().map(token -> "(" + token.word() + "," + token.ner() + ")")
    // .collect(Collectors.joining(" "));
    // System.out.println(tokensAndNERTags);
    return map;
  }

  public void printSummary() {
    System.out.println("========== Stanford NLP ===========");
    System.out.println("Number of entities: " + listEntities.size());
  }

  public void saveData() {
    String textEntities = null;
    for (String str : listEntities) {
      textEntities += str + "\n";
    }
    FileUtils.writeToFile(outputFile, textEntities);
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
      EntityDetector2 ed = new EntityDetector2();
      ed.setInput(inputFile);
      ed.setOutputPath(outputFilePath);
      ed.parseDocument();
      ed.saveData();
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }
  }

}
