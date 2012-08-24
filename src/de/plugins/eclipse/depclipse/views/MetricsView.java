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
import java.util.List;

import jdepend.framework.JavaPackage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.plugins.eclipse.depclipse.DepclipsePlugin;

public class MetricsView extends ViewPart implements PropertyChangeListener {

	/** The view's identifier */
	public static final String ID = DepclipsePlugin.ID
			+ ".views.MetricsView"; //$NON-NLS-1$

	// paint surface for drawing
	protected PaintSurface paintSurface;
	protected ToolTipHandler tooltip;
	protected Canvas paintCanvas;
	private boolean disposed;

	/**
	 * The constructor.
	 */
	public MetricsView() {
		super();
	}

	/**
	 * Cleanup
	 */
	public void dispose() {
		paintCanvas.dispose();
		paintSurface.dispose();
		tooltip.dispose();
		disposed = true;
		super.dispose();
		DepclipsePlugin.getJDependData().removePropertyChangeListener(this);
	}

	public void setInput(JavaPackage[] newPackages) {
		redrawPackages(newPackages);
	}

	protected void redrawPackages(JavaPackage[] metrics) {
		if (paintSurface != null) {
			paintSurface.drawMetrics(metrics);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		paintCanvas = new Canvas(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		paintCanvas.setLayoutData(gridData);

		// paintSurface
		paintSurface = new PaintSurface(paintCanvas, parent.getDisplay()
				.getSystemColor(SWT.COLOR_WHITE));

		tooltip = new ToolTipHandler(parent.getShell());
		tooltip.activateHoverHelp(paintCanvas);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(paintCanvas,
				"de.plugins.eclipse.depclipse.jdepend"); //$NON-NLS-1$
		DepclipsePlugin.getJDependData().addPropertyChangeListener(this);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		paintSurface.setFocus();
	}

	public String getMetricInfo(int x, int y) {
		PaintSurface.Metric m = paintSurface.getMetric(x, y);
		return m == null ? null : m.toString();
	}

	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * Emulated tooltip handler Notice that we could display anything in a
	 * tooltip besides text and images. For instance, it might make sense to
	 * embed large tables of data or buttons linking data under inspection to
	 * material elsewhere, or perform dynamic lookup for creating tooltip text
	 * on the fly.
	 */
	protected class ToolTipHandler {
		protected Shell tipShell;

		protected Label tipLabelImage, tipLabelText;

		// private Widget tipWidget; // widget this tooltip is hovering over
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
					String text = getMetricInfo(event.x, event.y);
					if (text == null) {
						text = Messages.MetricsView_2
								+ Messages.MetricsView_3
								+ Messages.MetricsView_4;
					}

					tipPosition = control.toDisplay(pt);

					tipLabelText.setText(text);
					tipShell.pack();
					setHoverLocation(tipShell, tipPosition);
					tipShell.setVisible(true);
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
			shellBounds.y = Math.max(Math.min(position.y - 40 + 16,
					displayBounds.height - shellBounds.height), 0);
			shell.setBounds(shellBounds);
		}
	}

	/**
	 * ToolTip help handler
	 */
	protected interface ToolTipHelpTextHandler {
		/**
		 * Get help text
		 * 
		 * @param widget
		 *            the widget that is under help
		 * @return a help text string
		 */
		public String getHelpText(Widget widget);
	}

	protected void updateUI(List<JavaPackage> packages) {
		if (packages == null) {
			return;
		}
		setInput(packages.toArray(new JavaPackage[0]));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("SELECTION_CHANGED")) { //$NON-NLS-1$
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run() {
                    updateUI(DepclipsePlugin.getJDependData().getSelectedJavaPackages());
                }
            });			
		}	
		
	}

}