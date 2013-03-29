package com.conductor.checktests;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;

/**
 * Created with IntelliJ IDEA. User: darata Date: 3/28/13 Time: 3:52 PM
 */
public class CheckTestsSearchLevelActionGroup extends ActionGroup {
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        DataContext dataContext = null;
        if (e != null) {
            dataContext = e.getDataContext();
        }
        Project project = null;
        if (dataContext != null) {
            project = PlatformDataKeys.PROJECT.getData(dataContext);
        }
        if (project == null) {
            throw new IllegalStateException("project is null");
        }
        return getChildActions(project, true);
    }

    private static AnAction[] getChildActions(final Project project, final boolean performAction) {
        final AnAction[] childActions = new AnAction[11];
        childActions[0] = new CheckTestsSearchLevelApplier("Find'em All", project, 0, performAction);
        for (int idx = 1; idx < 11; idx++) {
            final AnAction anAction = new CheckTestsSearchLevelApplier("Perform " + idx + " Recursive Searches",
                    project, idx, performAction);
            childActions[idx] = anAction;
        }
        return childActions;
    }

    public static DefaultActionGroup createPopupActionGroup(final Project project, final boolean performAction) {
        final DefaultActionGroup group = new DefaultActionGroup();
        final AnAction[] childActions = getChildActions(project, performAction);
        group.add(childActions[0]);
        group.addSeparator();
        for (int idx = 1; idx < 11; idx++) {
            group.add(childActions[idx]);
        }
        return group;
    }

    private static class CheckTestsSearchLevelApplier extends ToggleAction {
        private Project project;
        private int levelsToSearch;
        private boolean performAction;

        CheckTestsSearchLevelApplier(final String text, final Project project, final int levelsToSearch,
                final boolean performAction) {
            super(text);
            this.project = project;
            this.levelsToSearch = levelsToSearch;
            this.performAction = performAction;
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            final CheckTestsConfiguration checkTestsConfiguration = CheckTestsConfiguration.getInstance(project);
            return checkTestsConfiguration.LEVELS_TO_CHECK_FOR_TESTS == levelsToSearch;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            if (state) {
                final CheckTestsConfiguration checkTestsConfiguration = CheckTestsConfiguration.getInstance(project);
                checkTestsConfiguration.LEVELS_TO_CHECK_FOR_TESTS = levelsToSearch;
                if(performAction) {
                    CheckTestAction.checkForTests(e, project);
                }
            }
        }
    }
}
