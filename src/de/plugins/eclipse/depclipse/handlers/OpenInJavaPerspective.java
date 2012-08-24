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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.OpenJavaPerspectiveAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ISetSelectionTarget;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.model.TreeFolder;
import de.plugins.eclipse.depclipse.model.TreeLeaf;
import de.plugins.eclipse.depclipse.model.TreeObject;
import de.plugins.eclipse.depclipse.views.PackageTreeView;

/**
 * Commandhandler for opening the JavaPerspective for the selected
 * JavaElement
 * 
 * @author Andrei Loskutov
 * @author Jens Cornelis
 */
public class OpenInJavaPerspective extends AbstractHandler {

	@Override
	public boolean isEnabled() {
		return (DepclipsePlugin.getJDependData().getResources().length > 0);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();
		PackageTreeView view = (PackageTreeView) page.findView("de.plugins.eclipse.depclipse.views.PackageTreeView"); //$NON-NLS-1$
		ISelection select = view.getSite().getSelectionProvider().getSelection();
		
		Object obj = ((IStructuredSelection) select).getFirstElement();
        TreeObject to;
        to = (TreeObject)obj;

	    if(!to.isLeaf()){
	    	// if the selected element is a package, this package is selected in the java view
	        IJavaElement [] javaElements = (IJavaElement [])((TreeFolder)to).getIJavaElements().toArray(new IJavaElement [0]);
	        Action oa = new OpenJavaPerspectiveAction();
	        oa.run();

	        IViewPart part = DepclipsePlugin.getActivePage().findView(JavaUI.ID_PACKAGES);
	        if(part instanceof ISetSelectionTarget){
	            ISetSelectionTarget target = (ISetSelectionTarget) part;
	            target.selectReveal(new StructuredSelection(javaElements));
	        }
	    } else {
	    	// if the selected element is a class, this class is opened in the java view
		    IJavaElement javaElement = ((TreeLeaf)to).getIJavaElement();
		    IEditorPart javaEditor = null;
		    try {
		        Action oa = new OpenJavaPerspectiveAction();
		        oa.run();
		        javaEditor = JavaUI.openInEditor(javaElement);
		        JavaUI.revealInEditor(javaEditor, javaElement);
		    } catch (PartInitException e) {
		        DepclipsePlugin.handle(e);
		    } catch (JavaModelException e) {
		        DepclipsePlugin.handle(e);
		    }
		    
	    }
	    return null;
	}

}
