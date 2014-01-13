package com.dacatech.checktests;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;

@RunWith(MockitoJUnitRunner.class)
public class CheckTestActionTest {
    private CheckTestAction checkTestAction;

    @Before
    public void testCheckTestAction() {
        checkTestAction = new CheckTestAction();
    }

    @Test(expected = IllegalStateException.class)
    public void testActionPerformedProjectIsNull() {
        final AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
        final DataContext dataContext = Mockito.mock(DataContext.class);
        Mockito.when(anActionEvent.getDataContext()).thenReturn(dataContext);
        Mockito.when(dataContext.getData(Mockito.anyString())).thenReturn(null);
        checkTestAction.actionPerformed(anActionEvent);
    }

    @Test
    public void testActionPerformedProjectIsDisposed() {
        final AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
        final DataContext dataContext = Mockito.mock(DataContext.class);
        Mockito.when(anActionEvent.getDataContext()).thenReturn(dataContext);
        final Project project = Mockito.mock(Project.class);
        Mockito.when(dataContext.getData(Mockito.anyString())).thenReturn(project);
        Mockito.when(project.isDisposed()).thenReturn(true);
        final CheckTestAction checkTestActionSpy = Mockito.spy(checkTestAction);
        Mockito.doNothing().when(checkTestActionSpy)
                .showMessageDialog(Mockito.any(Project.class), Mockito.anyString(), Mockito.anyString());
        checkTestActionSpy.actionPerformed(anActionEvent);
        Mockito.verify(checkTestActionSpy).showMessageDialog(project, CheckTestAction.MESSAGE_DISPOSED,
                CheckTestAction.TITLE_DISPOSED);
        Mockito.verify(checkTestActionSpy, Mockito.never()).checkTests(anActionEvent, project);
    }

    @Test
    public void testActionPerformedProjectIsDumb() {
        final AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
        final DataContext dataContext = Mockito.mock(DataContext.class);
        Mockito.when(anActionEvent.getDataContext()).thenReturn(dataContext);
        final Project project = Mockito.mock(Project.class);
        Mockito.when(dataContext.getData(Mockito.anyString())).thenReturn(project);
        Mockito.when(project.isDisposed()).thenReturn(false);
        final CheckTestAction checkTestActionSpy = Mockito.spy(checkTestAction);
        Mockito.doNothing().when(checkTestActionSpy)
                .showMessageDialog(Mockito.any(Project.class), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(checkTestActionSpy).projectIsDumb(project);
        checkTestActionSpy.actionPerformed(anActionEvent);
        Mockito.verify(checkTestActionSpy).showMessageDialog(project, CheckTestAction.MESSAGE_DUMB,
                CheckTestAction.TITLE_DUMB);
        Mockito.verify(checkTestActionSpy, Mockito.never()).checkTests(anActionEvent, project);
    }

    @Test
    public void testActionPerformed() {
        final AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
        final DataContext dataContext = Mockito.mock(DataContext.class);
        Mockito.when(anActionEvent.getDataContext()).thenReturn(dataContext);
        final Project project = Mockito.mock(Project.class);
        Mockito.when(dataContext.getData(Mockito.anyString())).thenReturn(project);
        Mockito.when(project.isDisposed()).thenReturn(false);
        final CheckTestAction checkTestActionSpy = Mockito.spy(checkTestAction);
        Mockito.doNothing().when(checkTestActionSpy)
                .showMessageDialog(Mockito.any(Project.class), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(false).when(checkTestActionSpy).projectIsDumb(project);
        Mockito.doNothing().when(checkTestActionSpy).checkTests(anActionEvent, project);
        checkTestActionSpy.actionPerformed(anActionEvent);
        Mockito.verify(checkTestActionSpy).checkTests(anActionEvent, project);
    }

    @Test
    public void testCheckForTestsNoFilesSelected() {
        final AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
        final Project project = Mockito.mock(Project.class);
        final CheckTestAction checkTestActionSpy = Mockito.spy(checkTestAction);
        Mockito.when(anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(null);
        checkTestActionSpy.checkTests(anActionEvent, project);
        Mockito.verify(checkTestActionSpy, Mockito.never()).getTestClasses(Mockito.any(Project.class),
                Mockito.any(VirtualFile[].class));
        Mockito.verify(checkTestActionSpy, Mockito.never()).showTestListDialog(Mockito.any(Project.class),
                Mockito.anyListOf(PsiClass.class));
    }

    @Test
    public void testCheckForTestsNoTestsFound() {
        final AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
        final Project project = Mockito.mock(Project.class);
        final CheckTestAction checkTestActionSpy = Mockito.spy(checkTestAction);
        final VirtualFile virtualFile = Mockito.mock(VirtualFile.class);
        final VirtualFile[] virtualFiles = new VirtualFile[] { virtualFile };
        Mockito.when(anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(virtualFiles);
        Mockito.doReturn(null).when(checkTestActionSpy).getTestClasses(project, virtualFiles);
        checkTestActionSpy.checkTests(anActionEvent, project);
        Mockito.verify(checkTestActionSpy, Mockito.never()).showTestListDialog(Mockito.any(Project.class),
                Mockito.anyListOf(PsiClass.class));
    }

    @Test
    public void testCheckForTests() {
        final AnActionEvent anActionEvent = Mockito.mock(AnActionEvent.class);
        final Project project = Mockito.mock(Project.class);
        final CheckTestAction checkTestActionSpy = Mockito.spy(checkTestAction);
        final VirtualFile virtualFile = Mockito.mock(VirtualFile.class);
        final VirtualFile[] virtualFiles = new VirtualFile[] { virtualFile };
        Mockito.when(anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)).thenReturn(virtualFiles);
        final Set<PsiClass> testClasses = new HashSet<PsiClass>();
        testClasses.add(Mockito.mock(PsiClass.class));
        Mockito.doReturn(testClasses).when(checkTestActionSpy).getTestClasses(project, virtualFiles);
        Mockito.doNothing().when(checkTestActionSpy).showTestListDialog(Mockito.any(Project.class), Mockito.anyListOf(PsiClass.class));
        checkTestActionSpy.checkTests(anActionEvent, project);
        Mockito.verify(checkTestActionSpy).showTestListDialog(Mockito.any(Project.class), Mockito.anyListOf(PsiClass.class));
    }
}
