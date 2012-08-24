/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.handlers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "de.plugins.eclipse.depclipse.handlers.messages"; //$NON-NLS-1$
	public static String OpenJDependConsole_JDepend_text_output_processing;
	public static String RefreshJDepend_JDepend_analysis;
	public static String RemoveOrphans_Remove_all_orphaned_Dependencies;
	public static String RemoveOrphans_Remove_Orphaned_Dialog_Text;
	public static String SaveJDependOutput_Error;
	public static String SaveJDependOutput_Append;
	public static String SaveJDependOutput_Cancel;
	public static String SaveJDependOutput_Couldnt_create_file;
	public static String SaveJDependOutput_Couldnt_open_file_for_writing;
	public static String SaveJDependOutput_Error_during_writing_to_file;
	public static String SaveJDependOutput_File_is_Readonly;
	public static String SaveJDependOutput_Override;
	public static String SaveJDependOutput_Remember_and_do_not_ask_me_again;
	public static String SaveJDependOutput_Save_JDepend_output;
	public static String SaveJDependOutput_Save_JDepend_output_as_XML;
	public static String SaveJDependOutput_Save_JDepend_output_to_file;
	public static String SaveJDependOutput_Warning_File_already_exists;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
