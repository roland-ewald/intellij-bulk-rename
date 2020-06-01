/*
 * (c) Copyright 2015-20, Limbus Medical Technologies GmbH
 * All rights reserved.
 */
package es.ewald.intellij.bulkrename;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Interaction with the IntelliJ refactoring API.
 *
 * @see com.intellij.refactoring.rename.RenameDialog
 */
public class RefactoringUtils {

  private final Project project;

  public RefactoringUtils(Project project) {
    this.project = project;
  }

  Optional<PsiJavaFile> lookUpJavaFile(String fileName) {
    return lookUpJavaFile(project, fileName);
  }

  void renameJavaFile(RenameTask task, PsiJavaFile psiFile) {
    renameJavaFile(project, task, psiFile);
  }

  static Optional<PsiJavaFile> lookUpJavaFile(Project project, String fileName) {
    VirtualFile oldFile = LocalFileSystem.getInstance().findFileByPath(fileName);
    if (oldFile != null) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(oldFile);
      if (psiFile instanceof PsiJavaFile) {
        return Optional.of((PsiJavaFile) psiFile);
      }
    }
    return Optional.empty();
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
}
