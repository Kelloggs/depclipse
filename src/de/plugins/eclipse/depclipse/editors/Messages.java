/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.editors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.plugins.eclipse.depclipse.editors.messages"; //$NON-NLS-1$
	public static String RuleEditor_UnconfirmedDependency;
	public static String RuleEditor_ObsoleteDependency;
	public static String RuleEditor_Allow;
	public static String RuleEditor_Column_DependsUpon;
	public static String RuleEditor_Column_RootPackage;
	public static String RuleEditor_Confirmation_Icon_Description;
	public static String RuleEditor_group_Allowed_Dependencies;
	public static String RuleEditor_group_Prohibited_Dependencies;
	public static String RuleEditor_JDR_File_could_not_be_read_message;
	public static String RuleEditor_JDR_File_Error_Label;
	public static String RuleEditor_Mark_Confirmed;
	public static String RuleEditor_Orphaned_Icon_Description;
	public static String RuleEditor_Prohibit;
	public static String RuleEditor_Remove_all_orphaned_Dependencies;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
