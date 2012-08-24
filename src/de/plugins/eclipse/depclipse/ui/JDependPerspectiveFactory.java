/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/

/**
 * Created on 31.12.2002
 */
package de.plugins.eclipse.depclipse.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import de.plugins.eclipse.depclipse.views.DependencyView;
import de.plugins.eclipse.depclipse.views.MetricsView;
import de.plugins.eclipse.depclipse.views.PackageTreeView;
import de.plugins.eclipse.depclipse.views.ProhibitedDependencyView;

/**
 * @author Andy the Great
 */
public class JDependPerspectiveFactory implements IPerspectiveFactory {

    /**
     * Constructor for JDependPerspectiveFactory.
     */
    public JDependPerspectiveFactory() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        layout.setEditorAreaVisible(false);      

        // add items for Window->Open Perspective menu in JDepend-Perspective
        layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaPerspective"); //$NON-NLS-1$
        layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaHierarchyPerspective"); //$NON-NLS-1$
        layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaBrowsingPerspective"); //$NON-NLS-1$
        
        // add items for Window->Show View menu in JDepend-Perspective
        layout.addShowViewShortcut("de.plugins.eclipse.depclipse..views.DependencyView"); //$NON-NLS-1$
        layout.addShowViewShortcut("de.plugins.eclipse.depclipse..views.PackageTreeView"); //$NON-NLS-1$
        layout.addShowViewShortcut("de.plugins.eclipse.depclipse..views.MetricsView"); //$NON-NLS-1$
        layout.addShowViewShortcut("de.plugins.eclipse.depclipse..views.ProhibitedDependencyView"); //$NON_NLS-1$
        layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView"); //$NON-NLS-1$
        layout.addShowViewShortcut("org.eclipse.jdt.ui.PackageExplorer"); //$NON-NLS-1$
        layout.addShowViewShortcut("org.eclipse.ui.views.ResourceNavigator"); //$NON-NLS-1$
  
        IFolderLayout top =
            layout.createFolder("top", IPageLayout.LEFT, 1f, editorArea);  //$NON-NLS-1$
        top.addView(DependencyView.ID);      
        top.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
        
        IFolderLayout bottom_left =
            layout.createFolder("bottom_left", IPageLayout.LEFT, 0.25f, DependencyView.ID);  //$NON-NLS-1$
        bottom_left.addView(MetricsView.ID);

        IFolderLayout left =
            layout.createFolder("left", IPageLayout.TOP, 0.7f, MetricsView.ID);     //$NON-NLS-1$
        left.addView(PackageTreeView.ID);
        
        IFolderLayout right = 
        	layout.createFolder("right", IPageLayout.RIGHT, 0.75f, DependencyView.ID ); //$NON-NLS-1$
        right.addView(ProhibitedDependencyView.ID);
    }

}
