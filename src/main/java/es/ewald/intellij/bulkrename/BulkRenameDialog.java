/*
 * (c) Copyright 2015-20, Limbus Medical Technologies GmbH
 * All rights reserved.
 */
package es.ewald.intellij.bulkrename;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameProcessor;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

public class BulkRenameDialog extends DialogWrapper {

  private final Project project;

  public BulkRenameDialog(Project project) {
    super(true); // use current window as parent
    this.project = project;
    init();
    setTitle("Bulk rename");

    // Using the event, create and show a dialog
//    PsiElement element = null;
//    String newName = "";
//    boolean searchInComments = true;
//    boolean searchInTest = true;
//    new RenameProcessor(project, element, newName, searchInComments, searchInTest);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    JPanel dialogPanel = new JPanel(new BorderLayout());

    JLabel label = new JLabel("bulk rename");
    label.setPreferredSize(new Dimension(100, 100));
    dialogPanel.add(label, BorderLayout.CENTER);

    return dialogPanel;
  }

}
