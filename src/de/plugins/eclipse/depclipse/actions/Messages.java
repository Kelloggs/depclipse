/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.plugins.eclipse.depclipse.actions.messages"; //$NON-NLS-1$
	public static String SaveProjectRulesAction_Error_saving_File;
	public static String SaveProjectRulesAction_Please_check_your_permissions_to_write_this_file;
	public static String ShowDependencyAction_JDepend_anaylsis;
	public static String ShowDependencyAction_JDR_File_Error_Message;
	public static String ToggleJDependOutputAction_JDepend_text_output_processing;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
