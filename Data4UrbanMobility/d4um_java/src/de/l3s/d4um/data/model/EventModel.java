/*
 * This file is part of Data4UrbanMobility. Copyright (C) 2017 - L3S
 */
package de.l3s.d4um.data.model;

/**
 * The Event model.
 * 
 * @author Chenfeng Zhu
 *
 */
public class EventModel {

  public String id;

  public String name;
  public String name_lang;
  public String desc;
  public String desc_lang;

  public EventModel() {}

  public EventModel(String id) {
    this.id = id;
  }

  public String toString() {
    return "Id(" + id + "): " + name + ", " + desc;
  }

}
