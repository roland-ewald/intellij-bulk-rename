/*
 * (c) Copyright 2015-20, Limbus Medical Technologies GmbH
 * All rights reserved.
 */
package es.ewald.intellij.bulkrename;

import com.intellij.refactoring.rename.RenameProcessor;

import static org.apache.commons.lang3.Validate.*;

/**
 * Describes a single rename task.
 */
public class RenameTask {

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
}
