package de.l3s.d4um;

import de.l3s.d4um.data.DatasetExtractor;
import de.l3s.d4um.data.DatasetExtractorNegative;
import de.l3s.d4um.utils.ConfigUtils;

public class SDType {

  private String path = ConfigUtils.getDataDirectory();

  private String inputDataPositive = path + "/eventsQuadsNewYorkPositive.nq";
  private String inputIdPositive = path + "/eventsIdsNewYorkPositive.tsv";
  private String inputDataNegative = path + "/eventsQuadsNewYorkNegative.nq";
  private String inputIdNegative = path + "/eventsIdsNewYorkNegative.tsv";
  private String outputDataPositive = path + "/eventPositive.nq";
  private String outputDataNegative = path + "/eventNegativeNew.nq";

  private int size = 12420;

  public void extract() {
    // Extract dataset.
    DatasetExtractor de = new DatasetExtractor();
    de.setId(inputIdPositive);
    de.setDataset(inputDataPositive);
    de.setOutput(outputDataPositive);
    de.readEventIds();
    de.extractData();
    de = new DatasetExtractor();
    de.setId(inputIdNegative);
    de.setDataset(inputDataNegative);
    de.setOutput(path + "/eventNegative.nq");
    de.readEventIds();
    de.extractData();

    // Extract negative dataset.
    DatasetExtractorNegative den = new DatasetExtractorNegative();
    den.setIdNegative(inputIdNegative);
    den.setDataset(path + "/eventNegative.nq");
    den.setOutput(outputDataNegative);
    den.setSize(size);
  }

  public static void main(String... strings) {}

}
