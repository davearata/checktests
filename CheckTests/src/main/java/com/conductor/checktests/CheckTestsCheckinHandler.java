package com.conductor.checktests;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.intellij.CommonBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.util.PairConsumer;
import com.intellij.util.ui.UIUtil;

/**
 * Created with IntelliJ IDEA. User: darata Date: 3/8/13 Time: 3:08 PM
 */
public class CheckTestsCheckinHandler extends CheckinHandler {
    private final Project myProject;
    private final CheckinProjectPanel myCheckinPanel;
    private static final Logger LOG = Logger.getInstance("#" + CheckTestsCheckinHandler.class.getName());

    public CheckTestsCheckinHandler(final Project project, CheckinProjectPanel panel) {
        myProject = project;
        myCheckinPanel = panel;
    }

    @Override
    @Nullable
    public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        final JCheckBox checkBox = new JCheckBox("Check for Tests");
        return new RefreshableOnComponent() {
            @Override
            public JComponent getComponent() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(checkBox);
                refreshEnable(checkBox);
                return panel;
            }

            @Override
            public void refresh() {
            }

            @Override
            public void saveState() {
                // TODO how do we save settings?
                // getSettings().CHECK_CODE_SMELLS_BEFORE_PROJECT_COMMIT = checkBox.isSelected();
            }

            @Override
            public void restoreState() {
                // TODO how do we retrieve settings?
                checkBox.setSelected(true);
            }
        };
    }

    private void refreshEnable(JCheckBox checkBox) {
        if (DumbService.getInstance(myProject).isDumb()) {
            checkBox.setEnabled(false);
            checkBox.setToolTipText("Check for Tests is impossible until indices are up-to-date");
        } else {
            checkBox.setEnabled(true);
            checkBox.setToolTipText("");
        }
    }

    private VcsConfiguration getSettings() {
        return VcsConfiguration.getInstance(myProject);
    }

    @Override
    public ReturnResult beforeCheckin(CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
        if (true) {// check settings if see if its enabled
            if (DumbService.getInstance(myProject).isDumb()) {
                if (Messages
                        .showOkCancelDialog(
                                myProject,
                                "Check for Tests can't be performed while IntelliJ IDEA updates the indices in background.\n"
                                        + "You can commit the changes without running inspections, or you can wait until indices are built.",
                                "Check for Tests is not possible right now", "&Wait", "&Commit", null) == DialogWrapper.OK_EXIT_CODE) {
                    return ReturnResult.CANCEL;
                }
                return ReturnResult.COMMIT;
            }

            try {
                final Set<PsiClass> testClasses = TestClassDetector.getInstance(myProject).findTestClasses(
                        new ArrayList<VirtualFile>(myCheckinPanel.getVirtualFiles()));
                if (!testClasses.isEmpty()) {
                    return runTests(testClasses, executor);
                } else {
                    return ReturnResult.COMMIT;
                }
            } catch (ProcessCanceledException e) {
                return ReturnResult.CANCEL;
            } catch (Exception e) {
                LOG.error(e);
                if (Messages.showOkCancelDialog(myProject, "Check for Tests failed with exception: "
                        + e.getClass().getName() + ": " + e.getMessage(), "Check for Tests failed", "&Commit",
                        "&Cancel", null) == DialogWrapper.OK_EXIT_CODE) {
                    return ReturnResult.COMMIT;
                }
                return ReturnResult.CANCEL;
            }
        } else {
            return ReturnResult.COMMIT;
        }
    }

    private ReturnResult runTests(final Set<PsiClass> testClasses, @Nullable CommitExecutor executor) {
        String commitButtonText = executor != null ? executor.getActionText() : myCheckinPanel.getCommitActionName();
        if (commitButtonText.endsWith("...")) {
            commitButtonText = commitButtonText.substring(0, commitButtonText.length() - 3);
        }

        final List<String> lines = new ArrayList<String>();
        lines.add("Found " + testClasses.size() + " tests, would you like to run them?");
        for (final PsiClass testClass : testClasses) {
            lines.add(testClass.getName());
        }

        final int answer = Messages.showYesNoCancelDialog(myProject, StringUtil.join(lines, "\n"),
                "Check for Tests Results", "Run Tests", commitButtonText, CommonBundle.getCancelButtonText(),
                UIUtil.getWarningIcon());
        if (answer == 0) {
            TestRunner.runTest(myProject, Lists.newArrayList(testClasses));
            return ReturnResult.CLOSE_WINDOW;
        } else if (answer == 2 || answer == -1) {
            return ReturnResult.CANCEL;
        } else {
            return ReturnResult.COMMIT;
        }
    }
}
