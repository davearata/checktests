package com.conductor.checktests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.vcsUtil.Rethrow;

/**
 * Created with IntelliJ IDEA. User: darata Date: 3/8/13 Time: 3:41 PM
 */
public class TestClassDetectorImpl extends TestClassDetector {
    private final Project myProject;
    private static final Logger LOG = Logger.getInstance("#com.conductor.checktests.TestClassDetectorImpl");
    private Exception myException;

    public TestClassDetectorImpl(final Project project) {
        myProject = project;
    }

    @Override
    public Set<PsiClass> findTestClasses(final List<VirtualFile> virtualFiles) throws ProcessCanceledException {
        final Set<PsiClass> result = Sets.newHashSet();
        boolean completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            @Override
            public void run() {
                try {
                    @Nullable
                    final ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
                    progress.setText("Processing");
                    progress.setIndeterminate(true);
                    final LinkedList<PsiElement> referenceSearchElements = Lists.newLinkedList();
                    for(final VirtualFile virtualFile : virtualFiles) {
                        if (progress.isCanceled()) {
                            throw new ProcessCanceledException();
                        }
                        referenceSearchElements.addAll(getReferenceSearchElements(virtualFile));
                    }
                    result.addAll(findTestClasses(referenceSearchElements));
                } catch (Exception e) {
                    LOG.error(e);
                    myException = e;
                }
            }
        }, "Checking for Tests", true, myProject);

        if (!completed) {
            throw new ProcessCanceledException();
        }
        if (myException != null) {
            Rethrow.reThrowRuntime(myException);
        }

        return result;
    }

    Set<PsiElement> getReferenceSearchElements(final VirtualFile virtualFile) {
        final Set<PsiElement> elements = Sets.newHashSet();
        if(virtualFile != null) {
            final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
            final PsiClass[] psiClasses = PsiUtils.getPsiClasses(psiFile);
            if(psiClasses != null) {
                elements.addAll(Arrays.asList(psiClasses));
            } else {
                elements.add(psiFile);
            }
        }
        return elements;
    }

    // VisibleForTesting
    Set<PsiClass> findTestClasses(final LinkedList<PsiElement> psiElementsToSearch) {
        final Set<PsiClass> testClasses = new HashSet<PsiClass>();
        for(int idx = 0; idx < psiElementsToSearch.size(); idx++) {
            final PsiElement psiElementToSearch = psiElementsToSearch.get(idx);
            final List<PsiReference> psiReferences = new ArrayList<PsiReference>();

            final GlobalSearchScope projectScope = GlobalSearchScope.projectScope(psiElementToSearch.getProject());
            final PsiReference[] references = ReferencesSearch.search(psiElementToSearch, projectScope, false).toArray(
                    new PsiReference[0]);
            psiReferences.addAll(Lists.newArrayList(references));

            for (final PsiReference psiReference : psiReferences) {
                final PsiElement referenceElement = psiReference.getElement();
                final PsiClass referencePsiClass = PsiUtils.getPsiClass(referenceElement);
                if (referencePsiClass != null) {
                    if(!psiElementsToSearch.contains(referencePsiClass)) {
                        psiElementsToSearch.addLast(referencePsiClass);
                    }
                    if(isTestClass(referencePsiClass)) {
                        testClasses.add(referencePsiClass);
                    }
                } else {
                    final PsiFile psiFile = referenceElement.getContainingFile();
                    if(!psiElementsToSearch.contains(psiFile)) {
                        psiElementsToSearch.addLast(psiFile);
                    }
                }
            }
        }
        return testClasses;
    }

    /**
     * Will add the file psiFile to the testClassFile List. if a PsiFile with the same name already exists then it just
     * returns
     *
     * @param newPsiClass
     *            the class to add
     */
    private boolean isTestClass(final PsiClass newPsiClass) {
        if (newPsiClass == null) {
            return false;
        }
        final String name = newPsiClass.getName();
        return name != null && name.contains("Test");
    }
}
