/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.plugins.eclipse.depclipse.preferences.messages"; //$NON-NLS-1$
	public static String JDependPreferencePage_Add_filter;
	public static String JDependPreferencePage_Add_package;
	public static String JDependPreferencePage_Ask_before_save;
	public static String JDependPreferencePage_Choose_a_package_and_add_it_to_package_filters;
	public static String JDependPreferencePage_Default_is_true;
	public static String JDependPreferencePage_Defined_package_filters;
	public static String JDependPreferencePage_Disable_all;
	public static String JDependPreferencePage_DisableAllToolTip;
	public static String JDependPreferencePage_Enable_all;
	public static String JDependPreferencePage_EnableAllToolTip;
	public static String JDependPreferencePage_EnableDisable_package;
	public static String JDependPreferencePage_ForbiddenDependenciesToolTip;
	public static String JDependPreferencePage_Invalid_package_filter_Return_to_continue;
	public static String JDependPreferencePage_Key_in_the_name_of_a_new_package_filter;
	public static String JDependPreferencePage_Options_for_JDepend;
	public static String JDependPreferencePage_Remove;
	public static String JDependPreferencePage_Remove_all_selected_package_filters;
	public static String JDependPreferencePage_Save_as_XML;
	public static String JDependPreferencePage_Save_as_XML_ToolTip;
	public static String JDependPreferencePage_Search_project_for_prohibited_dependencies;
	public static String JDependPreferencePage_Searching_Progress_Monitor;
	public static String JDependPreferencePage_Select_a_package_to_filter_for_JDepend;
	public static String JDependPreferencePage_Set_Filename_for_Prohbibited_Dependencies;
	public static String JDependPreferencePage_Use_more_comprehensive_cycles_search;
	public static String JDependPreferencePage_Use_package_filters;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
