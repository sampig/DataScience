/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility for N-Gram.
 * 
 * @author Chenfeng Zhu
 *
 */
public class NGramUtils {

  private List<String> nGrams = new ArrayList<String>();

  public void generateNGrams(String str, int n) {
    if (str.length() == n) {
      int counter = 0;
      while (counter < n) {
        nGrams.add(str.substring(counter));
        counter++;
      }
      return;
    }
    int counter = 0;
    String gram = "";
    while (counter < n) {
      gram += str.charAt(counter);
      counter++;
    }
    nGrams.add(gram);
    generateNGrams(str.substring(1), n);
  }

  public void printNGrams() {
    System.out.print(nGrams.size() + ": ");
    for (String str : nGrams) {
      System.out.print(str + ", ");
    }
    System.out.println();
  }

  public List<String> getKmers(String seq, int k) {
    nGrams = new ArrayList<String>();
    int seqLength = seq.length();
    if (seqLength > k) {
      for (int i = 0; i < seqLength - k + 1; i++) {
        nGrams.add(seq.substring(i, k + i));
        // System.out.println(seq.substring(i, k + i));
      }
    } else {
      nGrams.add(seq);
      // System.out.println(seq);
    }
    return nGrams;
  }

  public List<String> getKmersByWord(String text, int n) {
    nGrams = new ArrayList<String>();
    String[] arrayWords = text.split(" ");
    int length = arrayWords.length;
    if (length > n) {
      for (int i = 0; i < length - n + 1; i++) {
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < n; j++) {
          sb.append(arrayWords[i + j] + " ");
        }
        nGrams.add(sb.substring(0, sb.length() - 1));
      }
    } else {
      nGrams.add(text);
    }
    return nGrams;
  }

  public List<String> getNGrams() {
    return nGrams;
  }

  /**
   * <ul>
   * <li>Remove all punctuation</li>
   * <li>Remove all tab</li>
   * <li>keep only one space</li>
   * </ul>
   * 
   * @param str
   * @return
   */
  public static String removePunctuaion(String str) {
    if (str == null) {
      return "";
    }
    str = str.replaceAll("\\t", " ");
    str = str.replaceAll("\\n", " ");
    str = str.replaceAll("\\p{P}", " ");
    str = str.replaceAll("\\p{Punct}", " ");
    str = str.replaceAll("[−|+©¬®●¥€=¾́⊕ ♥>>®´×►]", " ");
    str = str.replaceAll("\t", " ");
    while (str.contains("  ")) {
      str = str.replaceAll("  ", " ");
    }
    return str;
  }

  /**
   * Remove tab symbol.
   * 
   * @param str
   * @return
   */
  public static String removeTab(String str) {
    if (str == null) {
      return "";
    }
    str = str.replaceAll("\t", " ");
    while (str.contains("  ")) {
      str = str.replaceAll("  ", " ");
    }
    return str;
  }

  /**
   * Get N-Grams by character.
   * 
   * @param text
   * @param n
   * @return
   */
  public static List<String> getNGrams(String text, int n) {
    List<String> nGrams = new ArrayList<String>();
    if (text == null) {
      return nGrams;
    }
    int seqLength = text.length();
    if (seqLength > n) {
      for (int i = 0; i < seqLength - n + 1; i++) {
        nGrams.add(text.substring(i, n + i));
      }
    } else {
      nGrams.add(text);
    }
    return nGrams;
  }

  /**
   * Get N-Grams by word.
   * 
   * @param text
   * @param n
   * @return
   */
  public static List<String> getNGramsByWord(String text, int n) {
    List<String> nGrams = new ArrayList<String>();
    if (text == null) {
      return nGrams;
    }
    String[] arrayWords = text.split(" ");
    int length = arrayWords.length;
    if (length > n) {
      for (int i = 0; i < length - n + 1; i++) {
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < n; j++) {
          sb.append(arrayWords[i + j] + " ");
        }
        nGrams.add(sb.substring(0, sb.length() - 1));
      }
    } else {
      nGrams.add(text);
    }
    return nGrams;
  }

  public static void main(String[] args) {
    String title = "Voices After Eight";
    String desc =
        "Jazz / Pop / a cappella, Kirchplatz mit Joachim Schuhmacher, Thomas Aufermann und Axel Zwingenberger";
    int k = 3;
    NGramUtils ng = new NGramUtils();
    // ng.generateNGrams(text, k);
    ng.getKmers(title, k);
    ng.printNGrams();
    ng.getKmers(desc, k);
    ng.printNGrams();
    desc = removePunctuaion(desc);
    System.out.println(desc);
    ng.getKmers(desc, k);
    ng.printNGrams();
    desc =
        "Messe für Comics, Manga, Anime, Film, Cosplay, Games Installation, Zeichnung von Caroline Bayer & Silke Schatz";
    ng.getKmers(desc, k);
    ng.printNGrams();
    desc = removePunctuaion(desc);
    System.out.println(desc);
    ng.getKmers(desc, k);
    ng.printNGrams();
    desc =
        "Messe für Comics, Manga, Anime, Film, Cosplay, Games Installation, Zeichnung von Caroline Bayer & Silke Schatz";
    ng.getKmersByWord(desc, k);
    ng.printNGrams();
    desc = removePunctuaion(desc);
    System.out.println(desc);
    ng.getKmersByWord(desc, k);
    ng.printNGrams();
  }

}
