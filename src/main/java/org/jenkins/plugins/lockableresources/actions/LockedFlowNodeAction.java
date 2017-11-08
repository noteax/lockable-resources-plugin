package org.jenkins.plugins.lockableresources.actions;

import hudson.model.InvisibleAction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class LockedFlowNodeAction extends InvisibleAction {
    private List<String> resourceNames = new ArrayList<>();
    private boolean inversePrecedence;
    private String resourceDescription;
    private String resourceVariableName;
    private boolean released;

    public LockedFlowNodeAction(@Nonnull List<String> resourceNames, @CheckForNull String resourceDescription,
                                boolean inversePrecedence, String resourceVariableName) {
        this.resourceNames.addAll(resourceNames);
        this.resourceDescription = resourceDescription;
        this.inversePrecedence = inversePrecedence;
        this.resourceVariableName = resourceVariableName;
    }

    @Nonnull
    public List<String> getResourceNames() {
        return resourceNames;
    }

    @CheckForNull
    public String getResourceDescription() {
        return resourceDescription;
    }

    public boolean isInversePrecedence() {
        return inversePrecedence;
    }

    public void release() {
        this.released = true;
    }

    public String getResourceVariableName() {
        return resourceVariableName;
    }

    public boolean isReleased() {
        return released;
    }
}
