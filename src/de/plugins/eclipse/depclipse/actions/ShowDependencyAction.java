/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.plugins.eclipse.depclipse.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.xml.sax.SAXParseException;

import com.thoughtworks.xstream.converters.ConversionException;

import de.plugins.eclipse.depclipse.DepclipsePlugin;

/**
 * Action for running JDepend and showing JDepend4Eclipse-Perspective
 * 
 * @author Andrei Loskutov
 */
public class ShowDependencyAction implements IObjectActionDelegate {

	/** Stores the selection. */
	private IStructuredSelection selection;

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction,
	 *      IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// noop
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		final IResource[] resources = getSelectedResources();
		if (resources.length == 0) {
			return;
		}		
		
		runJDependJob(resources);
		
		IWorkbench workb = DepclipsePlugin.getDefault().getWorkbench();
		try {
			workb.showPerspective(
					"de.plugins.eclipse.depclipse.ui.JDependPerspective",  //$NON-NLS-1$
					workb.getActiveWorkbenchWindow());
		} catch (Exception e) {
			DepclipsePlugin.handle(e);
			return;
		}
			
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection mySelection) {
		if (mySelection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) mySelection;
			if (action != null) {
				try {
					action.setEnabled(isEnabled());
				} catch (Exception e) {
					action.setEnabled(false);
					DepclipsePlugin.handle(e);
				}
			}
		}
	}

	protected boolean isEnabled() throws Exception {
		IResource[] resources = getSelectedResources();
		return resources.length > 0;
	}

	/**
	 * Returns the selected resources.
	 * 
	 * @return the selected resources
	 */
	protected IResource[] getSelectedResources() {
		ArrayList<IResource> resources = new ArrayList<IResource>();
		if (selection != null && !selection.isEmpty()) {
			for (Iterator<?> elements = selection.iterator(); elements
					.hasNext();) {
				IResource next = (IResource) elements.next();
				if (next == null) {
					continue;
				}
				if (next instanceof IFolder) {
					resources.add((IFolder) next);
					continue;
				} else {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(IFolder.class);
					if (adapter instanceof IFolder) {
						resources.add((IFolder) adapter);
						continue;
					}
				}
				if (next instanceof IJavaElement) {
					try {
						IResource javaRes = ((IJavaElement) next)
								.getCorrespondingResource();
						if (javaRes != null
								&& javaRes.getType() == IResource.FOLDER) {
							resources.add(javaRes);
						}
					} catch (JavaModelException e) {
						DepclipsePlugin.handle(e);
					}
				}
			}
		}

		if (!resources.isEmpty()) {
			return (IResource[]) resources.toArray(new IResource[0]);
		}
		return new IResource[0];
	}
	
	public final void runJDependJob(final IResource[] resources) {
		Job job = new Job(Messages.ShowDependencyAction_JDepend_anaylsis) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					DepclipsePlugin.getJDependAdapter().startAnalyze(DepclipsePlugin.getJDependData(), resources);
				} catch (SAXParseException e) {
					DepclipsePlugin.handle(e);
				} catch (IOException e) {
					DepclipsePlugin.handle(e);
				} catch (CoreException e) {
					DepclipsePlugin.handle(e);
				} catch (ConversionException e) {
					DepclipsePlugin.handle(e);
					return new Status(IStatus.ERROR, DepclipsePlugin.ID, 1, 
							Messages.ShowDependencyAction_JDR_File_Error_Message
							, e);        	
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
