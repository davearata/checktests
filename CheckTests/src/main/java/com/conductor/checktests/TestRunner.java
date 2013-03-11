package com.conductor.checktests;

import java.util.List;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetManager;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.testframework.TestSearchScope;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

/**
 * Created with IntelliJ IDEA. User: darata Date: 3/8/13 Time: 9:24 AM
 */
public class TestRunner {
    public static void runTest(final Project project, final List<PsiClass> testClasses) {
        final RunManagerEx instanceEx = RunManagerEx.getInstanceEx(project);
        final RunnerAndConfigurationSettings configuration = getConfiguration(instanceEx, testClasses);
        if (configuration == null) {
            return;
        }

        Executor myExecutor = null;
        final Executor[] executors = Extensions.getExtensions(Executor.EXECUTOR_EXTENSION_NAME);
        for (final Executor executor : executors) {
            if (executor instanceof DefaultRunExecutor) {
                myExecutor = executor;
            }
        }

        ExecutionTarget target = ExecutionTargetManager.getActiveTarget(project);

        if (myExecutor != null) {
            ExecutionManager.getInstance(project).restartRunProfile(project, myExecutor, target, configuration,
                    (RunContentDescriptor) null);

            RunManagerEx.getInstanceEx(project).addConfiguration(configuration, false);
            instanceEx.setSelectedConfiguration(configuration);
        }
    }

    private static RunnerAndConfigurationSettings getConfiguration(final RunManager runManager,
            final List<PsiClass> testClasses) {
        final ConfigurationType type = getConfigurationType();
        final RunnerAndConfigurationSettingsImpl runnerAndConfigurationSettings = (RunnerAndConfigurationSettingsImpl) runManager
                .createRunConfiguration("CheckTests", type.getConfigurationFactories()[0]);
        final JUnitConfiguration conf = (JUnitConfiguration) runnerAndConfigurationSettings.getConfiguration();
        conf.bePatternConfiguration(testClasses, null);
        //conf.setMainClass(testClasses.get(0));
        final JUnitConfiguration.Data data = conf.getPersistentData();
        data.setScope(TestSearchScope.WHOLE_PROJECT);
        return runnerAndConfigurationSettings;
    }

    private static ConfigurationType getConfigurationType() {
        ConfigurationType[] types = Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP);
        for (ConfigurationType type : types) {
            if (type.getDisplayName().equals("JUnit")) {
                return type;
            }
        }
        return null;
    }
}
