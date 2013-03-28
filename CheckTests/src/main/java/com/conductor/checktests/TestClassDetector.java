package com.conductor.checktests;

import java.util.List;
import java.util.Set;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;

/**
 * Created with IntelliJ IDEA. User: darata Date: 3/8/13 Time: 3:35 PM
 */
public abstract class TestClassDetector {
    public static TestClassDetector getInstance(final Project project) {
        return ServiceManager.getService(project, TestClassDetector.class);
    }

    /**
     * Performs pre-checkin analysis to find tests classes related to the specified files
     * 
     * @param virtualFiles
     *            the files to analyze
     * @return the list of test classes found during analysis
     * @throws ProcessCanceledException
     */
    public abstract Set<PsiClass> findTestClasses(final List<VirtualFile> virtualFiles, final int levelsToSearch)
            throws ProcessCanceledException;
}
