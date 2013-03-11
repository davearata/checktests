package com.conductor.checktests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.ui.ScrollPaneFactory;

/**
 * Created with IntelliJ IDEA. User: darata Date: 2/14/13 Time: 12:30 PM
 */
public class CheckTestAction extends AnAction {

    final static String MESSAGE_DISPOSED = "Check for Tests can't be performed while the project is disposed doing other work";
    final static String TITLE_DISPOSED = "Check For Tests Is not Possible Right Now";
    final static String MESSAGE_DUMB = "Check for Tests can't be performed while IntelliJ IDEA updates the indices in background.\n"
            + "You can commit the changes without running inspections, or you can wait until indices are built.";
    final static String TITLE_DUMB = "Check for Tests is not possible right now";

    public void actionPerformed(AnActionEvent event) throws IllegalStateException {
        final DataContext dataContext = event.getDataContext();
        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            throw new IllegalStateException("project is null");
        }

        if (project.isDisposed()) {
            showMessageDialog(project, MESSAGE_DISPOSED, TITLE_DISPOSED);
            return;
        }

        if (projectIsDumb(project)) {
            showMessageDialog(project, MESSAGE_DUMB, TITLE_DUMB);
            return;
        }

        checkForTests(event, project);
    }

    // VisibleForTesting
    void checkForTests(AnActionEvent event, Project project) {
        final VirtualFile[] virtualFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

        final Set<PsiClass> testClasses;
        if (virtualFiles != null && virtualFiles.length > 0) {
            testClasses = getTestClasses(project, virtualFiles);
        } else {
            testClasses = Sets.newHashSet();
        }

        if (testClasses != null && testClasses.size() > 0) {
            showDialog(project, Lists.newArrayList(testClasses));
        }
    }

    //VisibleForTesting
    Set<PsiClass> getTestClasses(Project project, VirtualFile[] virtualFiles) {
        return TestClassDetector.getInstance(project).findTestClasses(Lists.newArrayList(virtualFiles));
    }

    // VisibleForTesting
    boolean projectIsDumb(Project project) {
        return DumbService.getInstance(project).isDumb();
    }

    // VisibleForTesting
    void showMessageDialog(final Project project, final String message, final String title) {
        Messages.showMessageDialog(project, message, title, null);
    }

    //VisibleForTesting
    void showDialog(final Project project, final List<PsiClass> testClasses) {
        final DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setTitle("CheckTests Results");
        final JTextArea textArea = new JTextArea(10, 50);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        if (testClasses.size() > 0) {
            final List<String> lines = new ArrayList<String>();
            lines.add("Found " + testClasses.size() + " tests, would you like to run them?");
            for (final PsiClass testClass : testClasses) {
                lines.add(testClass.getName());
            }
            textArea.setText(StringUtil.join(lines, "\n"));
            final Runnable runTestsRunnable = new Runnable() {
                @Override
                public void run() {
                    dialogBuilder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
                    TestRunner.runTest(project, testClasses);
                }
            };
            dialogBuilder.setOkOperation(runTestsRunnable);
        } else {
            textArea.setText("Found no tests, maybe you should create one");
        }
        dialogBuilder.setCenterPanel(ScrollPaneFactory.createScrollPane(textArea));
        dialogBuilder.show();
    }
}
