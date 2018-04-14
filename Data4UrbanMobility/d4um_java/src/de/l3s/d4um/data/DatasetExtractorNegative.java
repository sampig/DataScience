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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.NGramUtils;
import de.l3s.d4um.utils.VectorUtils;

/**
 * Extract a new negative balanced dataset according to the list of IDs.<br/>
 * Input:
 * <ul>
 * <li>the file for id list.</li>
 * <li>the original dataset.</li>
 * <li>dataset size.</li>
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
public class DatasetExtractorNegative {

  private String filepath = ConfigUtils.getDataDirectory();

  // private String inputIdPositive = filepath + "/eventsIdsNewYorkPositive.tsv";
  private String inputIdNegative = filepath + "/eventsIdsNewYorkNegative.tsv";

  private String datasetFilename = filepath + "/eventNegative.nq";
  private String outputFilename = filepath + "/eventNegativeNew.nq";

  private int size = 0;

  private List<String> listEventIds = new ArrayList<>(0);
  private List<String> listEventDataIds = new ArrayList<>(0);
  private Map<String, String> mapEventDataIds = new HashMap<String, String>(0);
  private List<String> listEventExtract = new ArrayList<>(0);

  public DatasetExtractorNegative() {}

  public List<String> getListEventIds() {
    return this.listEventIds;
  }

  // public void setIdPositive(String positive) {
  // this.inputIdPositive = positive;
  // System.out.println("Set file for positive id: " + this.inputIdPositive);
  // }

  public void setIdNegative(String negative) {
    this.inputIdNegative = negative;
    System.out.println("Set file for negative id: " + this.inputIdNegative);

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
   * Read the negative IDs.
   */
  public void readEventIds() {
    System.out.println("Reading the data from '" + inputIdNegative + "'...");
    FileReader fileReader = null;
    BufferedReader bReader = null;
    try {
      fileReader = new FileReader(inputIdNegative);
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
        String newId = id.contains("http") ? id
            : id + NGramUtils.removePunctuaion(url).replaceAll(" ", "") + "_" + url + "";
        listEventIds.add(newId);
        mapEventDataIds.put(newId, "");
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
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String url = nx[3].getLabel();
        String id = nodeid.contains(url) ? nodeid : nodeid + "_<" + url + ">";
        if (!listEventIds.contains(id)) {
          continue;
        }
        if (predicate.endsWith("/name")) {
          String value = mapEventDataIds.get(id) + "name";
          mapEventDataIds.put(id, value);
        } else if (predicate.endsWith("/description")) {
          String value = mapEventDataIds.get(id) + "description";
          mapEventDataIds.put(id, value);
        }
      }
      for (Map.Entry<String, String> entry : mapEventDataIds.entrySet()) {
        String value = entry.getValue();
        if (value != null && value.contains("name") && value.contains("description")) {
          // if (value != null && (value.equals("namedescription") ||
          // value.equals("descriptionname"))) {
          String id = entry.getKey();
          if (!listEventDataIds.contains(id)) {
            listEventDataIds.add(id);
          }
        }
      }
      System.out.println("Number of events with name and description: " + listEventDataIds.size());
      if (size > 0) {
        List<Integer> listIds = VectorUtils.getRandomNumbers(size, listEventDataIds.size());
        for (int i : listIds) {
          listEventExtract.add(listEventDataIds.get(i));
        }
      } else {
        listEventExtract.addAll(listEventDataIds);
      }
      System.out.println("Extract number of events: " + listEventExtract.size());
      System.out.println("And writing data into '" + outputFilename + "'...");
      is = new FileInputStream(this.datasetFilename);
      nxp.parse(is);
      for (Node[] nx : nxp) {
        String nodeid = nx[0].toString();
        String url = nx[3].getLabel();
        String id = nodeid.contains(url) ? nodeid : nodeid + "_<" + url + ">";
        if (!listEventExtract.contains(id)) {
          continue;
        }
        // save the events.
        bWriter.write(id + " " + nx[1] + " " + nx[2] + " " + nx[3] + " .\n");
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
    DatasetExtractorNegative den = new DatasetExtractorNegative();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [dataset] [outputfile] [size]";
        System.out.println("java " + den.getClass().getName() + parameters);
        return;
      }
      den.setIdNegative(strings[0]);
    }
    if (strings.length >= 2) {
      den.setDataset(strings[1]);
    }
    if (strings.length >= 3) {
      den.setOutput(strings[2]);
    }
    if (strings.length >= 4) {
      int size = 0;
      try {
        size = Integer.parseInt(strings[3]);
      } catch (Exception e) {
        e.printStackTrace();
      }
      den.setSize(size);
    }
    den.readEventIds();
    den.extractData();
  }

}
