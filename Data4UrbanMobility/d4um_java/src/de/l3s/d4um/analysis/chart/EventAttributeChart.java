/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.chart;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.FileUtils;
import de.l3s.d4um.utils.PredicateUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * Display the event attributes with bar chart.
 * 
 * @author Chenfeng Zhu
 */
public class EventAttributeChart extends Application {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private static String inputFilename = DEFAULT_PATH + "/fm_event_plds.out";
  private static String outputImageFile = DEFAULT_PATH + "/fm_event_plds.png";

  private Map<String, Integer> mapProperties = new HashMap<>(0);
  private int threshold = 1000;

  private static String title = "Event PLDs";
  private String xLabel = "Quantity";
  private String yLabel = "Property";

  private int barGap = 5;
  private int sizeWidth = 800;
  private int sizeHeight = 600;

  @Override
  public void start(Stage stage) throws Exception {
    // read data
    if (mapProperties == null || mapProperties.size() <= 0) {
      readEventProperties();
    }
    // windows and layout
    stage.setTitle(title + " Bar Chart");
    final NumberAxis xAxis = new NumberAxis();
    final CategoryAxis yAxis = new CategoryAxis();
    final BarChart<Number, String> bc = new BarChart<Number, String>(xAxis, yAxis);
    bc.setCategoryGap(0);
    bc.setBarGap(barGap);
    bc.setAnimated(false);
    bc.setTitle(title);
    xAxis.setLabel(xLabel);
    xAxis.setTickLabelRotation(90);
    yAxis.setLabel(yLabel);
    // data series
    XYChart.Series<Number, String> series = new XYChart.Series<>();
    series.setName(yLabel);
    for (String key : mapProperties.keySet()) {
      if (mapProperties.get(key) < threshold) {
        continue;
      }
      series.getData().add(new XYChart.Data<Number, String>(mapProperties.get(key), key));
    }
    Scene scene = new Scene(bc, sizeWidth, sizeHeight);
    bc.getData().add(series);
    stage.setScene(scene);
    stage.show();
    // save as image.
    FileUtils.saveBarChartAsPng(outputImageFile, bc);
  }

  /**
   * Read the summary of event properties.
   */
  private void readEventProperties() {
    System.out.println("Reading the data from '" + inputFilename + "'...");
    FileReader fileReader = null;
    BufferedReader bReader = null;
    Map<String, Integer> map = new HashMap<>(0);
    try {
      fileReader = new FileReader(inputFilename);
      bReader = new BufferedReader(fileReader);
      String line = null;
      while ((line = bReader.readLine()) != null) {
        String[] str = line.split(",");
        map.put(str[0], Integer.parseInt(str[1]));
      }
      mapProperties = PredicateUtils.sortByValue(map, PredicateUtils.SortBy.ASC);
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
  }

  public static void main(String... strings) {
    if (strings.length >= 1) {
      if ("-h".equalsIgnoreCase(strings[0]) || "-help".equalsIgnoreCase(strings[0])) {
        String parameters = " [inputfile] [outputImage]";
        System.out.println("java " + EventAttributeChart.class.getName() + parameters);
        return;
      }
      inputFilename = strings[0];
    }
    if (strings.length >= 2) {
      outputImageFile = strings[1];
    }
    if (strings.length >= 3) {
      title = strings[2];
    }
    launch(strings);
  }

}
