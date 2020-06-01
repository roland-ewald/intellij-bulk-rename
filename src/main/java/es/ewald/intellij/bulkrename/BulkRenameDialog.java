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
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.jetbrains.annotations.NotNull;
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
import java.util.Arrays;

public class BulkRenameDialog extends DialogWrapper implements DocumentListener {

  private static final Logger LOG = LoggerFactory.getLogger("es.ewald.intellij.bulkrename.BulkRenameDialog");

  private final Project project;

  private final CollectionListModel<RenameTask> tasks = new CollectionListModel<>();

  private String csvFile = null;

  public BulkRenameDialog(Project project) {
    super(true);
    this.project = project;
    setTitle("Bulk Rename");
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    TextFieldWithBrowseButton csvFileChooser = new TextFieldWithBrowseButton();
    csvFileChooser.addBrowseFolderListener("Choose CSV",
        "Choose CSV",
        project,
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
        if (psiFile instanceof PsiJavaFile) {
          renameJavaFile(project, task, (PsiJavaFile) psiFile);
        }
      }
    }
  }

  static void renameJavaFile(Project project, RenameTask task, PsiJavaFile psiFile) {
    @NotNull PsiClass[] classesInFile = psiFile.getClasses();
    PsiClass classToRename = null;
    if (classesInFile.length == 1) {
      classToRename = classesInFile[0];
    } else if (classesInFile.length > 1) {
      classToRename = Arrays.stream(classesInFile)
          .filter(clazz -> clazz.hasModifierProperty(PsiModifier.PUBLIC))
          .findAny()
          .orElse(null);
    }
    if (classToRename != null) {
      performRename(project,
          classToRename,
          task.getNewType(),
          task.isSearchInComments(),
          task.isSearchTextOccurrences());
    }
  }

  /**
   * @see com.intellij.refactoring.rename.RenameDialog#performRename(String)
   */
  static void performRename(
      Project project,
      @NotNull PsiClass psiClass,
      @NotNull String newName,
      boolean searchComments,
      boolean searchText) {
    final RenamePsiElementProcessor elementProcessor = RenamePsiElementProcessor.forElement(psiClass);
    elementProcessor.setToSearchInComments(psiClass, searchComments);
    elementProcessor.setToSearchForTextOccurrences(psiClass, searchText);
    final RenameProcessor processor = new RenameProcessor(project,
        psiClass,
        newName,
        GlobalSearchScope.projectScope(project),
        searchComments,
        searchText);
    for (AutomaticRenamerFactory factory : AutomaticRenamerFactory.EP_NAME.getExtensionList()) {
      if (factory.isApplicable(psiClass) && factory.getOptionName() != null) {
        processor.addRenamerFactory(factory);
      }
    }
    processor.setPreviewUsages(false);
    processor.run();
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
