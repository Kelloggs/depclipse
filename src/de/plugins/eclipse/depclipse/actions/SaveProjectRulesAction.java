/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.rules.IRuleParser;

/**
 * Action saving ProjectRules from JDependData model using the IRuleParser
 * 
 * @author Jens Cornelis
 */
public class SaveProjectRulesAction extends Action {
	private IFile file;
	
	public SaveProjectRulesAction(IFile file) {
		this.file = file;
	}

	public void run() {
		IRuleParser parser = DepclipsePlugin.getRuleParser();

		try {
			parser.setProjectRules(file, DepclipsePlugin.getJDependData().getProjectRules());
		} catch (IOException e) {
			MessageDialog.openWarning(null, Messages.SaveProjectRulesAction_Error_saving_File, Messages.SaveProjectRulesAction_Please_check_your_permissions_to_write_this_file);
			DepclipsePlugin.handle(e);
		} catch (CoreException e) {
			MessageDialog.openWarning(null, Messages.SaveProjectRulesAction_Error_saving_File, Messages.SaveProjectRulesAction_Please_check_your_permissions_to_write_this_file); 
			DepclipsePlugin.handle(e);
		}
	}
}
