package com.dacatech.checktests;

import org.jdom.Element;

import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.PlatformUtils;

/**
 * Created with IntelliJ IDEA. User: darata Date: 3/28/13 Time: 11:27 AM
 */
@State(name = "CheckTestsConfiguration", storages = { @Storage(file = StoragePathMacros.WORKSPACE_FILE) })
public class CheckTestsConfiguration implements PersistentStateComponent<Element> {
    private static final Logger LOG = Logger.getInstance("#com.dacatech.checktests.CheckTestsConfiguration");
    private Project project;
    public boolean CHECK_FOR_TESTS_BEFORE_PROJECT_COMMIT = !PlatformUtils.isPyCharm() && !PlatformUtils.isRubyMine();
    public int LEVELS_TO_CHECK_FOR_TESTS = 0;

    /**
     * Create a new configuration bean.
     * 
     * @param project
     *            the project we belong to.
     */
    public CheckTestsConfiguration(final Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project is required");
        }

        this.project = project;
    }

    public Element getState() {
        try {
            final Element e = new Element("state");
            writeExternal(e);
            return e;
        } catch (WriteExternalException e1) {
            LOG.error(e1);
            return null;
        }
    }

    public void loadState(Element state) {
        try {
            readExternal(state);
        } catch (InvalidDataException e) {
            LOG.error(e);
        }
    }

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    public static CheckTestsConfiguration getInstance(Project project) {
        return ServiceManager.getService(project, CheckTestsConfiguration.class);
    }
}
