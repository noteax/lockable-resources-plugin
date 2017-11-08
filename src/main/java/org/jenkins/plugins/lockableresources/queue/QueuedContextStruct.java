/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2016, Florian Hug. All rights reserved.             *
 *                                                                     *
 * This file is part of the Jenkins Lockable Resources Plugin and is   *
 * published under the MIT license.                                    *
 *                                                                     *
 * See the "LICENSE.txt" file for more information.                    *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.jenkins.plugins.lockableresources.queue;

import java.io.Serializable;

import org.jenkinsci.plugins.workflow.steps.StepContext;

/**
 * This class is used to queue pipeline contexts
 * which shall be executed once the necessary
 * resources are free'd.
 */
public class QueuedContextStruct implements Serializable {

	/**
	 * Reference to the pipeline step context.
	 */
	private StepContext context;
	
	/**
	 * Reference to the resources required by the step context.
	 */
	private LockableResourcesStruct lockableResourcesStruct;
	
	/**
	 * Description of the required resources used within logging messages.
	 */
	private String resourceDescription;

	/**
	 * True if the relevant step is "getLock" rather than "lock"
	 */
	private boolean nonBlockScoped = false;

    /**
     * Name of the environment variable holding the resource name
     */
    private String resourceVariableName;

	public QueuedContextStruct(StepContext context, LockableResourcesStruct lockableResourcesStruct, String resourceDescription,
                               String resourceVariableName) {
		this(context, lockableResourcesStruct, resourceDescription, false, resourceVariableName);
	}

	/**
	 * Constructor for the QueuedContextStruct class.
	 */
	public QueuedContextStruct(StepContext context, LockableResourcesStruct lockableResourcesStruct, String resourceDescription,
							   boolean nonBlockScoped,
                               String resourceVariableName) {
		this.context = context;
		this.lockableResourcesStruct = lockableResourcesStruct;
		this.resourceDescription = resourceDescription;
		this.nonBlockScoped = nonBlockScoped;
        this.resourceVariableName = resourceVariableName;
	}
	
	/**
	 * Gets the pipeline step context.
	 */
	public StepContext getContext() {
		return this.context;
	}
	
	/**
	 * Gets the required resources.
	 */
	public LockableResourcesStruct getResources() {
		return this.lockableResourcesStruct;
	}

	/**
	 * Gets the resource description for logging messages.
	 */
	public String getResourceDescription() {
		return this.resourceDescription;
	}

	/**
	 * True if the relevant step is "getLock" (and therefore not block-scoped)
	 */
	public boolean isNonBlockScoped() {
		return nonBlockScoped;
	}

    public String getResourceVariableName() {
        return resourceVariableName;
    }

	private static final long serialVersionUID = 1L;
}
