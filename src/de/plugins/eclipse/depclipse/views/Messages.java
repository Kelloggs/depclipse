/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.plugins.eclipse.depclipse.views.messages"; //$NON-NLS-1$
	public static String DependencyView_column_A;
	public static String DependencyView_column_AC;
	public static String DependencyView_column_Ca;
	public static String DependencyView_column_CC;
	public static String DependencyView_column_Ce;
	public static String DependencyView_column_Cycle;
	public static String DependencyView_column_D;
	public static String DependencyView_column_I;
	public static String DependencyView_column_Package;
	public static String DependencyView_Depends_Upon;
	public static String DependencyView_JDependAnalysisViewerToolTip;
	public static String DependencyView_Packages_with_Cycle;
	public static String DependencyView_Selected_Objects;
	public static String DependencyView_Used_By;
	public static String JDependConsole_Close_JDepend_console;
	public static String JDependConsole_JDepend_Console_Title;
	public static String JDependConsole_Toggle_XML_Text;
	public static String MetricsView_2;
	public static String MetricsView_3;
	public static String MetricsView_4;
	public static String PaintSurface_abstractness;
	public static String PaintSurface_Abstractness;
	public static String PaintSurface_Distance;
	public static String PaintSurface_instability;
	public static String PaintSurface_InstabilityGraphLabel;
	public static String ProhibitedDependencyView_Edit_Project_Rules;
	public static String ProhibitedDependencyView_Opens_the_Editor_for_Project_Rules;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
