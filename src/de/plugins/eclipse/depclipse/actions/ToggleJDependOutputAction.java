/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.actions;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import jdepend.framework.JavaPackage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;

/**
 * Action for toggling the format of the JDepend-Output
 * between xml and plain text.
 * 
 * @author Andrei Loskutov
 */
public class ToggleJDependOutputAction extends Action {
	
	private IPreferenceStore prefs;
	
	public ToggleJDependOutputAction(String text, int style) {
		super(text, style);
		prefs = DepclipsePlugin.getDefault().getPreferenceStore();
	}

	public void run() {
		prefs.setValue(JDependPreferenceConstants.PREF_OUTPUT_XML, isChecked());
        runJDepend();
	}
	
	private void runJDepend() {
	    Job job = new Job(Messages.ToggleJDependOutputAction_JDepend_text_output_processing){
	        protected IStatus run(IProgressMonitor monitor) {
	        	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	            PrintWriter pw = new PrintWriter(stream);
	            boolean asXml = prefs
	            	.getBoolean(JDependPreferenceConstants.PREF_OUTPUT_XML);
	            jdepend.textui.JDepend jdep;
	            if(asXml){
	                jdep = new jdepend.xmlui.JDepend(pw){
	                    protected ArrayList<JavaPackage> getPackagesList() {
	                        return (ArrayList<JavaPackage>) DepclipsePlugin.getJDependData().getJavaPackages();
	                    }
	                };
	            } else {
	                jdep = new jdepend.textui.JDepend(pw){
	                    protected ArrayList<JavaPackage> getPackagesList() {
	                        return (ArrayList<JavaPackage>) DepclipsePlugin.getJDependData().getJavaPackages();
	                    }
	                };
	            }
	            jdep.analyze();
	            DepclipsePlugin.getJDependData().setFileOutput(stream.toString());
	            
	            return monitor.isCanceled()?  Status.CANCEL_STATUS : Status.OK_STATUS;
	        }
	    };
	    job.setUser(true);
	    job.setPriority(Job.INTERACTIVE);
	    job.schedule();
	}
}
