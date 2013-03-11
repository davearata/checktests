package com.conductor.checktests;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.checkin.CodeAnalysisBeforeCheckinHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: darata
 * Date: 3/8/13
 * Time: 3:07 PM
 */
public class CheckTestsCheckinHandlerFactory extends CheckinHandlerFactory {
    @Override
    @NotNull
    public CheckinHandler createHandler(final CheckinProjectPanel panel, CommitContext commitContext) {
        return new CheckTestsCheckinHandler(panel.getProject(), panel);
    }
}