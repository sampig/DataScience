/*
 * 
 */
package org.zhuzhu.application.sap;

/**
 * 
 * @author Chenfeng Zhu
 *
 */
public class ZhuAnalyzer {

  public static void main(String... strings) {
    String[] str = "a    b".split(" ");
    System.out.println(str.length);
    for (String s : str) {
      if (s == null || "".equalsIgnoreCase(s)) {
        continue;
      }
      System.out.println(s);
    }
  }

}
