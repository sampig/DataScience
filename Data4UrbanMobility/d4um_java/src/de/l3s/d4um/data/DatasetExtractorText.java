/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.VectorUtils;

/**
 * Extract a new dataset only including name and description according to the list of IDs.<br/>
 * Input:
 * <ul>
 * <li>the file for id list.</li>
 * <li>the original dataset.</li>
 * </ul>
 * <br/>
 * Output:
 * <ul>
 * <li>a new dataset in n-quad format.</li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class DatasetExtractorText {

  private String filepath = ConfigUtils.getDataDirectory();

  private String inputFilename = filepath + "/eventsIdsNewYorkPositive.tsv";
  // inputPositive
  private String datasetFilename = filepath + "/eventsQuadsNewYorkPositive.nq";
  private String outputFilename = filepath + "/eventTextPositive.nq";

  private int size = 0;

  private List<String> listEventIds = new ArrayList<>(0);
  private List<String> listEventExtract = new ArrayList<>(0);

  public DatasetExtractorText() {}

  public List<String> getListEventIds() {
    return this.listEventIds;
  }

  public void setInput(String input) {
    this.inputFilename = input;
    System.out.println("Set file for id: " + this.inputFilename);
  }

  public void setDataset(String dataset) {
    this.datasetFilename = dataset;
    System.out.println("Set dataset: " + this.datasetFilename);
  }

  public void setOutput(String output) {
    this.outputFilename = output;
    System.out.println("Set output: " + this.outputFilename);
  }

  public void setSize(int size) {
    this.size = size;
    System.out.println("Set the size of new dataset: " + this.size);
  }

  /**
   * Read the IDs.
   */
  public void readEventIds() {
    System.out.println("Reading the data from '" + inputFilename + "'...");
    FileReader fileReader = null;
    BufferedReader bReader = null;
    try {
      fileReader = new FileReader(inputFilename);
      bReader = new BufferedReader(fileReader);
      String line = null;
      while ((line = bReader.readLine()) != null) {
        if (!line.contains("\t")) {
          System.out.println(line);
        }
        String[] str = line.split("\t");
        if (str.length >= 3) {
          System.out.println(line);
          continue;
        }
        String url = str[0];
        String id = str[1];
        String newId = id.contains(url) ? id : id + "_" + url;
        listEventIds.add(newId);
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
    }
    System.out.println("Number of events: " + listEventIds.size());
    if (size > 0) {
      List<Integer> listIds = VectorUtils.getRandomNumbers(size, listEventIds.size());
      for (int i : listIds) {
        listEventExtract.add(listEventIds.get(i));
      }
    } else {
      listEventExtract.addAll(listEventIds);
    }
    System.out.println("Extract number of events: " + listEventExtract.size());
  }

  /**
   * Save the data into a new file in n-quad format.
   */
  public void extractData() {
    if (listEventIds == null || listEventIds.size() <= 0) {
      this.readEventIds();
    }
    FileInputStream is;
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      is = new FileInputStream(this.datasetFilename);
      fWriter = new FileWriter(this.outputFilename);
      bWriter = new BufferedWriter(fWriter);
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + datasetFilename + "...");
      System.out.println("And writing data into '" + outputFilename + "'...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String url = nx[3].getLabel();
        if (!predicate.endsWith("/name") && !predicate.endsWith("/description")) {
          continue;
        }
        String id = nodeid.contains(url) ? nodeid : nodeid + "_" + url;
        if (!listEventExtract.contains(id)) {
          continue;
        }
        // save the events.
        bWriter.write(nx[0] + " " + nx[1] + " " + nx[2] + " " + nx[3] + "   .\n");
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
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
    DatasetExtractorText det = new DatasetExtractorText();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [dataset] [output] [size]";
        System.out.println("java " + det.getClass().getName() + parameters);
        return;
      }
      det.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      det.setDataset(strings[1]);
    }
    if (strings.length >= 3) {
      det.setOutput(strings[2]);
    }
    if (strings.length >= 4) {
      int size = 0;
      try {
        size = Integer.parseInt(strings[3]);
      } catch (Exception e) {
        e.printStackTrace();
      }
      det.setSize(size);
    }
    det.readEventIds();
    det.extractData();
  }

}
