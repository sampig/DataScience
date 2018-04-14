/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A utility for configuration properties.
 * 
 * @author Chenfeng Zhu
 *
 */
public class ConfigUtils {

  public abstract class ConfigConstant {

    public static final String DATA_DIRECTORY = "data.directory";
    public static final String DATA_EVENT_FILE = "data.eventfile";
    public static final String DATA_EVENT_FILE_POS = "data.eventfileP";
    public static final String DATA_EVENT_FILE_NEG = "data.eventfileN";
    public static final String DATA_EVENT_FILE_NAMEDESC = "data.eventnamedescfile";
    public static final String DATA_EVENT_FILE_NAMEDESC_P = "data.eventnamedescfileP";
    public static final String DATA_EVENT_FILE_NAMEDESC_N = "data.eventnamedescfileN";
    public static final String DATA_EVENT_FILE_NAMEDESC_DE = "data.eventnamedescdefile";

    public static final String TAGME_URL = "tagme.url";
    public static final String TAGME_LANG = "tagme.lang";
    public static final String TAGME_TOKEN = "tagme.token";

  }

  private static final String CONFIG_FILE = "config.properties";

  private static Properties prop = new Properties();
  private static InputStream inputStream =
      ConfigUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE);

  /**
   * Get the value.
   * 
   * @param key
   * @return
   */
  public static String getValue(String key) {
    String value = null;
    try {
      if (inputStream != null) {
        prop.load(inputStream);
      } else {
        throw new FileNotFoundException(
            "property file '" + CONFIG_FILE + "' not found in the classpath");
      }
      value = prop.getProperty(key);
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
    return value;
  }

  /**
   * Get the directory where data is located.
   * 
   * @return the directory
   */
  public static String getDataDirectory() {
    String directory = null;
    try {
      if (inputStream != null) {
        prop.load(inputStream);
      } else {
        throw new FileNotFoundException(
            "property file '" + CONFIG_FILE + "' not found in the classpath");
      }
      directory = prop.getProperty(ConfigConstant.DATA_DIRECTORY);
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
    return directory;
  }

  /**
   * Get the file name of event data.
   * 
   * @return the file name
   */
  public static String getEventDataFilename() {
    String filename = null;
    try {
      if (inputStream != null) {
        prop.load(inputStream);
      } else {
        throw new FileNotFoundException(
            "property file '" + CONFIG_FILE + "' not found in the classpath");
      }
      filename = prop.getProperty(ConfigConstant.DATA_EVENT_FILE);
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
    return filename;
  }

  /**
   * Get the URL for TagME web services. (e.g.
   * https://tagme.d4science.org/tagme/tag?lang=LANG&gcube-token=YOUR_TOKEN&text=)
   * 
   * @return the URL
   */
  public static String getTagME() {
    String url = "";
    try {
      if (inputStream != null) {
        prop.load(inputStream);
      } else {
        throw new FileNotFoundException(
            "property file '" + CONFIG_FILE + "' not found in the classpath");
      }
      url = prop.getProperty(ConfigConstant.TAGME_URL) + "?lang="
          + prop.getProperty(ConfigConstant.TAGME_LANG) + "&gcube-token="
          + prop.getProperty(ConfigConstant.TAGME_TOKEN) + "&text=";
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
    return url;
  }

}
