/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.model.JDependData;

/**
 * Commandhandler for confirming the selected ProhibitedDependencies.
 * 
 * @author Jens Cornelis
 */
public class ConfirmDependency extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Map<?,?> parameters = event.getParameters();
		JDependData jdependData = DepclipsePlugin.getJDependData();
		List<String[]> tempList = new ArrayList<String[]>();
		
		for(Object selection : parameters.values()) {
			if(selection instanceof ISelection) {
				StructuredSelection sel = (StructuredSelection) selection;
				List<?> selectedElements = sel.toList();
				
				for(Object tmp : selectedElements) {
					String[] selPack = (String[]) tmp;
					tempList.add(new String[] {selPack[2], selPack[3]} );			
				}		
			}
		}		
		jdependData.confirmProhibitedDependency(tempList);
		return null;
	}

}
