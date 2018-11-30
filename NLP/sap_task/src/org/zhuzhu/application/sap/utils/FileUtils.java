/*
 * 
 */
package org.zhuzhu.application.sap.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;

/**
 * A utility for dealing with files.
 * 
 * @author Chenfeng Zhu
 *
 */
public class FileUtils {

  /**
   * Save the text into the output file.
   * 
   * @param outputfile
   * @param text
   */
  public static void writeToFile(String outputfile, String text) {
    if (outputfile == null) {
      return;
    }
    System.out.println("Start writing text into " + outputfile + "...");
    FileWriter fWriter = null;
    BufferedWriter bWriter = null;
    try {
      fWriter = new FileWriter(outputfile);
      bWriter = new BufferedWriter(fWriter);
      bWriter.write(text + "\n");
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
      System.out.println("Writing text: done.");
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
      System.out.println("Writing data: done.");
    }
  }

  /**
   * Save the data as a word cloud image.
   * 
   * @param outputfile
   * @param map
   */
  public static void writeDataIntoWordCloud(String outputfile, Map<String, Integer> map) {
    System.out.println("Start saving the image into " + outputfile + "...");
    List<WordFrequency> wordFrequencies = new ArrayList<>(0);
    for (Entry<String, Integer> entry : map.entrySet()) {
      WordFrequency wf = new WordFrequency(entry.getKey(), entry.getValue());
      wordFrequencies.add(wf);
    }
    final Dimension dimension = new Dimension(600, 600);
    final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
    wordCloud.setPadding(2);
    wordCloud.setBackground(new CircleBackground(300));
    wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1),
        new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
    wordCloud.setFontScalar(new LinearFontScalar(10, 40));
    wordCloud.build(wordFrequencies);
    wordCloud.writeToFile(outputfile + ".png");
    System.out.println("Save image: done.");
  }

}
