/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.utils;

/**
 * A utility for dealing with URL.
 * 
 * @author Chenfeng Zhu
 *
 */
public class URLUtils {

  /**
   * Get the URL from the node id(example: "_:noded73ad66345849b4a38e5fc85e02d45_
   * <http://www.coolibri.de/veranstaltungen/essen/ausstellungen/18.05.12.html>").
   * 
   * @param nodeid the node ID
   * @return the URL in the nodeid
   */
  public static String getURL(String nodeid) {
    String url = null;
    url = nodeid.substring(nodeid.indexOf("<") + 1, nodeid.indexOf(">"));
    return url;
  }

  /**
   * Get the top-level domain of the URL.
   * 
   * @param url URL
   * @return top-level domain
   */
  public static String getTLD(String url) {
    String tld = null;
    String pld = getPLD(url);
    tld = pld.substring(pld.lastIndexOf(".") + 1);
    return tld;
  }

  /**
   * Get the paid-level domain of the URL.
   * 
   * @param url URL
   * @return paid-level domain
   */
  public static String getPLD(String url) {
    String pld = null;
    if (url.contains("://")) {
      pld = url.substring(url.indexOf("://") + 3);
    }
    if (pld.contains("/")) {
      pld = pld.substring(0, pld.indexOf("/"));
    }
    return pld;
  }

}
