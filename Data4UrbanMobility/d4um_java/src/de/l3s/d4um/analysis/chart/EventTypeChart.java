/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.analysis.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import de.l3s.d4um.utils.ConfigUtils;
import de.l3s.d4um.utils.PredicateUtils;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

/**
 * Display the event types with bar chart.
 * 
 * @author Chenfeng Zhu
 */
public class EventTypeChart extends Application {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private String typesFilename = DEFAULT_PATH + "/ede_event_types.out";
  private String outputImageFile = DEFAULT_PATH + "/ede_event_types.png";

  private Map<String, Integer> mapTypes = new HashMap<>(0);
  private int threshold = 1;

  private String title = "Event Types";
  private String xLabel = "Quantity";
  private String yLabel = "Type";

  private int barGap = 5;
  private int sizeWidth = 800;
  private int sizeHeight = 600;

  @Override
  public void start(Stage stage) throws Exception {
    // read data
    if (mapTypes == null || mapTypes.size() <= 0) {
      readEventTypes();
    }
    // windows and layout
    stage.setTitle("Event Type Bar Chart");
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
    for (String key : mapTypes.keySet()) {
      if (mapTypes.get(key) < threshold) {
        continue;
      }
      series.getData().add(new XYChart.Data<Number, String>(mapTypes.get(key), key));
    }
    Scene scene = new Scene(bc, sizeWidth, sizeHeight);
    bc.getData().add(series);
    stage.setScene(scene);
    stage.show();
    // save as image.
    this.saveAsPng(bc);
  }

  /**
   * Read the summary of event types.
   */
  private void readEventTypes() {
    System.out.println("Reading the data from '" + typesFilename + "'...");
    FileReader fileReader = null;
    BufferedReader bReader = null;
    Map<String, Integer> map = new HashMap<>(0);
    try {
      fileReader = new FileReader(typesFilename);
      bReader = new BufferedReader(fileReader);
      String line = null;
      while ((line = bReader.readLine()) != null) {
        String[] str = line.split(",");
        map.put(str[0], Integer.parseInt(str[1]));
      }
      mapTypes = PredicateUtils.sortByValue(map, PredicateUtils.SortBy.ASC);
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

  /**
   * Save the bar chart into a PNG file.
   * 
   * @param barChart
   */
  @FXML
  private void saveAsPng(BarChart<Number, String> barChart) {
    System.out.println("Save the chart into '" + outputImageFile + "'...");
    WritableImage image = barChart.snapshot(new SnapshotParameters(), null);
    File file = new File(outputImageFile);
    try {
      ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    } catch (IOException e) {
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

}
