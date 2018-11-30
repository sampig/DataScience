/*
 * 
 */
package org.zhuzhu.application.sap.utils;

/**
 * 
 * @author Chenfeng Zhu
 *
 */
public class ClassificationUtils {
  /**
   * Type of Classification.<br/>
   * <ul>
   * <li>Decision Tree</li>
   * <li>Naive Bayes</li>
   * <li>kNN</li>
   * <li>SVM</li>
   * </ul>
   * 
   * @author Chenfeng Zhu
   *
   */
  public enum TypeClassification {
    DT, NB, KNN, SVM;

    public String toString() {
      switch (this) {
        case DT:
          return "Decision Tree: J48";
        case NB:
          return "Naive Bayes";
        case KNN:
          return "kNN";
        case SVM:
          return "SVM";
        default:
          return "Undefined";
      }
    }
  };

}
