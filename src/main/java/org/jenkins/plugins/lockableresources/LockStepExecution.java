package org.jenkins.plugins.lockableresources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.EnvVars;
import org.jenkins.plugins.lockableresources.queue.LockableResourcesStruct;

import com.google.inject.Inject;

import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkins.plugins.lockableresources.util.ResourcesNames;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.BodyInvoker;
import org.jenkinsci.plugins.workflow.steps.EnvironmentExpander;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

public class LockStepExecution extends AbstractStepExecutionImpl {

	@Inject(optional = true)
	private LockStep step;

	@StepContextParameter
	private transient Run<?, ?> run;

	@StepContextParameter
	private transient TaskListener listener;

	private static final Logger LOGGER = Logger.getLogger(LockStepExecution.class.getName());

	@Override
	public boolean start() throws Exception {
		step.validate();

		LockUtils.queueLock(step, step.resource, step.label, step.quantity, step.inversePrecedence, step.variable, getContext());

		return false;
	}

	public static void proceed(List<String> resourcenames, StepContext context, String resourceDescription, boolean inversePrecedence, String resourceVariableName) {
		Run<?, ?> r = null;
		try {
			r = context.get(Run.class);
			context.get(TaskListener.class).getLogger().println("Lock acquired on [" + resourceDescription + "]");
		} catch (Exception e) {
			context.onFailure(e);
			return;
		}

        LOGGER.finest("Lock acquired on [" + resourceDescription + "] by " + r.getExternalizableId());

        handleResourceVariable(context, resourcenames, resourceVariableName)
                .withCallback(new Callback(resourcenames, resourceDescription, inversePrecedence))
                .withDisplayName(null)
                .start();
    }

    private static BodyInvoker handleResourceVariable(StepContext context, List<String> resourcenames, String resourceVariableName) {
        BodyInvoker bodyInvoker = context.newBodyInvoker();
        if (resourceVariableName != null) {
            EnvironmentExpander environmentExpander;
            try {
                environmentExpander = EnvironmentExpander.merge(context.get(EnvironmentExpander.class),
                        new ResourceVariableNameExpander(resourceVariableName, resourcenames));
            } catch (Exception e) {
                throw new RuntimeException("adding environment variable for the resource name failed.", e);
            }

            bodyInvoker = bodyInvoker.withContext(environmentExpander);
        }

	    return bodyInvoker;
    }

	public static final class ResourceVariableNameExpander extends EnvironmentExpander {
		private static final long serialVersionUID = 1;
		private final Map<String,String> overrides;

		public ResourceVariableNameExpander(String resourceVariableName, List<String> resourceNames) {
			this.overrides = new HashMap<>();
			this.overrides.put(resourceVariableName, ResourcesNames.joinResourceNames(resourceNames));
		}

		@Override public void expand(EnvVars env) throws IOException, InterruptedException {
			env.overrideAll(overrides);
		}
	}

	private static final class Callback extends BodyExecutionCallback.TailCall {

		private final List<String> resourceNames;
		private final String resourceDescription;
		private final boolean inversePrecedence;

		Callback(List<String> resourceNames, String resourceDescription, boolean inversePrecedence) {
			this.resourceNames = resourceNames;
			this.resourceDescription = resourceDescription;
			this.inversePrecedence = inversePrecedence;
		}

		protected void finished(StepContext context) throws Exception {
			LockableResourcesManager.get().unlockNames(this.resourceNames, context.get(Run.class), this.inversePrecedence);
			context.get(TaskListener.class).getLogger().println("Lock released on resource [" + resourceDescription + "]");
			LOGGER.finest("Lock released on [" + resourceDescription + "]");
		}

		private static final long serialVersionUID = 1L;

	}

	@Override
	public void stop(Throwable cause) throws Exception {
		boolean cleaned = LockableResourcesManager.get().unqueueContext(getContext());
		if (!cleaned) {
			LOGGER.log(Level.WARNING, "Cannot remove context from lockable resource witing list. The context is not in the waiting list.");
		}
		getContext().onFailure(cause);
	}

	private static final long serialVersionUID = 1L;

}
