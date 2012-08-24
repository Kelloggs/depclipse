/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.EditorPart;
import org.xml.sax.SAXParseException;

import com.thoughtworks.xstream.converters.ConversionException;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.actions.SaveProjectRulesAction;
import de.plugins.eclipse.depclipse.rules.AbstractPackageRule;
import de.plugins.eclipse.depclipse.rules.AllowedPackageRule;
import de.plugins.eclipse.depclipse.rules.ProhibitedPackageRule;

/**
 * Editor for showing and handling the XML-JDR format.
 * 
 * @author Jens Cornelis
 */
public class RuleEditor extends EditorPart {
	
	private String[] columnNames = { Messages.RuleEditor_UnconfirmedDependency, Messages.RuleEditor_ObsoleteDependency, Messages.RuleEditor_Column_RootPackage, Messages.RuleEditor_Column_DependsUpon }; //$NON-NLS-1$ //$NON-NLS-2$
	private boolean dirty;
	private IFile file;
	private SashForm sashForm;
	private Table proDepTable;
	private Table allDepTable;
	private TableViewer proDepViewer;
	private SimpleViewerSorter proDepSorter;
	private TableViewer allDepViewer;
	private SimpleViewerSorter allDepSorter;
	private Button allowButton;
	private Button prohibitButton;
	private Button confirmSelectedButton;
	private Button removeOrphans;

	private int max(int[] t) {
	    int maximum = t[0]; 
	    for (int i=1; i<t.length; i++) {
	        if (t[i] > maximum) {
	            maximum = t[i];  
	        }
	    }
	    return maximum;
	}
	
	private int min(int[] t) {
	    int minimum = t[0]; 
	    for (int i=1; i<t.length; i++) {
	        if (t[i] < minimum) {
	        	minimum = t[i];  
	        }
	    }
	    return minimum;
	}
	
	private static final class SimpleViewerSorter extends ViewerSorter{

		private boolean reverse;
		private int column;

		public int getPriority() {
			return column;
		}

		public void reversePriority() {
			reverse = !reverse;
		}

		public void setPriority(int column) {
			this.column = column;
		}

		public int compare(Viewer viewer, Object e1, Object e2) {
			if(e1 instanceof String[] && e2 instanceof String[]) {
				String[] strings1 = (String[]) e1;
				String[] strings2 = (String[]) e2;
				
				int result = strings1[column].compareTo(strings2[column]);
				

				if (reverse) {
					result = -result;
				}
				return result;
			}
			return super.compare(viewer, e1, e2);
		}
	}

	private final class OrphanRemovalListener implements Listener {
	
		@Override
		public void handleEvent(Event event) {
			invokeCommand();
	
		}
		private void invokeCommand() {			
			IHandlerService hdlService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				hdlService.executeCommand("de.plugins.eclipse.depclipse.handlers.RemoveOrphans", null); //$NON-NLS-1$
			} catch (ExecutionException e1) {
				DepclipsePlugin.logError(e1, ""); //$NON-NLS-1$
			} catch (NotDefinedException e1) {
				DepclipsePlugin.logError(e1, ""); //$NON-NLS-1$
			} catch (NotEnabledException e1) {
				DepclipsePlugin.logError(e1, ""); //$NON-NLS-1$
			} catch (NotHandledException e1) {
				DepclipsePlugin.logError(e1, ""); //$NON-NLS-1$
			}
			dirty = true;
			firePropertyChange(PROP_DIRTY);
		}
	}

	private final class ConfirmationListener implements Listener {
		
		@Override
		public void handleEvent(Event event) {
			invokeCommand();
		}		
	
		private void invokeCommand() {			
			int index = max(proDepViewer.getTable().getSelectionIndices());
			
			ICommandService cmdService = (ICommandService) getSite()
					.getService(ICommandService.class);
			Command cmd = cmdService
					.getCommand("de.plugins.eclipse.depclipse.handlers.ConfirmDependency"); //$NON-NLS-1$
			try {
				Map<String, ISelection> params = new HashMap<String, ISelection>();
				
				params.put("de.plugins.eclipse.depclipse.handlers.ConfirmDependency.Selection", proDepViewer.getSelection()); //$NON-NLS-1$
				ExecutionEvent execEvent = new ExecutionEvent(cmd, params,
						null, null);
				
				cmd.executeWithChecks(execEvent);
			} catch (ExecutionException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotHandledException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotDefinedException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotEnabledException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			}
			
			if(!(proDepViewer.getTable().getItemCount() == index + 1)){
				proDepViewer.getTable().select(index + 1);
				proDepViewer.getTable().showItem(proDepViewer.getTable().getItem(index + 1));
			} else {
				proDepViewer.getTable().select(index);
				proDepViewer.getTable().showItem(proDepViewer.getTable().getItem(index));
			}
			proDepViewer.getTable().forceFocus();
			dirty = true;
			firePropertyChange(PROP_DIRTY);
		}
	}

	private final class AllowListener implements Listener, IDoubleClickListener {
		@Override
		public void handleEvent(Event event) {
			invokeCommand();
		}

		@Override
		public void doubleClick(DoubleClickEvent event) {
			invokeCommand();
		}

		private void invokeCommand() {
			int index = min(proDepViewer.getTable().getSelectionIndices());
			
			ICommandService cmdService = (ICommandService) getSite()
					.getService(ICommandService.class);
			Command cmd = cmdService
					.getCommand("de.plugins.eclipse.depclipse.handlers.AllowDependency"); //$NON-NLS-1$
			try {
				Map<String, ISelection> params = new HashMap<String, ISelection>();
				params.put("de.plugins.eclipse.depclipse.handlers.AllowDependency.Selection", //$NON-NLS-1$
								proDepViewer.getSelection());
				ExecutionEvent execEvent = new ExecutionEvent(cmd, params,
						null, null);
				cmd.executeWithChecks(execEvent);
			} catch (ExecutionException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotHandledException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotDefinedException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotEnabledException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			}
			
			// check if table is empty - if not select minimal index after handler
			if(proDepViewer.getTable().getItemCount() == 0) {
				// do nothing
			} else if(!(proDepViewer.getTable().getItemCount() == index)) {
				proDepViewer.getTable().setSelection(index);
				proDepViewer.getTable().showItem(proDepViewer.getTable().getItem(index)); 
			} else {
				proDepViewer.getTable().setSelection(index - 1);
				proDepViewer.getTable().showItem(proDepViewer.getTable().getItem(index - 1)); 
			}
			
			proDepViewer.getTable().forceFocus();
			dirty = true;
			firePropertyChange(PROP_DIRTY);
		}
	}

	private final class ProhibitListener implements Listener,
			IDoubleClickListener {
		@Override
		public void handleEvent(Event event) {
			invokeCommand();
		}

		@Override
		public void doubleClick(DoubleClickEvent event) {
			invokeCommand();
		}

		private void invokeCommand() {
			int index = min(allDepViewer.getTable().getSelectionIndices());
			ICommandService cmdService = (ICommandService) getSite()
					.getService(ICommandService.class);
			Command cmd = cmdService
					.getCommand("de.plugins.eclipse.depclipse.handlers.ProhibitDependency"); //$NON-NLS-1$
			try {
				Map<String, ISelection> params = new HashMap<String, ISelection>();
				params
						.put(
								"de.plugins.eclipse.depclipse.handlers.ProhibitDependency.Selection", //$NON-NLS-1$
								allDepViewer.getSelection());
				ExecutionEvent execEvent = new ExecutionEvent(cmd, params,
						null, null);
				cmd.executeWithChecks(execEvent);
			} catch (ExecutionException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotHandledException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotDefinedException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (NotEnabledException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			}
			
			// check if table is empty - if not select minimal index after handler
			if(allDepViewer.getTable().getItemCount() == 0) {
				// do nothing
			} else if(!(allDepViewer.getTable().getItemCount() == index)) {
				allDepViewer.getTable().setSelection(index);
				allDepViewer.getTable().showItem(allDepViewer.getTable().getItem(index)); 
			} else {
				allDepViewer.getTable().setSelection(index - 1);
				allDepViewer.getTable().showItem(allDepViewer.getTable().getItem(index - 1)); 
			}
			allDepViewer.getTable().forceFocus();
			dirty = true;
			firePropertyChange(PROP_DIRTY);
		}
	}

	static class ArrayContentProvider implements IStructuredContentProvider,
			PropertyChangeListener {

		String[][] data;
		Viewer v;
		String relatedProperty;

		public static final String ID = DepclipsePlugin.ID
				+ ".editors.RuleEditor"; //$NON-NLS-1$

		public ArrayContentProvider(String relatedProperty) {
			this.relatedProperty = relatedProperty;
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (newInput != null && newInput instanceof Map<?, ?>) {
				Map<?, ?> ruleMap = (Map<?, ?>) newInput;
				List<String[]> tmpList = new ArrayList<String[]>();
				for (Object obj : ruleMap.values()) {
					AbstractPackageRule rule = (AbstractPackageRule) obj;
					for (String efferent : rule.getEfferentPackages()) {
						String[] tmp = new String[4];
						if (rule instanceof ProhibitedPackageRule) {
							tmp[0] = String
									.valueOf(((ProhibitedPackageRule) rule)
											.isConfirmed(efferent));
							tmp[1] = String.valueOf(rule.isOrphaned(efferent));
							tmp[2] = rule.getRootPackage();
							tmp[3] = efferent;
							tmpList.add(tmp);
						} else if (rule instanceof AllowedPackageRule) {
							tmp[0] = String.valueOf(rule.isOrphaned(efferent));
							tmp[1] = rule.getRootPackage();
							tmp[2] = efferent;
							tmpList.add(tmp);
						}
					}
				}
				data = tmpList.toArray(new String[0][]);
				this.v = v;
				v.refresh();

			}
			if (oldInput == null) {
				DepclipsePlugin.getJDependData()
						.addPropertyChangeListener(this);
			}
		}

		public void dispose() {
			DepclipsePlugin.getJDependData()
					.removePropertyChangeListener(this);
		}

		public Object[] getElements(Object parent) {
			if (data == null) {
				return new Object[0];
			} else
				return data;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(relatedProperty)) {
				inputChanged(v, data, evt.getNewValue());
			}

		}
	}

	static class AllowedTableLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			switch (index) {
			case 0:
				return ""; //$NON-NLS-1$
			case 1:
			case 2:
				if (obj instanceof String[]) {
					String[] arr = (String[]) obj;
					return arr[index];
				}
			default:
				return getText(obj);
			}
		}

		public Image getColumnImage(Object obj, int index) {
			boolean boolVal = false;
			if (obj instanceof String[]) {
				String tmp = ((String[]) obj)[index];
				if (tmp.equalsIgnoreCase("true") //$NON-NLS-1$
						|| tmp.equalsIgnoreCase("false")) { //$NON-NLS-1$
					boolVal = Boolean.valueOf(tmp);
				}
			}
			switch (index) {
			case 0:
				if (boolVal) {
					return DepclipsePlugin.getDefault()
							.getImageDescriptor("trash.png") //$NON-NLS-1$
							.createImage();
				} else {
					return DepclipsePlugin.getDefault()
					.getImageDescriptor("allow.png") //$NON-NLS-1$
					.createImage();
				}
			default:
				String imageKey;
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE;
				return JavaUI.getSharedImages().getImage(imageKey);
			}
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	static class ProhibitedTableLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			switch (index) {
			case 0:
			case 1:
				return ""; //$NON-NLS-1$
			case 2:
			case 3:
				if (obj instanceof String[]) {
					String[] arr = (String[]) obj;
					return arr[index];
				}
			default:
				return getText(obj);
			}
		}

		public Image getColumnImage(Object obj, int index) {
			boolean boolVal = false;
			if (obj instanceof String[]) {
				String tmp = ((String[]) obj)[index];
				if (tmp.equalsIgnoreCase("true") //$NON-NLS-1$
						|| tmp.equalsIgnoreCase("false")) { //$NON-NLS-1$
					boolVal = Boolean.valueOf(tmp);
				}
			}
			switch (index) {
			case 0:
				if (!boolVal) {
					return DepclipsePlugin.getDefault()
							.getImageDescriptor("unconfirmed.gif") //$NON-NLS-1$
							.createImage();
				} else {
					return null;
				}
			case 1:
				if (boolVal) {
					return DepclipsePlugin.getDefault()
							.getImageDescriptor("trash.png") //$NON-NLS-1$
							.createImage();
				} else {
					return DepclipsePlugin.getDefault()
					.getImageDescriptor("prohibit.png") //$NON-NLS-1$
					.createImage();
				}
			default:
				String imageKey;
				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE;
				return JavaUI.getSharedImages().getImage(imageKey);
			}
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	
	@Override
	public void doSave(IProgressMonitor monitor) {
		SaveProjectRulesAction saveAction = new SaveProjectRulesAction(((IFileEditorInput) getEditorInput())
						.getFile());
		saveAction.run();
		dirty = false;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void doSaveAs() {
		// no implementation here as SaveAs is not allowed
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (input instanceof IFileEditorInput) {
			file = ((IFileEditorInput) input).getFile();
			try {
				DepclipsePlugin.getProjectRules(file.getProject());
			} catch (IOException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (CoreException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (SAXParseException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
			} catch (ConversionException e) {
				DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
				ErrorDialog.openError(null, 
						Messages.RuleEditor_JDR_File_Error_Label, null, 
						new Status(IStatus.ERROR, DepclipsePlugin.ID, 1, 
								Messages.RuleEditor_JDR_File_could_not_be_read_message, 
								e));
			}					
			setPartName(input.getName());
			setSite(site);
			setInput(input);
		}
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Group globalGroup = new Group(parent, SWT.VERTICAL);
		globalGroup.setLayout(new GridLayout(1, false));
		globalGroup.setLayoutData(new GridData());
		sashForm = new SashForm(globalGroup, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(sashForm,
				"de.plugins.eclipse.depclipse.jdepend"); //$NON-NLS-1$

		Group proDepGroup = new Group(sashForm, SWT.VERTICAL);
		proDepGroup.setText(Messages.RuleEditor_group_Prohibited_Dependencies);
		proDepGroup.setLayout(new GridLayout(1, false));
		proDepTable = createProhibitedTable(proDepGroup);

		proDepViewer = new TableViewer(proDepTable);
		proDepViewer.setUseHashlookup(true);
		proDepViewer.setContentProvider(new ArrayContentProvider(
				"prohibitedDependencies")); //$NON-NLS-1$
		proDepViewer.setLabelProvider(new ProhibitedTableLabelProvider());
		proDepSorter = new SimpleViewerSorter();
		proDepSorter.setPriority(2);
		proDepViewer.setSorter(proDepSorter);
		proDepViewer.setInput(DepclipsePlugin.getJDependData()
				.getProjectRules().getProhibitedPackageRules());

		SashForm proButtonForm = new SashForm(proDepGroup, SWT.HORIZONTAL);
		proButtonForm.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		confirmSelectedButton = new Button(proButtonForm, SWT.PUSH);
		confirmSelectedButton.setImage(DepclipsePlugin.getDefault()
				.getImageDescriptor("unconfirmed.gif").createImage()); //$NON-NLS-1$
		confirmSelectedButton.addListener(SWT.Selection,
				new ConfirmationListener());
		confirmSelectedButton.setText(Messages.RuleEditor_Mark_Confirmed);
		
		allowButton = new Button(proButtonForm, SWT.PUSH);
		allowButton.setText(Messages.RuleEditor_Allow);
		allowButton.addListener(SWT.Selection, new AllowListener());
		allowButton.setImage(DepclipsePlugin.getDefault()
				.getImageDescriptor("allow.png").createImage()); //$NON-NLS-1$

		Group allowDepGroup = new Group(sashForm, SWT.VERTICAL);
		allowDepGroup.setText(Messages.RuleEditor_group_Allowed_Dependencies);
		allowDepGroup.setLayout(new GridLayout(1, false));
		allDepTable = createAllowedTable(allowDepGroup);
		allDepViewer = new TableViewer(allDepTable);
		allDepViewer.setUseHashlookup(true);
		allDepViewer.setLabelProvider(new AllowedTableLabelProvider());
		allDepSorter = new SimpleViewerSorter();
		allDepSorter.setPriority(1);
		allDepViewer.setSorter(allDepSorter);
		allDepViewer.setContentProvider(new ArrayContentProvider(
				"allowedDependencies")); //$NON-NLS-1$
		allDepViewer.setInput(DepclipsePlugin.getJDependData()
				.getProjectRules().getAllowedPackageRules());

		SashForm allButtonForm = new SashForm(allowDepGroup, SWT.HORIZONTAL);
		allButtonForm.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		prohibitButton = new Button(allButtonForm, SWT.PUSH);
		prohibitButton.setText(Messages.RuleEditor_Prohibit);
		prohibitButton.addListener(SWT.Selection, new ProhibitListener());
		prohibitButton.setImage(DepclipsePlugin.getDefault()
				.getImageDescriptor("prohibit.png").createImage()); //$NON-NLS-1$

		removeOrphans = new Button(globalGroup, SWT.PUSH);
		removeOrphans.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1,1));
		removeOrphans.setText(Messages.RuleEditor_Remove_all_orphaned_Dependencies);
		removeOrphans.setImage(DepclipsePlugin.getDefault()
				.getImageDescriptor("trash.png").createImage()); //$NON-NLS-1$
		removeOrphans.addListener(SWT.Selection, new OrphanRemovalListener());
		hookContextMenu();
		hookDoubleClickAction();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RuleEditor.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(allDepViewer.getControl());
		allDepViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, allDepViewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

	}

	protected Table createAllowedTable(Composite mySashForm) {
		int style = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		final Table table = new Table(mySashForm, style);
		
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 3;	
		
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);	

		SelectionListener headerListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selectedCol = table.indexOf((TableColumn) e.widget);
				if (selectedCol == allDepSorter.getPriority()) {
					allDepSorter.reversePriority();
				} else {
					allDepSorter.setPriority(selectedCol);
				}
				allDepViewer.refresh();
			}
		};
		TableColumn orphDepColumn = new TableColumn(table, SWT.LEFT, 0);
		orphDepColumn.setText(columnNames[1]);
		orphDepColumn.pack();
		orphDepColumn.setToolTipText(Messages.RuleEditor_Orphaned_Icon_Description);
		orphDepColumn.addSelectionListener(headerListener);

		TableColumn rootColumn = new TableColumn(table, SWT.LEFT, 1);
		rootColumn.setText(columnNames[2]);
		rootColumn.setWidth(200);
		rootColumn.addSelectionListener(headerListener);

		TableColumn depColumn = new TableColumn(table, SWT.LEFT, 2);
		depColumn.setText(columnNames[3]);
		depColumn.setWidth(200);
		depColumn.addSelectionListener(headerListener);
		
		return table;
	}

	protected Table createProhibitedTable(Composite mySashForm) {
		int style = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		final Table table = new Table(mySashForm, style);

		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 4;

		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		

		SelectionListener headerListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selectedCol = table.indexOf((TableColumn) e.widget);
				if (selectedCol == proDepSorter.getPriority()) {
					proDepSorter.reversePriority();
				} else {
					proDepSorter.setPriority(selectedCol);
				}
				proDepViewer.refresh();
			}
		};

		TableColumn confDepColumn = new TableColumn(table, SWT.LEFT, 0);
		confDepColumn.setText(columnNames[0]);
		confDepColumn.pack();
		confDepColumn
				.setToolTipText(Messages.RuleEditor_Confirmation_Icon_Description);
		confDepColumn.addSelectionListener(headerListener);

		TableColumn orphDepColumn = new TableColumn(table, SWT.LEFT, 1);
		orphDepColumn.setText(columnNames[1]);
		orphDepColumn.pack();
		orphDepColumn
				.setToolTipText(Messages.RuleEditor_Orphaned_Icon_Description);
		orphDepColumn.addSelectionListener(headerListener);

		TableColumn rootColumn = new TableColumn(table, SWT.LEFT, 2);
		rootColumn.setText(columnNames[2]);
		rootColumn.setWidth(200);
		rootColumn.addSelectionListener(headerListener);

		TableColumn depColumn = new TableColumn(table, SWT.LEFT, 3);
		depColumn.setText(columnNames[3]);
		depColumn.setWidth(200);
		depColumn.addSelectionListener(headerListener);

		return table;
	}

	private void hookDoubleClickAction() {
		proDepViewer.addDoubleClickListener(new AllowListener());
		allDepViewer.addDoubleClickListener(new ProhibitListener());
	}

	@Override
	public void setFocus() {

	}
}
