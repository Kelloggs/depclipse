/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.plugins.eclipse.depclipse.DepclipsePlugin;

/**
 * Commandhandler refreshing the View by rerunning JDepend.
 * 
 * @author Andrei Loskutov
 */
public class RefreshJDepend extends AbstractHandler {

	@Override
	public boolean isEnabled() {
		return (DepclipsePlugin.getJDependData().getResources().length > 0);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IResource[] dirs = DepclipsePlugin.getJDependData().getResources();
	    runJDependJob(dirs);
		return null;
	}
	
	public final void runJDependJob(final IResource[] resources) {
		Job job = new Job(Messages.RefreshJDepend_JDepend_analysis) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					DepclipsePlugin.getJDependAdapter().startAnalyze(DepclipsePlugin.getJDependData(), resources);

				} catch (Exception e) {
					DepclipsePlugin.handle(e);
					monitor.setCanceled(true);
				}
				return monitor.isCanceled() ? Status.CANCEL_STATUS
						: Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
	
}
