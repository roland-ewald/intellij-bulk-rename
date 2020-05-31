/*
 * (c) Copyright 2015-20, Limbus Medical Technologies GmbH
 * All rights reserved.
 */
package es.ewald.intellij.bulkrename;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.refactoring.RenameRefactoring;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;

public class BulkRenameDialog extends DialogWrapper implements DocumentListener {

  private static final Logger LOG = LoggerFactory.getLogger("es.ewald.intellij.bulkrename.BulkRenameDialog");

  private final Project project;

  private final CollectionListModel<RenameTask> tasks = new CollectionListModel<>();

  private String csvFile = null;

  public BulkRenameDialog(Project project) {
    super(true);
    this.project = project;
    init();
    setTitle("Bulk Rename");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    TextFieldWithBrowseButton csvFileChooser = new TextFieldWithBrowseButton();
    csvFileChooser.addBrowseFolderListener("Choose CSV", "Choose CSV", project,
        FileChooserDescriptorFactory.createSingleFileDescriptor("csv"));
    csvFileChooser.getTextField().getDocument().addDocumentListener(this);

    JBList<RenameTask> renameTaskLists = new JBList<>(tasks);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(csvFileChooser, BorderLayout.NORTH);
    panel.add(renameTaskLists, BorderLayout.CENTER);
    return panel;
  }

  @Override
  protected void doOKAction() {
    super.doOKAction();
    for (RenameTask task : tasks.getItems()) {
      LOG.info("Renaming {} into {}", task.getFileName(), task.getNewType());
      VirtualFile oldFile = LocalFileSystem.getInstance().findFileByPath(task.getFileName());
      if (oldFile != null) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(oldFile);
        if (psiFile != null) {
          RenameRefactoring rename = RefactoringFactory.getInstance(project)
              .createRename(psiFile, task.getNewType(), task.isSearchInComments(), task.isSearchTextOccurrences());
          rename.run();
        }
      }
    }
  }

  @Override
  public void insertUpdate(DocumentEvent documentEvent) {
    changedUpdate(documentEvent);
  }

  @Override
  public void removeUpdate(DocumentEvent documentEvent) {
    csvFile = null;
    tasks.removeAll();
  }

  @Override
  public void changedUpdate(DocumentEvent documentEvent) {
    try {
      csvFile = documentEvent.getDocument().getText(0, documentEvent.getLength());
      reloadTasks();
    } catch (BadLocationException | IOException | CsvException e) {
      LOG.error("Could not load rename tasks.", e);
    }
  }

  private void reloadTasks() throws IOException, CsvException {
    tasks.removeAll();
    if (csvFile != null) {
      try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withSkipLines(1).build()) {
        for (String[] line : reader.readAll()) {
          tasks.add(new RenameTask(line[0], line[1], false, false));
        }
      }
    }
  }
}
