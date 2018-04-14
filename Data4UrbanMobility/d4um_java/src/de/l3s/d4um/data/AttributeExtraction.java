/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.data;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import de.l3s.d4um.utils.ConfigUtils;

/**
 * Select the features and extract them into a new file.
 * 
 * @author Chenfeng Zhu
 *
 */
@Deprecated
public class AttributeExtraction {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();
  
  private String inputfile = DEFAULT_PATH + "/" + ConfigUtils.getEventDataFilename(); // "/eventRelated.nq";

  private String[] attributesName = {"name", "description"}; //

  public void extractTextAttributes() {
    String[] outputs = new String[attributesName.length];
    FileWriter[] fWriters = new FileWriter[attributesName.length];
    BufferedWriter[] bWriters = new BufferedWriter[attributesName.length];
    FileInputStream is;
    try {
      is = new FileInputStream(inputfile);

      for (int i = 0; i < outputs.length; i++) {
        outputs[i] = DEFAULT_PATH + "/event_" + attributesName[i] + "_text.out";
        fWriters[i] = new FileWriter(outputs[i]);
        bWriters[i] = new BufferedWriter(fWriters[i]);
      }

      NxParser nxp = new NxParser();
      nxp.parse(is);

      for (Node[] nx : nxp) {
        for (int i = 0; i < attributesName.length; i++) {
          String f = attributesName[i];
          if (nx[1] != null && nx[1].getLabel().endsWith(f)
              && !"null".equalsIgnoreCase(nx[2].getLabel())) {
            bWriters[i].write(nx[2].getLabel() + "\n");
          }
        }
        // if (nx.length > 4) {
        // System.out.println(nx.length);
        // }
        // System.out.println(nx[0]);
        // System.out.println(nx[1]);
        // System.out.println(nx[2]);
        // System.out.println(nx[3]);
        // for (Node n : nx) {
        // System.out.println(n.getLabel() + ": " + n);
        // }
        // break;
      }
      // System.out.println(nxp.lineNumber());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        for (int i = 0; i < outputs.length; i++) {
          if (bWriters[i] != null) {
            bWriters[i].close();
          }
          if (fWriters[i] != null) {
            fWriters[i].close();
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void extractURLAttributes() {
    ;
  }

  public static void main(String... strings) {
    AttributeExtraction fe = new AttributeExtraction();
    fe.extractTextAttributes();
    fe.extractURLAttributes();
  }

}
