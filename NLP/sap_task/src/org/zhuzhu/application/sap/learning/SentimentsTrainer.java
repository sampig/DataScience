/**
 * 
 */
package org.zhuzhu.application.sap.learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.zhuzhu.application.sap.DistributionApplication;
import org.zhuzhu.application.sap.utils.ClassificationUtils.TypeClassification;
import org.zhuzhu.application.sap.utils.TokenizationUtils;
import org.zhuzhu.application.sap.utils.VectorUtils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

/**
 * <ul>
 * <li>TF(term)=f/sum(f)</li>
 * <li>IDF(term)=log(D/d)</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class SentimentsTrainer {

  private String inputTrain = null;
  private String inputTest = null;
  private String outputPath = null;

  private String outputModel = null;
  private TypeClassification tc = TypeClassification.DT;
  private int numFolds = 10;

  private String labelClass = "sentiment_label";
  private List<String> listClass = new ArrayList<>(0);
  private ArrayList<Attribute> attributes = new ArrayList<>(0);
  private Instances dataSet;
  private Instances dataSetTest;

  private List<String[]> listData = new ArrayList<>(0);
  private List<String[]> listTest = new ArrayList<>(0);
  private int sizeDocument = 0;
  private Map<String, Double> mapFeatures = new HashMap<>(0);
  private List<String> listFeatures = new ArrayList<>(0);

  public void setTrain(String input) {
    this.inputTrain = input;
    System.out.println("Set training datafile: " + this.inputTrain);
  }

  public void setTest(String input) {
    this.inputTest = input;
    System.out.println("Set testing datafile: " + this.inputTest);
  }

  public void setOutputPath(String path) {
    this.outputPath = path;
    System.out.println("Set output file path: " + this.outputPath);
    this.outputModel = path + "/sentiments_weka.model";
  }

  public void setClassificationType(TypeClassification tc) {
    this.tc = tc;
    System.out.println("Use classification: " + tc);
  }

  public void readTrainData() {
    FileReader fileReader = null;
    InputStreamReader inStreamReader = null;
    BufferedReader bReader = null;
    try {
      if (inputTrain == null || "".equalsIgnoreCase(inputTrain)) {
        System.out.println("Reading the text from local...");
        inStreamReader = new InputStreamReader(DistributionApplication.class.getClassLoader()
            .getResourceAsStream("/sentiment/train_data.csv"));
        bReader = new BufferedReader(inStreamReader);
      } else {
        System.out.println("Reading the text from '" + inputTrain + "'...");
        fileReader = new FileReader(inputTrain);
        bReader = new BufferedReader(fileReader);
      }
      String line = null;
      line = bReader.readLine();// ignore the header.
      while ((line = bReader.readLine()) != null) {
        // System.out.println(line);
        String emotion = line.substring(0, line.indexOf(","));
        String content = line.substring(line.indexOf(","));
        if (!listClass.contains(emotion)) {
          listClass.add(emotion);
        }
        listData.add(new String[] {emotion, content});
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
      System.out.println("Training data size: " + listData.size());
    }
    this.generateDataset();
    if (inputTest != null && !"".equalsIgnoreCase(inputTest)) {
      try {
        System.out.println("Reading the text from '" + inputTrain + "'...");
        fileReader = new FileReader(inputTest);
        bReader = new BufferedReader(fileReader);
        String line = null;
        line = bReader.readLine();// ignore the header.
        while ((line = bReader.readLine()) != null) {
          // System.out.println(line);
          String id = line.substring(0, line.indexOf(","));
          String content = line.substring(line.indexOf(","));
          listTest.add(new String[] {id, content});
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
        System.out.println("Read test data: done.");
        System.out.println("Testing data size: " + listData.size());
      }
    }
  }

  public void generateDataset() {
    System.out.println("Generating dataset using TF*IDF for single words...");
    System.out.println("Generating features...");
    for (String[] str : listData) {
      if (str.length != 2) {
        System.out.println("Error:" + str);
        continue;
      }
      String content = str[1];
      content = TokenizationUtils.removePunctuation(content.toLowerCase());
      for (String t : TokenizationUtils.tokenizeTextEN(content)) {
        Double countName = mapFeatures.get(t);
        mapFeatures.put(t, (countName == null) ? 1 : countName + 1);
      }
    }
    sizeDocument = listData.size();
    List<String> listRemove = new ArrayList<>(0);
    // TODO: there is no proof to do that, just in order to reduce features by force.
    for (String key : mapFeatures.keySet()) {
      if (mapFeatures.get(key) < 5) {
        listRemove.add(key);
        continue;
      }
    }
    for (String key : listRemove) {
      mapFeatures.remove(key);
    }
    System.out.println("Features size: " + mapFeatures.size());
    System.out.println("Updating features using IDF(=log(D/d))...");
    for (String key : mapFeatures.keySet()) {
      double value = Math.log(1.0 * sizeDocument / mapFeatures.get(key));
      mapFeatures.put(key, value);
      listFeatures.add(key);
      attributes.add(new Attribute(key));
    }
    System.out.println("Categories: " + listClass.size());
    System.out.println("Attributes size: " + attributes.size());
    attributes.add(new Attribute(labelClass, listClass));
    System.out.println("Attributes with label size: " + attributes.size());
    dataSet = new Instances("SentimentsRelation", attributes, 0);
    dataSetTest = new Instances("SentimentsRelation", attributes, 0);
    // System.out.println(dataSet);
    // Add data into dataset:
    for (String[] data : listData) {
      if (data.length != 2) {
        System.out.println("Error:" + data);
        continue;
      }
      double[] values = new double[dataSet.numAttributes()];
      String emotion = data[0];
      String content = data[1];
      content = TokenizationUtils.removePunctuation(content.toLowerCase());
      Map<String, Double> map = new HashMap<>(0);
      for (String t : TokenizationUtils.tokenizeTextEN(content)) {
        // NGramUtils.getNGramsByWord(content, 1)
        // TokenizationUtils.tokenizeTextEN(content)
        Double v = map.get(t);
        map.put(t, (v == null) ? 1 : v + 1);
      }
      for (Entry<String, Double> value : map.entrySet()) {
        if (!listFeatures.contains(value.getKey())) {
          continue;
        }
        double tf = value.getValue() / map.size();
        double idf = mapFeatures.get(value.getKey());
        values[listFeatures.indexOf(value.getKey())] = tf * idf;
      }
      values = VectorUtils.normalizeVector(values);
      values[dataSet.numAttributes() - 1] = listClass.indexOf(emotion);
      dataSet.add(new DenseInstance(1.0, values));
    }
    // Add test data:
    if (listTest.size() > 0) {
      for (String[] data : listTest) {
        double[] values = new double[dataSetTest.numAttributes()];
        String content = data[1];
        content = TokenizationUtils.removePunctuation(content.toLowerCase());
        Map<String, Double> map = new HashMap<>(0);
        for (String t : TokenizationUtils.tokenizeTextEN(content)) {
          Double v = map.get(t);
          map.put(t, (v == null) ? 1 : v + 1);
        }
        for (Entry<String, Double> value : map.entrySet()) {
          if (!listFeatures.contains(value.getKey())) {
            continue;
          }
          double tf = value.getValue() / map.size();
          double idf = mapFeatures.get(value.getKey());
          values[listFeatures.indexOf(value.getKey())] = tf * idf;
        }
        values = VectorUtils.normalizeVector(values);
        dataSetTest.add(new DenseInstance(1.0, values));
      }
    }
  }

  public void generateTestData() {
    System.out.println("Generating Testing dataset...");
  }

  public void buildClassifier() {
    if (dataSet.classIndex() == -1) {
      dataSet.setClassIndex(dataSet.numAttributes() - 1);
    }
    if (tc == TypeClassification.DT) {
      outputModel = this.outputPath + "/sentiments_dt_weka.model";
      try {
        String[] options = Utils.splitOptions("-U");
        J48 tree = new J48();
        tree.setOptions(options);
        tree.buildClassifier(dataSet);
        this.evaluateClassifier(tree);
        // Test the model:
        // Evaluation evaluation = new Evaluation(dataSet);
        // evaluation.evaluateModel(tree, testData);
        // System.out.println(evaluation.toSummaryString("\nResults\n======\n", false));
        // Save the model
        this.saveClassificationModel(tree);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (tc == TypeClassification.NB) {
      // train NaiveBayes
      NaiveBayes nb = new NaiveBayes();
      try {
        nb.buildClassifier(dataSet);
        this.evaluateClassifier(nb);
        this.saveClassificationModel(nb);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (tc == TypeClassification.KNN) {
      // train kNN
      Classifier ibk = new IBk(1);
      try {
        ibk.buildClassifier(dataSet);
        this.evaluateClassifier(ibk);
        this.saveClassificationModel(ibk);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void evaluateClassifier(Classifier classifier) {
    System.out.println("Starting evaluation...");
    try {
      Evaluation evaluation = new Evaluation(dataSet);
      evaluation.crossValidateModel(classifier, dataSet, numFolds, new Random(1));
      System.out.println(evaluation.toSummaryString("\nResults\n======\n", false));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void saveClassificationModel(Classifier classifier) {
    System.out.println("Saving model into '" + outputModel + "'...");
    try {
      weka.core.SerializationHelper.write(outputModel, classifier);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Save model: done.");
  }

  /**
   * Sparse the ARFF file.
   */
  @SuppressWarnings("unused")
  private void sparse() {
    System.out.println("Sparsing ARFF file...");
    String output = "/home/zhuzhu/Documents/data/sentiment/samples.arff";
    NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse();
    try {
      nonSparseToSparseInstance.setInputFormat(dataSet);
      Instances sparseDataset = Filter.useFilter(dataSet, nonSparseToSparseInstance);
      ArffSaver arffSaverInstance = new ArffSaver();
      arffSaverInstance.setInstances(sparseDataset);
      arffSaverInstance.setFile(new File(output));
      arffSaverInstance.writeBatch();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Sparsed file is saved in " + output);
  }

  public static void main(String... strings) {
    Options options = new Options();
    Option iTrain = new Option("tr", "train", true, "input training datafile");
    iTrain.setRequired(true);
    options.addOption(iTrain);
    Option iTest = new Option("te", "test", true, "input testing datafile");
    iTest.setRequired(false);
    options.addOption(iTest);
    Option output = new Option("op", "outputpath", true, "output file path");
    output.setRequired(true);
    options.addOption(output);
    Option classification =
        new Option("ct", "classificationtype", true, "Classification: DT|NB|KNN");
    CommandLineParser parser = new DefaultParser();
    classification.setRequired(false);
    options.addOption(classification);
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, strings);
      String inTrainFile = cmd.getOptionValue("train");
      String inTestFile = cmd.getOptionValue("test");
      String outputFilePath = cmd.getOptionValue("outputpath");
      String classificationType = cmd.getOptionValue("classificationtype");
      SentimentsTrainer st = new SentimentsTrainer();
      st.setTrain(inTrainFile);
      st.setTest(inTestFile);
      st.setOutputPath(outputFilePath);
      if ("DT".equalsIgnoreCase(classificationType)) {
        st.setClassificationType(TypeClassification.DT);
      } else if ("NB".equalsIgnoreCase(classificationType)) {
        st.setClassificationType(TypeClassification.NB);
      } else if ("KNN".equalsIgnoreCase(classificationType)) {
        st.setClassificationType(TypeClassification.KNN);
      }
      st.readTrainData();
      st.buildClassifier();
      // st.sparse();
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }
  }

}
