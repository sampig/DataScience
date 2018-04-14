package de.l3s.d4um.data;

import de.l3s.d4um.utils.ConfigUtils;

@Deprecated
public class LocationData {

  private static final String DEFAULT_PATH = ConfigUtils.getDataDirectory();

  private String fileEvent = DEFAULT_PATH + "/" + ConfigUtils.getEventDataFilename();

  private String fileLocation = DEFAULT_PATH + "/location.nq";

  public void loadData() {
    if (this.fileEvent == null || this.fileLocation == null) {
      return;
    }
  }

  public void setEvent(String event) {
    this.fileEvent = event;
  }

  public void setLocation(String location) {
    this.fileLocation = location;
  }

  public static void main(String... strings) {
    LocationData ld = new LocationData();
    ld.loadData();
  }

}
