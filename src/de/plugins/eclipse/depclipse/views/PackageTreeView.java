/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;


import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.actions.PackageTreeSelectionChangedAction;
import de.plugins.eclipse.depclipse.model.TreeFolder;
import de.plugins.eclipse.depclipse.model.TreeLeaf;
import de.plugins.eclipse.depclipse.model.TreeObject;

public class PackageTreeView extends ViewPart implements IShowInSource,
		PropertyChangeListener {
	protected TreeViewer viewer;
	protected ViewContentProvider treeContent;
	protected ToolTipHandler tooltip;
	protected TreeSelectionListener treeSelectionHandler;

	/** The view's identifier */
	public static final String ID = DepclipsePlugin.ID
			+ ".views.PackageTreeView"; //$NON-NLS-1$

	static final class TreeSelectionListener implements ISelectionChangedListener {

		public void selectionChanged(SelectionChangedEvent event) {
			PackageTreeSelectionChangedAction action = new PackageTreeSelectionChangedAction();
			action.runWithEvent(event);
		}
	}

	final class ViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		public Object[] getElements(Object parent) {
			if (viewer.getInput() == parent) {
				return getChildren(viewer.getInput());
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeFolder) {
				return ((TreeFolder) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeFolder) {
				return ((TreeFolder) parent).hasChildren();
			}
			return false;
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@Override
		public void dispose() {			
		}
	}

	static final class ViewLabelProvider extends LabelProvider  {

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			String imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CLASS;
			
			if (obj instanceof TreeFolder) {
				TreeFolder tp = (TreeFolder) obj;
				ImageDescriptor[] overlays = new ImageDescriptor[5];
				if(tp.hasCycle()) {
					overlays[0] = DepclipsePlugin.getDefault().getImageDescriptor("cycle_overlay.png"); //$NON-NLS-1$
					
				} 
				if (tp.breaksDependencyRule()) {
					overlays[1] = DepclipsePlugin.getDefault().getImageDescriptor("prohibit_overlay.png"); //$NON-NLS-1$
				} 

				imageKey = org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE;
				
				DecorationOverlayIcon icon = new DecorationOverlayIcon(JavaUI.getSharedImages().getImage(imageKey),
						overlays);
				return icon.createImage();		
			}
			
			return JavaUI.getSharedImages().getImage(imageKey);
		}
	}

	/**
	 * Emulated tooltip handler Notice that we could display anything in a
	 * tooltip besides text and images.
	 */
	private final class ToolTipHandler {
		protected Shell tipShell;
		protected Label tipLabelImage, tipLabelText;
		protected Point tipPosition; // the position being hovered over

		/**
		 * Creates a new tooltip handler
		 * 
		 * @param parent
		 *            the parent Shell
		 */
		public ToolTipHandler(Shell parent) {
			final Display display = parent.getDisplay();

			tipShell = new Shell(parent, SWT.ON_TOP);
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 2;
			gridLayout.marginWidth = 2;
			gridLayout.marginHeight = 2;
			tipShell.setLayout(gridLayout);

			tipShell.setBackground(display
					.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

			tipLabelImage = new Label(tipShell, SWT.NONE);
			tipLabelImage.setForeground(display
					.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			tipLabelImage.setBackground(display
					.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			tipLabelImage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
					| GridData.VERTICAL_ALIGN_CENTER));

			tipLabelText = new Label(tipShell, SWT.NONE);
			tipLabelText.setForeground(display
					.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			tipLabelText.setBackground(display
					.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			tipLabelText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
					| GridData.VERTICAL_ALIGN_CENTER));
		}

		void dispose() {
			if (tipShell != null) {
				tipShell.dispose();
				tipShell = null;
			}
		}

		/**
		 * Enables customized hover help for a specified control
		 * 
		 * @control the control on which to enable hoverhelp
		 */
		public void activateHoverHelp(final Control control) {
			/*
			 * Get out of the way if we attempt to activate the control
			 * underneath the tooltip
			 */
			control.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					if (tipShell.isVisible())
						tipShell.setVisible(false);
				}
			});

			/*
			 * Trap hover events to pop-up tooltip
			 */
			control.addMouseTrackListener(new MouseTrackAdapter() {
				public void mouseExit(MouseEvent e) {
					if (tipShell.isVisible()) {
						tipShell.setVisible(false);
					}
				}

				public void mouseHover(MouseEvent event) {
					Point pt = new Point(event.x, event.y);

					TreeItem ti = viewer.getTree().getItem(pt);

					StringBuffer sb = new StringBuffer();
					getTreeText(sb, ti);

					if (sb.length() == 0) {
						tipShell.setVisible(false);
						return;
					}

					tipPosition = control.toDisplay(pt);

					tipLabelText.setText(sb.toString());
					tipShell.pack();
					setHoverLocation(tipShell, tipPosition);
					tipShell.setVisible(true);
				}

				private void getTreeText(StringBuffer sb, TreeItem ti) {
					if (ti == null || ti.getData() == null) {
						return;
					}
					sb.append(ti.getText());
					TreeObject to = (TreeObject) ti.getData();
					try {
						if (!to.isLeaf()) {
							ArrayList<String> paths = ((TreeFolder) to)
									.getClassesLocation();
							for (int i = 0; i < paths.size(); i++) {
								sb.append('\n').append(paths.get(i));
							}

						} else {

							sb.append('\n').append(
									((TreeLeaf) to).getByteCodePath());
						}
					} catch (JavaModelException e) {
						DepclipsePlugin.handle(e);
					}

				}
			});

		}

		/**
		 * Sets the location for a hovering shell
		 * 
		 * @param shell
		 *            the object that is to hover
		 * @param position
		 *            the position of a widget to hover over
		 */
		protected void setHoverLocation(Shell shell, Point position) {
			Rectangle displayBounds = shell.getDisplay().getBounds();
			Rectangle shellBounds = shell.getBounds();
			shellBounds.x = Math.max(Math.min(position.x + 8,
					displayBounds.width - shellBounds.width), 0);
			shellBounds.y = Math.max(Math.min(position.y + 16,
					displayBounds.height - shellBounds.height), 0);
			shell.setBounds(shellBounds);
		}
	}

	/**
	 * The constructor.
	 */
	public PackageTreeView() {
		super();
	}

	public void updateUI(TreeFolder newRoot) {
		viewer.setInput(newRoot);
		// actualize tree
		// should select elements
		viewer.setSelection(new StructuredSelection(((TreeFolder) viewer.getInput()).getChildren()),
				true);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		tooltip = new ToolTipHandler(parent.getShell());
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeContent = new ViewContentProvider();
		viewer.setContentProvider(treeContent);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setInput(new TreeFolder());
		treeSelectionHandler = new TreeSelectionListener();
		viewer.addSelectionChangedListener(treeSelectionHandler);
		hookContextMenu();
		hookDoubleClickAction();
		tooltip.activateHoverHelp(viewer.getTree());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
				"de.plugins.eclipse.depclipse.jdepend"); //$NON-NLS-1$
		getSite().setSelectionProvider(viewer);
		DepclipsePlugin.getJDependData().addPropertyChangeListener(this);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator("additions")); //$NON-NLS-1$
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IHandlerService hdlService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					hdlService.executeCommand("de.plugins.eclipse.depclipse.handlers.OpenInJavaPerspective", null); //$NON-NLS-1$
				} catch (ExecutionException e) {
					DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
				} catch (NotDefinedException e) {
					DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
				} catch (NotEnabledException e) {
					DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
				} catch (NotHandledException e) {
					DepclipsePlugin.logError(e, ""); //$NON-NLS-1$
				} 
			}
		});
	}

	public void dispose() {
		getSite().setSelectionProvider(null);
		tooltip.dispose();
		viewer.getTree().dispose();
		viewer = null;
		super.dispose();
		DepclipsePlugin.getJDependData().removePropertyChangeListener(this);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public ShowInContext getShowInContext() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.size() != 1) {
			return null;
		}
		TreeObject firstElement = (TreeObject) selection.getFirstElement();
		if (firstElement.getIResource() != null) {
			return new ShowInContext(null, new StructuredSelection(firstElement
					.getIResource()));
		}
		return null;
	}

	// XXX: not sure how to avoid this warning, as this is from IAdaptable
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(ISelectionProvider.class))
			return treeSelectionHandler;
		if (adapter == IShowInSource.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("DATA_CHANGED")) { //$NON-NLS-1$
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				public void run() {
					updateUI(DepclipsePlugin.getJDependData().getRoot());
				}
			});
		}
	}
}