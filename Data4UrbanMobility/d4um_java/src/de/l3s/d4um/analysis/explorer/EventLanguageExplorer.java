/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.explorer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.FileUtils;
import de.l3s.d4um.utils.PredicateUtils;

/**
 * Display the language-related information.
 * <ul>
 * <li></li>
 * </ul>
 * <br/>
 * Input:
 * <ul>
 * <li>Event sub-dataset file. (N-Quads format)</li>
 * </ul>
 * <br/>
 * Output:
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventLanguageExplorer extends BaseExplorer {

  private String outputPrefix = "ele_P_";

  private String outputLang = "lang";
  private String outputLangName = "lang_name";
  private String outputLangDesc = "lang_desc";
  private String outputLangLoca = "lang_loca";

  private Map<String, Integer> mapLangs = new HashMap<>(0);
  private Map<String, Integer> mapLangsName = new HashMap<>(0);
  private Map<String, Integer> mapLangsDesc = new HashMap<>(0);
  private Map<String, Integer> mapLangsLoca = new HashMap<>(0);

  public EventLanguageExplorer() {
    super.setInput(super.getPath() + "/eventPositive.nq");
    super.setOutput(super.getPath() + "/eue.out");
  }

  public String getOutput(String name) {
    return super.getPath() + "/" + this.outputPrefix + name + ".out";
  }

  /**
   * Set the prefix for the output files.
   * 
   * @param prefix
   */
  public void setOutputPrefix(String prefix) {
    this.outputPrefix = prefix;
    System.out.println("Set output prefix: " + this.outputPrefix);
  }

  /**
   * Parse the event data file.
   * 
   * @param inputfile
   */
  public void parseEventFile() {
    FileInputStream is;
    try {
      is = new FileInputStream(super.getInput());
      NxParser nxp = new NxParser();
      System.out.println("Parsing " + super.getInput() + "...");
      nxp.parse(is);
      for (Node[] nx : nxp) {
        // String nodeid = nx[0].toString();
        String predicate = nx[1].getLabel();
        String obj = nx[2].getLabel();
        if (obj.contains("\"@") || obj.contains("\\\"@")) {
          String lang = nx[2].toString().substring(nx[2].toString().lastIndexOf("@"));
          Integer countLang = mapLangs.get(lang);
          mapLangs.put(lang, (countLang == null ? 1 : countLang + 1));
          // from name:
          if (predicate.toLowerCase().contains("name")) {
            Integer countLangName = mapLangsName.get(lang);
            mapLangsName.put(lang, (countLangName == null ? 1 : countLangName + 1));
          }
          // from description:
          if (predicate.toLowerCase().contains("description")) {
            Integer countLangDesc = mapLangsDesc.get(lang);
            mapLangsDesc.put(lang, (countLangDesc == null ? 1 : countLangDesc + 1));
          }
          // from location:
          if (predicate.toLowerCase().contains("location")) {
            Integer countLangLoca = mapLangsLoca.get(lang);
            mapLangsLoca.put(lang, (countLangLoca == null ? 1 : countLangLoca + 1));
          }
        }
      }
      System.out.println("The total number of lines: " + nxp.lineNumber());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Print some basic summary information.
   */
  public void summary() {
    System.out.println("Languages: " + mapLangs.size());
    System.out.println("Languages in name: " + mapLangsName.size());
    System.out.println("Languages in description: " + mapLangsDesc.size());
    System.out.println("Languages in location: " + mapLangsLoca.size());
  }

  /**
   * Save the data into files.
   */
  public void saveData() {
    FileUtils.writeMapdataToFile(this.getOutput(outputLang),
        PredicateUtils.sortByValue(mapLangs, PredicateUtils.SortBy.DESC));
    FileUtils.writeMapdataToFile(this.getOutput(outputLangName),
        PredicateUtils.sortByValue(mapLangsName, PredicateUtils.SortBy.DESC));
    FileUtils.writeMapdataToFile(this.getOutput(outputLangDesc),
        PredicateUtils.sortByValue(mapLangsDesc, PredicateUtils.SortBy.DESC));
    FileUtils.writeMapdataToFile(this.getOutput(outputLangLoca),
        PredicateUtils.sortByValue(mapLangsLoca, PredicateUtils.SortBy.DESC));
  }

  public static void main(String... strings) {
    EventLanguageExplorer ele = new EventLanguageExplorer();
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputpath] [outputprefix]";
        System.out.println("java " + EventLanguageExplorer.class.getName() + parameters);
        return;
      }
      ele.setInput(strings[0]);
    }
    if (strings.length >= 2) {
      ele.setPath(strings[1]);
    }
    if (strings.length >= 3) {
      ele.setOutputPrefix(strings[2]);
    }
    ele.parseEventFile();
    ele.summary();
    ele.saveData();
  }

}
