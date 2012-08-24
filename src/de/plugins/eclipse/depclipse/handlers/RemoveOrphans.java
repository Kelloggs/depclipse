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
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;

import de.plugins.eclipse.depclipse.DepclipsePlugin;

/**
 * Commandhandler removing the orphaned entries from the RuleEditor
 * 
 * @author Jens Cornelis
 */
public class RemoveOrphans extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean returnCode = MessageDialog
				.openConfirm(DepclipsePlugin.getDefault()
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Messages.RemoveOrphans_Remove_all_orphaned_Dependencies,
						Messages.RemoveOrphans_Remove_Orphaned_Dialog_Text);

		if (returnCode) {
			DepclipsePlugin.getJDependData().removeOrphanedDependencies();
		}
		return null;
	}

}
