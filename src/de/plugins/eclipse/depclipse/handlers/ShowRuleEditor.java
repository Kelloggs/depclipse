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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ui.actions.OpenJavaPerspectiveAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;

/**
 * Coomandhandler showing the RuleEditor.
 * 
 * @author Jens Cornelis
 */
public class ShowRuleEditor extends AbstractHandler {

	@Override
	public boolean isEnabled() {
		IPreferenceStore preferences = DepclipsePlugin.getDefault()
				.getPreferenceStore();
		
		return preferences.getBoolean(JDependPreferenceConstants.USE_FORBIDDEN_DEPENDENCIES)&&
			(DepclipsePlugin.getJDependData().getResources().length > 0);	
	}
	
	@Override
	public Object execute(ExecutionEvent event) {
		IProject project = DepclipsePlugin.getJDependData().getRoot().getIResource().getProject();
		IFile xmlFile = project.getFile(DepclipsePlugin.getDefault().getPreferenceStore().getString(JDependPreferenceConstants.FORBIDDEN_DEPENDENCIES_FILE)); 
		IEditorInput editorInput = new FileEditorInput(xmlFile);
		IWorkbenchPage page = DepclipsePlugin.getActivePage();
	
		try {
	        Action oa = new OpenJavaPerspectiveAction();
	        oa.run();	        
	        page.closeEditor(page.findEditor(editorInput), false);
	        IDE.openEditor(page, editorInput, "de.plugins.eclipse.depclipse.editors.RuleEditor"); //$NON-NLS-1$
		} catch (PartInitException e) {
			DepclipsePlugin.logError(e, "Error while opening RuleEditor"); //$NON-NLS-1$
		}
		return null;
	}
}
