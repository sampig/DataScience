/*
 * 
 */
package org.zhuzhu.application.sap.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility for N-Gram.
 * 
 * @author Chenfeng Zhu
 *
 */
public class NGramUtils {

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
    int length = text.length();
    if (length > n) {
      for (int i = 0; i < length - n + 1; i++) {
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
    } else if (length == n) {
      nGrams.add(text);
    }
    return nGrams;
  }

  /**
   * Get Skip-Grams by word.
   * 
   * @param text
   * @param n
   * @return
   */
  public static List<String> getSkipGramsByWord(String text, int n) {
    List<String> nGrams = new ArrayList<String>();
    if (text == null) {
      return nGrams;
    }
    String[] arrayWords = text.split(" ");
    int length = arrayWords.length;
    if (length > n + 2) {
      for (int i = 0; i < length - n - 1; i++) {
        nGrams.add(arrayWords[i] + "-" + arrayWords[i + n]);
      }
    } else if (length == n + 2) {
      nGrams.add(arrayWords[0] + "-" + arrayWords[length - 1]);
    }
    return nGrams;
  }

}
