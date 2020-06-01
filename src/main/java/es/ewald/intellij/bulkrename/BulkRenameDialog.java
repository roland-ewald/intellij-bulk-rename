/*
 * (c) Copyright 2015-20, Limbus Medical Technologies GmbH
 * All rights reserved.
 */
package es.ewald.intellij.bulkrename;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBList;
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
import java.io.IOException;
import java.util.Optional;

public class BulkRenameDialog extends DialogWrapper implements DocumentListener {

  private static final Logger LOG = LoggerFactory.getLogger("es.ewald.intellij.bulkrename.BulkRenameDialog");

  private final Project project;

  private final CollectionListModel<RenameTask> tasks = new CollectionListModel<>();

  private final RefactoringUtils refactoringUtils;

  public BulkRenameDialog(Project project) {
    this(project, new RefactoringUtils(project));
  }

  BulkRenameDialog(Project project, RefactoringUtils refactoringUtils) {
    super(true);
    this.project = project;
    this.refactoringUtils = refactoringUtils;
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
    renameTaskLists.setCellRenderer(new RenameTaskListCellRenderer());

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(csvFileChooser, BorderLayout.NORTH);
    panel.add(renameTaskLists, BorderLayout.CENTER);
    return panel;
  }

  @Override
  protected void doOKAction() {
    super.doOKAction();
    for (RenameTask task : tasks.getItems()) {
      LOG.info("Renaming class in '{}' to '{}'", task.getFileName(), task.getNewType());
      Optional<PsiJavaFile> javaFile = refactoringUtils.lookUpJavaFile(task.getFileName());
      javaFile.ifPresent(file -> refactoringUtils.renameJavaFile(task, file));
    }
  }

  @Override
  public void insertUpdate(DocumentEvent documentEvent) {
    changedUpdate(documentEvent);
  }

  @Override
  public void removeUpdate(DocumentEvent documentEvent) {
    tasks.removeAll();
  }

  @Override
  public void changedUpdate(DocumentEvent documentEvent) {
    try {
      reloadTasks(documentEvent.getDocument().getText(0, documentEvent.getLength()));
    } catch (BadLocationException | IOException | CsvException e) {
      LOG.error("Could not load rename tasks.", e);
    }
  }

  private void reloadTasks(String csvFile) throws IOException, CsvException {
    tasks.removeAll();
    if (csvFile != null) {
      tasks.add(RenameTask.loadTasks(csvFile));
    }
  }

  @SuppressWarnings("java:S110") // Too many parents in IntelliJ's type hierarchy can't be avoided here
  private static final class RenameTaskListCellRenderer extends SimpleListCellRenderer<RenameTask> {
    @Override
    public void customize(
        @NotNull JList<? extends RenameTask> list, RenameTask task, int index, boolean selected, boolean hasFocus) {
      setText(task.getDescription());
    }
  }
}
