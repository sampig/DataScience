/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.explorer;

import de.l3s.d4um.utils.ConfigUtils;

/**
 * 
 * @author Chenfeng Zhu
 *
 */
public abstract class BaseExplorer {

  private String filePath = ConfigUtils.getDataDirectory();

  private String inputFile = filePath + "/";
  private String outputFile = filePath + "/";

  public void setPath(String path) {
    if (path != null && path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    this.filePath = path;
    System.out.println("Set path: " + this.filePath);
  }

  public void setInput(String input) {
    this.inputFile = input;
    System.out.println("Set input: " + this.inputFile);
  }

  public void setOutput(String output) {
    this.outputFile = output;
    System.out.println("Set output: " + this.outputFile);
  }

  protected String getPath() {
    return this.filePath;
  }

  protected String getInput() {
    return this.inputFile;
  }

  protected String getOutput() {
    return this.outputFile;
  }

}
