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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.zhuzhu.application.sap.utils.Constants;
import org.zhuzhu.application.sap.utils.TokenizationUtils;
import org.zhuzhu.application.sap.utils.VectorUtils;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * <ul>
 * <li>Task 3: Classify sentiments of sentences.</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class SentimentsDetector {

  private String inputFile = null;
  private String inputModel = null;
  private String outputPath = null;
  private String inputFeatures = null;

  private String outputFileTest = null;
  private List<String> listSentences = new ArrayList<>(0);
  private Classifier classifier = null;
  private Instances dataSet = null;
  private List<String> listFeatures = new ArrayList<>(0);
  private List<String> listClass =
      Arrays.asList(new String[] {"empty", "sadness", "enthusiasm", "neutral", "worry", "surprise",
          "love", "fun", "hate", "happiness", "boredom", "relief", "anger"});

  public void setInput(String input) {
    this.inputFile = input;
    System.out.println("Set input file: " + this.inputFile);
  }

  public void setModel(String model) {
    this.inputModel = model;
    System.out.println("Set input model: " + this.inputModel);
  }

  public void setOutputPath(String path) {
    this.outputPath = path;
    System.out.println("Set output file path: " + this.outputPath);
    this.outputFileTest = path + "/sentiments_sentences.out";
    System.out.println("Set output file: " + this.outputFileTest);
  }

  public void setInputFeatures(String file) {
    this.inputFeatures = file; // /home/zhuzhu/Documents/data/sentiment/samples.arff
  }

  public List<String> getListSentences() {
    return this.listSentences;
  }

  public void initClassifier() {
    FileReader fileReader = null;
    BufferedReader bReader = null;
    try {
      System.out.println("Loading classifier...");
      classifier = (Classifier) weka.core.SerializationHelper.read(this.inputModel);
      System.out.println("Reading features...");
      ArrayList<Attribute> attributes = new ArrayList<>(0);
      fileReader = new FileReader(inputFeatures);
      bReader = new BufferedReader(fileReader);
      String line = null;
      while ((line = bReader.readLine()) != null) {
        listFeatures.add(line);
        attributes.add(new Attribute(line));
      }
      attributes.add(new Attribute("sentiment_label", listClass));
      dataSet = new Instances("SentimentsRelation", attributes, 0);
      dataSet.setClassIndex(dataSet.numAttributes() - 1);
    } catch (Exception e) {
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
    }
  }

  public void readDocument() {
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
        if ("".equalsIgnoreCase(line.trim())) {
          continue;
        }
        for (String sentence : line.split("\\.")) {
          listSentences.add(sentence);
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
      System.out.println("Number of sentences: " + listSentences.size());
      System.out.println("Read text: done.");
    }
  }

  public void classifyText(String text) {
    try {
      double[] values = new double[dataSet.numAttributes()];
      text = TokenizationUtils.removePunctuation(text.toLowerCase());
      Map<String, Double> map = new HashMap<>(0);
      for (String t : TokenizationUtils.tokenizeTextEN(text)) {
        Double v = map.get(t);
        map.put(t, (v == null) ? 1 : v + 1);
      }
      for (Entry<String, Double> value : map.entrySet()) {
        if (!listFeatures.contains(value.getKey())) {
          continue;
        }
        double tf = value.getValue() / map.size();
        values[listFeatures.indexOf(value.getKey())] = tf;
      }
      values = VectorUtils.normalizeVector(values);
      dataSet.add(new DenseInstance(1.0, values));
      int index = dataSet.numInstances() - 1;
      double value = classifier.classifyInstance(dataSet.instance(index));
      String prediction = dataSet.classAttribute().value((int) value);
      System.out
          .println("The predicted value of instance-" + index + " '" + text + "': " + prediction);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String... strings) {
    Options options = new Options();
    Option input = new Option("i", "input", true, "input file");
    input.setRequired(true);
    options.addOption(input);
    Option inputModel = new Option("im", "inputmodel", true, "input model file");
    inputModel.setRequired(true);
    options.addOption(inputModel);
    Option inputFeature = new Option("if", "inputfeature", true, "input feature file");
    inputFeature.setRequired(true);
    options.addOption(inputFeature);
    Option output = new Option("op", "outputpath", true, "output file path");
    output.setRequired(true);
    options.addOption(output);
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, strings);
      String inputFile = cmd.getOptionValue("input");
      String inputModelFile = cmd.getOptionValue("inputmodel");
      String inputFeatureFile = cmd.getOptionValue("inputfeature");
      String outputFilePath = cmd.getOptionValue("outputpath");
      SentimentsDetector sd = new SentimentsDetector();
      sd.setInput(inputFile);
      sd.setModel(inputModelFile);
      sd.setOutputPath(outputFilePath);
      sd.setInputFeatures(inputFeatureFile);
      sd.initClassifier();
      sd.readDocument();
      List<String> list = sd.getListSentences();

      // Continue output sentences and their predictions one by one.
      int index = 0;
      int n;
      Scanner scanner = new Scanner(System.in);
      while (true) {
        if (index >= list.size()) {
          break;
        }
        String sentence = list.get(index);
        index++;
        sd.classifyText(sentence);
        System.out.println("Type 0 to exit. Any numbers to continue.");
        n = scanner.nextInt();
        if (n != 0) {
          continue;
        } else {
          break;
        }
      }
      if (scanner != null) {
        scanner.close();
      }
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }
  }

}
