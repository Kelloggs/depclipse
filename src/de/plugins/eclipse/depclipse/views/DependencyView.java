/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 * 				 Jens Cornelis - changed call
 *******************************************************************************/

package de.plugins.eclipse.depclipse.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.model.TreeFolder;
import de.plugins.eclipse.depclipse.model.TreeLeaf;
import de.plugins.eclipse.depclipse.model.TreeObject;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;

public class DependencyView extends ViewPart implements PropertyChangeListener{
	private static final int FIRST_COLUMN = 0;
	private static final int LAST_COLUMN = 8;

	static final class PackageSorter extends ViewerSorter {
		protected boolean reverse;
		protected int column;

		protected int getPriority() {
			return column;
		}

		public void reversePriority() {
			reverse = !reverse;
		}

		public void setPriority(int column) {
			this.column = column;
		}

		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof String[] && e2 instanceof String[]) {
				String[] strings1 = (String[]) e1;
				String[] strings2 = (String[]) e2;
				if (strings1.length <= column || strings2.length <= column) {
					return super.compare(viewer, e1, e2);
				}
				int result;
				switch (column) {
				case FIRST_COLUMN:
				case LAST_COLUMN:
					result = strings1[column].compareTo(strings2[column]);
					break;
				default:
					result = Float.valueOf(strings1[column]).compareTo(
							Float.valueOf(strings2[column]));
					break;
				}
				if (reverse) {
					result = -result;
				}
				return result;
			}
			return super.compare(viewer, e1, e2);
		}
	}

	private SashForm sashForm;

	private Table cycleTable;
	private Table effTable;
	private Table affTable;
	private Table selTable;

	private TableViewer selViewer;
	private TableViewer cycleViewer;
	private TableViewer affViewer;
	private TableViewer effViewer;

	private boolean disposed;

	final static String[] columnHeaders = { Messages.DependencyView_column_Package, Messages.DependencyView_column_CC,
			Messages.DependencyView_column_AC, Messages.DependencyView_column_Ca, Messages.DependencyView_column_Ce, Messages.DependencyView_column_A, Messages.DependencyView_column_I, Messages.DependencyView_column_D, Messages.DependencyView_column_Cycle };

	private ColumnLayoutData[] columnLayouts = {
			new ColumnWeightData(50), new ColumnWeightData(10),
			new ColumnWeightData(10), new ColumnWeightData(10),
			new ColumnWeightData(10), new ColumnWeightData(10),
			new ColumnWeightData(10), new ColumnWeightData(10),
			new ColumnWeightData(10) };

	/** The view's identifier */
	public static final String ID = DepclipsePlugin.ID
			+ ".views.DependencyView"; //$NON-NLS-1$

	static final class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof String[]) {
				String[] arr = (String[]) obj;
				if (index < arr.length) {
					if (index + 1 == arr.length) {
						return ""; //$NON-NLS-1$
					}
					return arr[index];
				}
			}
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			if (obj instanceof String[]) {
				String[] arr = (String[]) obj;
				if (index < arr.length) {
					if ("true".equalsIgnoreCase(arr[index])) { //$NON-NLS-1$
						return DepclipsePlugin.getDefault().getImageDescriptor("cycle.png").createImage(); //$NON-NLS-1$
					}
				}
			}
			return index == 0 ? getImage(obj) : null;
		}

		public Image getImage(Object obj) {
			return JavaUI.getSharedImages().getImage(
					ISharedImages.IMG_OBJS_PACKAGE);
		}

	}

	static final class PackageViewContentProvider implements
			IStructuredContentProvider {

		JavaPackage[] elements;
		String[][] data;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput != null && newInput instanceof JavaPackage[]) {
				setElements((JavaPackage[]) newInput);
			}
		}

		public void dispose() {
			// ignored
		}

		private String getShortFloat(float f) {
			StringBuffer sb = new StringBuffer();
			int i = (int) f;
			sb.append(i);
			f = (f - i) * 100;
			i = (int) f;
			sb.append('.');
			if (i < 10) {
				sb.append('0');
			}
			sb.append(i);
			return sb.toString();
		}

		public Object[] getElements(Object parent) {
			if (elements == null) {
				return new Object[0];
			}
			data = new String[elements.length][];
			for (int elt = 0; elt < data.length; elt++) {
				String[] row = new String[columnHeaders.length];
				for (int column = 0; column < row.length; column++) {
					switch (column) {
					case FIRST_COLUMN:
						row[column] = "" + elements[elt].getName(); //$NON-NLS-1$
						break;
					case FIRST_COLUMN + 1:
						row[column] = "" //$NON-NLS-1$
								+ elements[elt].getConcreteClassCount();
						break;
					case FIRST_COLUMN + 2:
						row[column] = "" //$NON-NLS-1$
								+ elements[elt].getAbstractClassCount();
						break;
					case FIRST_COLUMN + 3:
						row[column] = "" + elements[elt].afferentCoupling(); //$NON-NLS-1$
						break;
					case FIRST_COLUMN + 4:
						row[column] = "" + elements[elt].efferentCoupling(); //$NON-NLS-1$
						break;
					case FIRST_COLUMN + 5:
						row[column] = getShortFloat(elements[elt]
								.abstractness());
						break;
					case FIRST_COLUMN + 6:
						row[column] = getShortFloat(elements[elt].instability());
						break;
					case FIRST_COLUMN + 7:
						row[column] = getShortFloat(elements[elt].distance());
						break;
					case LAST_COLUMN:
						row[column] = "" + elements[elt].containsCycle(); //$NON-NLS-1$
						break;
					default:
						break;
					}
				}
				data[elt] = row;
			}
			return data;
		}

		/**
		 * Sets the elements.
		 * 
		 * @param elements
		 *            The elements to set
		 */
		public void setElements(JavaPackage[] elements) {
			this.elements = elements;
		}

	}

	/**
	 * The constructor.
	 */
	public DependencyView() {
		super();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sashForm,
				"de.plugins.eclipse.depclipse"); //$NON-NLS-1$

		Group group1 = new Group(sashForm, SWT.NONE);
		GridLayout gridLayout1 = new GridLayout();
		group1.setLayout(gridLayout1);
		gridLayout1.numColumns = 1;
		group1
				.setLayoutData(new GridData(GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL
						| GridData.VERTICAL_ALIGN_FILL));
		group1.setText(Messages.DependencyView_Selected_Objects);

		selTable = createTable(group1);
		selViewer = new TableViewer(selTable);
		selViewer.setContentProvider(new PackageViewContentProvider());
		selViewer.setLabelProvider(new ViewLabelProvider());
		createColumns(selTable, selViewer);

		Group group = new Group(sashForm, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		group.setLayout(gridLayout);
		gridLayout.numColumns = 1;
		group
				.setLayoutData(new GridData(GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL
						| GridData.VERTICAL_ALIGN_FILL));
		group.setText(Messages.DependencyView_Packages_with_Cycle);

		cycleTable = createTable(group);
		cycleViewer = new TableViewer(cycleTable);
		cycleViewer.setContentProvider(new PackageViewContentProvider());
		cycleViewer.setLabelProvider(new ViewLabelProvider());
		createColumns(cycleTable, cycleViewer);

		// second table with depends on
		Group group2 = new Group(sashForm, SWT.NONE);
		GridLayout gridLayout2 = new GridLayout();
		group2.setLayout(gridLayout2);
		gridLayout.numColumns = 1;
		group2
				.setLayoutData(new GridData(GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL
						| GridData.VERTICAL_ALIGN_FILL));
		group2.setText(Messages.DependencyView_Depends_Upon);

		effTable = createTable(group2);
		effViewer = new TableViewer(effTable);
		effViewer.setContentProvider(new PackageViewContentProvider());
		effViewer.setLabelProvider(new ViewLabelProvider());
		createColumns(effTable, effViewer);

		// third table with used by
		Group group3 = new Group(sashForm, SWT.NONE);
		GridLayout gridLayout3 = new GridLayout();
		group3.setLayout(gridLayout3);
		gridLayout.numColumns = 1;
		group3
				.setLayoutData(new GridData(GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL
						| GridData.VERTICAL_ALIGN_FILL));
		group3.setText(Messages.DependencyView_Used_By);

		affTable = createTable(group3);
		affViewer = new TableViewer(affTable);
		affViewer.setContentProvider(new PackageViewContentProvider());
		affViewer.setLabelProvider(new ViewLabelProvider());
		createColumns(affTable, affViewer);

		// set percentual
		sashForm.setWeights(new int[] { 15, 15, 35, 35 });

		setTooltipText(Messages.DependencyView_JDependAnalysisViewerToolTip);
		DepclipsePlugin.getJDependData().addPropertyChangeListener(this);
	}

	public void setTooltipText(String text) {
		selTable.setToolTipText(text);
		effTable.setToolTipText(text);
		affTable.setToolTipText(text);
		cycleTable.setToolTipText(text);
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

	private void createColumns(final Table table, final TableViewer viewer) {
		TableLayout layout = (TableLayout) table.getLayout();
		final PackageSorter sorter = new PackageSorter();
		viewer.setSorter(sorter);
		SelectionListener headerListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selectedCol = table.indexOf((TableColumn) e.widget);
				if (selectedCol == sorter.getPriority()) {
					sorter.reversePriority();
				} else {
					sorter.setPriority(selectedCol);
				}
				viewer.refresh();
			}
		};
		for (int i = 0, lentgh = columnHeaders.length; i < lentgh; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setResizable(true);
			column.setText(columnHeaders[i]);
			layout.addColumnData(columnLayouts[i]);
			column.addSelectionListener(headerListener);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (sashForm != null) {
			sashForm.setFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		sashForm.dispose();
		disposed = true;
		super.dispose();
		DepclipsePlugin.getJDependData().removePropertyChangeListener(this);
	}

	public boolean isDisposed() {
		return disposed;
	}
	
	private static void collectCyclesForPackage(JavaPackage pack, List<JavaPackage> result) {
		if (DepclipsePlugin.getDefault().getPreferenceStore()
				.getBoolean(JDependPreferenceConstants.PREF_USE_ALL_CYCLES_SEARCH))  {
			pack.collectAllCycles(result);	
		} else {
			pack.collectCycle(result);
		}
	}

	private void updateUI(List<JavaPackage> selectedPackages,
			IResource[] selectedResources) {
		
		// create map for java packages -> faster doing this just once
		Map<String, JavaPackage> packMap = new HashMap<String, JavaPackage>();		
		for(JavaPackage pack : selectedPackages) {
			packMap.put(pack.getName(), pack);
		}

		List<JavaPackage> aff = new ArrayList<JavaPackage>();
		List<JavaPackage> eff = new ArrayList<JavaPackage>();
		List<JavaPackage> cl = new ArrayList<JavaPackage>();

		for(IResource resource : selectedResources) {
			TreeObject to = DepclipsePlugin.getJDependData().getFromResourceMap(resource);
			// if the selected resource is a TreeFolder <-> JavaPackage
			// otherwise it is a TreeLeaf <-> JavaClass
			if(to instanceof TreeFolder) {
				TreeFolder tf = (TreeFolder) to;
				eff.addAll(packMap.get(tf.getName()).getEfferents());
				aff.addAll(packMap.get(tf.getName()).getAfferents());
				collectCyclesForPackage(packMap.get(tf.getName()), cl);
			} else if (to instanceof TreeLeaf) {
				// TODO: CHECK IF THIS IS RIGHT
				TreeLeaf tl = (TreeLeaf) to;
				Set<JavaClass> clazzes = packMap.get(tl.getPackageName()).getClasses();
				Iterator<JavaClass> it = clazzes.iterator();
				while(it.hasNext()) {
					JavaClass thisClass = it.next();
					if(thisClass.getSourceFile().equals(tl.getName())) {
						eff.addAll(thisClass.getImportedPackages());
					}
				}
			}
			
		}
		
		removeDuplicate(aff);
		removeDuplicate(eff);
		removeDuplicate(cl);
		removeDuplicate(selectedPackages);

		affViewer.setInput(aff.toArray(new JavaPackage[0]));
		effViewer.setInput(eff.toArray(new JavaPackage[0]));
		cycleViewer.setInput(cl.toArray(new JavaPackage[0]));
		selViewer.setInput(selectedPackages.toArray(new JavaPackage[0]));
	}  
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void removeDuplicate(List arlList) {
		HashSet h = new HashSet(arlList);
		arlList.clear();
		arlList.addAll(h);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("SELECTION_CHANGED")) { //$NON-NLS-1$
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run() {
                    updateUI(DepclipsePlugin.getJDependData().getSelectedJavaPackages(), 
                    		DepclipsePlugin.getJDependData().getSelectedResources());
                }
            });			
		}	
	}
}