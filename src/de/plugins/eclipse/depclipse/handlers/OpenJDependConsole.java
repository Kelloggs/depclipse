/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Andrei Loskutov - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.handlers;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import jdepend.framework.JavaPackage;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.handlers.HandlerUtil;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;
import de.plugins.eclipse.depclipse.views.JDependConsole;

/**
 * Commandhandler for opening the JDependConsole and showing JDepend output.
 * 
 * @author Andrei Loskutov
 */
public class OpenJDependConsole extends AbstractHandler {

	@Override
	public boolean isEnabled() {
		return (DepclipsePlugin.getJDependData().getResources().length > 0);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command test = event.getCommand();
		
		boolean oldValue = HandlerUtil.toggleCommandState(test);
		if (oldValue) {
			IViewPart part = DepclipsePlugin.getActivePage().findView(
					IConsoleConstants.ID_CONSOLE_VIEW);
			if (part != null) {
				DepclipsePlugin.getActivePage().hideView(part);
			}
		} else {
			try {
				DepclipsePlugin.getActivePage().showView(
						IConsoleConstants.ID_CONSOLE_VIEW);
				JDependConsole.showConsole();
				runJDepend();
			} catch (PartInitException e) {
				DepclipsePlugin.handle(e);
			}
		}

		return null;
	}

	private void runJDepend() {
		Job job = new Job(Messages.OpenJDependConsole_JDepend_text_output_processing) {
			protected IStatus run(IProgressMonitor monitor) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(stream);
				boolean asXml = DepclipsePlugin.getDefault()
						.getPreferenceStore().getBoolean(
								JDependPreferenceConstants.PREF_OUTPUT_XML);
				jdepend.textui.JDepend jdep;
				if (asXml) {
					jdep = new jdepend.xmlui.JDepend(pw) {
						protected ArrayList<JavaPackage> getPackagesList() {
							return (ArrayList<JavaPackage>) DepclipsePlugin
									.getJDependData().getJavaPackages();
						}
					};
				} else {
					jdep = new jdepend.textui.JDepend(pw) {
						protected ArrayList<JavaPackage> getPackagesList() {
							return (ArrayList<JavaPackage>) DepclipsePlugin
									.getJDependData().getJavaPackages();
						}
					};
				}
				jdep.analyze();
				DepclipsePlugin.getJDependData().setFileOutput(
						stream.toString());

				return monitor.isCanceled() ? Status.CANCEL_STATUS
						: Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

}
