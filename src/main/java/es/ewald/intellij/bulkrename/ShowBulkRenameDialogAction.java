/*
 * (c) Copyright 2015-20, Limbus Medical Technologies GmbH
 * All rights reserved.
 */
package es.ewald.intellij.bulkrename;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ShowBulkRenameDialogAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = event.getProject();
    new BulkRenameDialog(project).show();
  }

  /**
   * @see <a href="https://www.jetbrains.org/intellij/sdk/docs/tutorials/action_system/working_with_custom_actions.html?search=actions#extending-the-update-method">IntelliJ actions tutorial</a>
   */
  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(e.getProject() != null);
  }
}
