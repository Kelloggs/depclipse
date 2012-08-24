/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.model.TreeFolder;
import de.plugins.eclipse.depclipse.model.TreeLeaf;
import de.plugins.eclipse.depclipse.model.TreeObject;

/**
 * Action handling a selection made by the user on the PackageTreeView.
 * 
 * @author Andrei Loskutov
 */
public class PackageTreeSelectionChangedAction extends Action {

	public void runWithEvent(SelectionChangedEvent event) {
		IStructuredSelection selectedStructure = (IStructuredSelection)event.getSelection();
        ArrayList<IResource> al = new ArrayList<IResource>();
        
        Iterator<?> iter = selectedStructure.iterator();
        IResource [] resources;
        while (iter.hasNext()) {
            TreeObject o = (TreeObject) iter.next();
            if (o.isLeaf()) {
                TreeLeaf tleaf = (TreeLeaf) o;
                if(tleaf.getIResource() != null){
                    al.add(tleaf.getIResource());
                }
            } else {
                TreeFolder tfolder = (TreeFolder)o;
                try {
					resources = tfolder.getIResources();           
					for (int i = 0; i < resources.length; i++) {
                    if(!al.contains(resources[i])){
                        al.add(resources[i]);
                    }
                }
				} catch (JavaModelException e) {
					DepclipsePlugin.handle(e);
				}
     
            }
        }
        DepclipsePlugin.getJDependData().setSelection((IResource[]) al.toArray(new IResource[0]));		
	}

}
