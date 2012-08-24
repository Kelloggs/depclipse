/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.*;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import de.plugins.eclipse.depclipse.DepclipsePlugin;

/**
 * View showing the selected ProhibitedDependencies.
 * Observer of JDependData.
 * 
 * @author Jens Cornelis
 */
public class ProhibitedDependencyView extends ViewPart implements PropertyChangeListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "de.plugins.eclipse.depclipse.views.ProhibitedDependencyView"; //$NON-NLS-1$

	private Table table;
	private TableViewer viewer;
	private SashForm sashForm;
	 
	static final class ArrayContentProvider implements IStructuredContentProvider {
		
		String[] data;
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if(newInput != null && newInput instanceof String[]) {
				data = (String[]) newInput;
			}
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (data == null) {
				return new Object[0];
			}
			
			else return data;
		}
	}
	static final class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if(obj instanceof String[]) {
				String[] arr = (String[]) obj;
				if(arr.length <= index) {
					return ""; //$NON-NLS-1$
				}
				return arr[index];
			}
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			String imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE;
			return JavaUI.getSharedImages().getImage(imageKey);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	
	static final class NameSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			String strings1 = (String) e1;
			String strings2 = (String) e2;
			int result = strings1.compareTo(strings2);
			return result;
		}
	}

	/**
	 * The constructor.
	 */
	public ProhibitedDependencyView() {
		super();
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sashForm,
				"de.plugins.eclipse.depclipse.jdepend"); //$NON-NLS-1$
		
		table = createTable(sashForm);
		viewer = new TableViewer(table);
		viewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn viewerNameColumn = new TableViewerColumn(viewer, SWT.NONE);
		viewerNameColumn.getColumn().setText("Prohibited Dependency"); //$NON-NLS-1$
		viewerNameColumn.getColumn().setWidth(250);		

		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		// get initial data - subscribe as observer
		Map<String, List<String>> initialData = DepclipsePlugin.getJDependData().getSelectedBrokenDependencyRules();
		updateUI(initialData);
		DepclipsePlugin.getJDependData().addPropertyChangeListener(this);
	}
	
	public void dispose() {
		DepclipsePlugin.getJDependData().removePropertyChangeListener(this);
	}
	
	protected Table createTable(Composite mySashForm) {
		Table table = new Table(mySashForm, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		return table;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ProhibitedDependencyView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	protected void updateUI(Map<String, List<String>> brokenDependencies) {

		ArrayList<String> data = new ArrayList<String>();
		for(Entry<String, List<String>> rootPackage : brokenDependencies.entrySet()) {	
			for(String depPackage : brokenDependencies.get(rootPackage.getKey())) {
				if(!data.contains(depPackage)) {
					data.add(depPackage);
				}
			}			
		}
		
		viewer.setInput(data.toArray(new String[0]));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("SELECTION_CHANGED")) { //$NON-NLS-1$
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run() {
                    updateUI(DepclipsePlugin.getJDependData().getSelectedBrokenDependencyRules());
                }
            });			
		}	
	}
}