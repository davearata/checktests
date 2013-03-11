package com.conductor.checktests;

import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: darata
 * Date: 3/11/13
 * Time: 12:52 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class TestClassDetectorImplTest {
    private TestClassDetectorImpl testClassDetector;

    @Mock
    private Project project;

    @Before
    public void setup() {
        testClassDetector = new TestClassDetectorImpl(project);
    }

//    @Test
//    public void testFindTestClasses() {
//        final Set<PsiElement> psiElements = Sets.newHashSet();
//        testClassDetector.findTestClasses(psiElements);
//    }
}
