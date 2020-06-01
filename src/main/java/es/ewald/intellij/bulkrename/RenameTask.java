/*
 * (c) Copyright 2015-20, Limbus Medical Technologies GmbH
 * All rights reserved.
 */
package es.ewald.intellij.bulkrename;

import com.intellij.refactoring.rename.RenameProcessor;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.*;

/**
 * Describes a single rename task.
 */
public class RenameTask {

  /** Needs to be full absolute path. */
  private final String fileName;

  private final String newType;

  /**
   * @see RenameProcessor#isSearchInComments()
   */
  private final boolean searchInComments;

  /**
   * @see RenameProcessor#isSearchTextOccurrences()
   */
  private final boolean searchTextOccurrences;

  public RenameTask(String fileName, String newType, boolean searchInComments, boolean searchTextOccurrences) {
    this.fileName = notBlank(fileName);
    this.newType = notBlank(newType);
    this.searchInComments = searchInComments;
    this.searchTextOccurrences = searchTextOccurrences;
  }

  public String getDescription() {
    return String.format("%s => %s (comments: %s, text: %s)",
        fileName,
        newType,
        searchInComments,
        searchTextOccurrences);
  }

  public static List<RenameTask> loadTasks(String csvFile) throws IOException, CsvException {
    try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withSkipLines(1).build()) {
      return reader.readAll()
          .stream()
          .map(line -> new RenameTask(line[0], line[1], Boolean.parseBoolean(line[2]), Boolean.parseBoolean(line[3])))
          .collect(Collectors.toList());
    }
  }

  public String getFileName() {
    return fileName;
  }

  public String getNewType() {
    return newType;
  }

  public boolean isSearchInComments() {
    return searchInComments;
  }

  public boolean isSearchTextOccurrences() {
    return searchTextOccurrences;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;

    if (o == null || getClass() != o.getClass())
      return false;

    RenameTask task = (RenameTask) o;

    return new EqualsBuilder().append(searchInComments, task.searchInComments)
        .append(searchTextOccurrences, task.searchTextOccurrences)
        .append(fileName, task.fileName)
        .append(newType, task.newType)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(fileName)
        .append(newType)
        .append(searchInComments)
        .append(searchTextOccurrences)
        .toHashCode();
  }
}
