/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.image.WritableImage;

/**
 * A utility for dealing with files.
 * 
 * @author Chenfeng Zhu
 *
 */
public class FileUtils {

  /**
   * Save the data in the map into the output file.
   * 
   * @param outputfile
   * @param map
   */
  public static void writeToFile(String outputfile, Map<String, String> map) {
    if (outputfile == null) {
      return;
    }
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(outputfile);
      bWriter = new BufferedWriter(fWriter);
      for (String key : map.keySet()) {
        bWriter.write(key + ", " + map.get(key));
        bWriter.write("\n");
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

  /**
   * Save the data in the map into the output file.
   * 
   * @param outputfile
   * @param map
   */
  public static void writeMapdataToFile(String outputfile, Map<String, Integer> map) {
    if (outputfile == null) {
      return;
    }
    System.out.println("Start writing data(" + map.size() + ") into " + outputfile + "...");
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(outputfile);
      bWriter = new BufferedWriter(fWriter);
      for (String key : map.keySet()) {
        bWriter.write(key + "," + map.get(key).intValue());
        bWriter.write("\n");
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

  /**
   * Save the bar chart into a PNG file.
   * 
   * @param output
   * @param barChart
   */
  @FXML
  public static void saveBarChartAsPng(String output, BarChart<?, ?> barChart) {
    System.out.println("Save the bar chart into '" + output + "'...");
    WritableImage image = barChart.snapshot(new SnapshotParameters(), null);
    File file = new File(output);
    try {
      ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
