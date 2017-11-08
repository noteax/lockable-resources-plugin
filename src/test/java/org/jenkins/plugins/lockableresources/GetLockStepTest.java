package org.jenkins.plugins.lockableresources;

import hudson.Functions;
import hudson.Launcher;
import hudson.model.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.test.steps.SemaphoreStep;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.*;
import org.jvnet.hudson.test.recipes.WithPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;

public class GetLockStepTest {

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Test
    public void lockOrderLabelQuantity() {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LockableResourcesManager.get().createResourceWithLabel("resource1", "label1");
                WorkflowJob p = story.j.jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "def myVariable = getLock label: 'label1', quantity: 1, variable: 'myVariable'\n" +
                                "echo \"Running with lock ${myVariable}\"\n" +
                                "releaseLock()\n" +
                                "echo 'Finish'\n"
                ));

                WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
                story.j.waitForCompletion(b1);

                story.j.waitForMessage("Running with lock resource1", b1);
                story.j.waitForMessage("Finish", b1);
            }
        });
    }

}
