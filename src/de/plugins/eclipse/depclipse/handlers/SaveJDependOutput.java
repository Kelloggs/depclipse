/*******************************************************************************
 * Copyright (c) 2010 Andrei Loskutov
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Andrei Loskutov - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.handlers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jdepend.framework.JavaPackage;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;

/**
 * Commandhandler saving the JDepend output to a file
 * 
 * @author Andrei Loskutov
 */
public class SaveJDependOutput extends AbstractHandler {

	private static final int CANCEL = -1;
	private static final int APPEND = 0;
	private static final int OVERRIDE = 1;
	private static String lastUsedFile;
	private IPreferenceStore prefs;
	
	@Override
	public boolean isEnabled() {
		return (DepclipsePlugin.getJDependData().getResources().length > 0);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		prefs = DepclipsePlugin.getDefault().getPreferenceStore();
		boolean shouldAsk = prefs
				.getBoolean(JDependPreferenceConstants.SAVE_TO_SHOW_OPTIONS);

		/*
		 * Show dialog if prefs is set, asking for open in editor
		 */
		boolean saveAsXml = prefs.getBoolean(JDependPreferenceConstants.SAVE_AS_XML);
		if (shouldAsk) {
			MessageDialogWithToggle dialogWithToggle = MessageDialogWithToggle
					.openYesNoCancelQuestion(
							getShell(),
							Messages.SaveJDependOutput_Save_JDepend_output,
							Messages.SaveJDependOutput_Save_JDepend_output_as_XML,
							Messages.SaveJDependOutput_Remember_and_do_not_ask_me_again, false, prefs,
							JDependPreferenceConstants.SAVE_TO_SHOW_OPTIONS);

			int returnCode = dialogWithToggle.getReturnCode();
			if (returnCode != IDialogConstants.YES_ID
					&& returnCode != IDialogConstants.NO_ID) {
				return null;
			}
			saveAsXml = returnCode == IDialogConstants.YES_ID;
			prefs.setValue(JDependPreferenceConstants.SAVE_AS_XML, saveAsXml);
		}

		/*
		 * open file selection dialog (external)
		 */
		File file = getFileFromUser(saveAsXml);
		if (file == null) {
			return null;
		}

		/*
		 * if selected file exists, ask for override/append/another file
		 */
		int overrideOrAppend = checkForExisting(file);
		if (overrideOrAppend == CANCEL) {
			return null;
		}

		IFile iFile = getWorkspaceFile(file);
		/*
		 * if selected file is in the workspace, checkout it or show error
		 * message
		 */
		if (iFile != null && !checkout(iFile, overrideOrAppend)) {
			return null;
		}

		/*
		 * save it
		 */
		doSave(file, overrideOrAppend);
		return null;
	}

	private static IFile getWorkspaceFile(File file) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile[] files = workspace.getRoot().findFilesForLocationURI(
				file.toURI());
		List<IFile> filesList = filterNonExistentFiles(files);
		if (filesList == null || filesList.size() != 1) {
			return null;
		}
		return filesList.get(0);
	}

	private static List<IFile> filterNonExistentFiles(IFile[] files) {
		if (files == null) {
			return null;
		}

		int length = files.length;
		ArrayList<IFile> existentFiles = new ArrayList<IFile>(length);
		for (int i = 0; i < length; i++) {
			if (files[i].exists()) {
				existentFiles.add(files[i]);
			}
		}
		return existentFiles;
	}

	private Shell getShell() {
		return DepclipsePlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell();
	}

	private void doSave(final File file, int overrideOrAppend) {

		final FileWriter fw;
		try {
			fw = new FileWriter(file, overrideOrAppend == APPEND);
		} catch (IOException e) {
			errorDialog(Messages.SaveJDependOutput_Couldnt_open_file_for_writing + file, e);
			return;
		}

		Job job = new Job(Messages.SaveJDependOutput_Save_JDepend_output_to_file) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					write(fw);
				} catch (Exception e) {
					errorDialog(Messages.SaveJDependOutput_Error_during_writing_to_file + file, e);
				} finally {
					try {
						fw.close();
					} catch (IOException e) {
						DepclipsePlugin.logError(e,
								"Couldn't close file: " + file); //$NON-NLS-1$
					}
				}
				return monitor.isCanceled() ? Status.CANCEL_STATUS
						: Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void errorDialog(String string, Exception e) {
		String message = e == null ? "" : e.getMessage(); //$NON-NLS-1$
		MessageDialog.openError(null, string, Messages.SaveJDependOutput_Error + message);
	}

	/**
	 * @param file
	 *            non null
	 * @param overrideOrAppend
	 * @return true if file doesn't exist and was created or writable
	 */
	private boolean checkout(IFile file, int overrideOrAppend) {
		if (file.getLocation() == null) {
			File file2 = new File(file.getFullPath().toOSString());
			if (!file2.exists()) {
				try {
					file2.createNewFile();
				} catch (IOException e) {
					errorDialog(Messages.SaveJDependOutput_Couldnt_create_file + file, e);
					return false;
				}
			}
			boolean canWrite = file2.canWrite();
			if (!canWrite) {
				errorDialog(Messages.SaveJDependOutput_File_is_Readonly + file, null);
			}
			return canWrite;
		}
		try {
			if (overrideOrAppend == APPEND && file.exists()) {
				file.appendContents(new ByteArrayInputStream(new byte[0]),
						true, true, new NullProgressMonitor());
			} else {
				if (file.exists()) {
					file.delete(true, new NullProgressMonitor());
				}
				file.create(new ByteArrayInputStream(new byte[0]), true,
						new NullProgressMonitor());
			}
		} catch (CoreException e) {
			errorDialog(Messages.SaveJDependOutput_File_is_Readonly + file, e);
			return false;
		}
		return true;
	}

	/**
	 * @param file
	 *            non null
	 * @return OVERRIDE if file not exists or exists and may be overriden,
	 *         APPEND if it exists and should be reused, CANCEL if action should
	 *         be cancelled
	 */
	private int checkForExisting(File file) {
		if (file.exists()) {
			MessageDialog md = new MessageDialog(getShell(),
					Messages.SaveJDependOutput_Warning_File_already_exists, null,
					Messages.SaveJDependOutput_Warning_File_already_exists, MessageDialog.WARNING,
					new String[] { Messages.SaveJDependOutput_Append, Messages.SaveJDependOutput_Override, Messages.SaveJDependOutput_Cancel }, 0);
			int result = md.open();
			switch (result) {
			case APPEND: // Append btn index
				return APPEND;
			case OVERRIDE: // Override btn index
				return OVERRIDE;
			default:
				return CANCEL;
			}
		}
		return OVERRIDE;
	}

	private File getFileFromUser(boolean asXml) {
		// without specifying style of FileDialog (SAVE), saving with GTK
		// WindowManager was not possible
		FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
		File returnFile = null;

		// set initial path of FileDialog
		if (lastUsedFile == null) {
			String property = System.getProperty("user.home"); //$NON-NLS-1$
			fd.setFilterPath(property);
		} else {
			fd.setFileName(lastUsedFile);
		}

		fd.setFilterExtensions(new String[] { asXml ? "*.xml" : "*.txt" }); //$NON-NLS-1$ //$NON-NLS-2$
		String fileStr = fd.open();
		if (fileStr != null) {
			if (new Path(fileStr).getFileExtension() == null) {
				fileStr += asXml ? ".xml" : ".txt"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			lastUsedFile = fileStr;
			returnFile = new File(fileStr);
		}
		return returnFile;
	}

	protected void write(FileWriter fw) {
		boolean asXml = prefs.getBoolean(JDependPreferenceConstants.SAVE_AS_XML);
		jdepend.textui.JDepend jdep;
		if (asXml) {
			jdep = new jdepend.xmlui.JDepend(new PrintWriter(fw)) {
				protected ArrayList<JavaPackage> getPackagesList() {
					return (ArrayList<JavaPackage>) DepclipsePlugin
							.getJDependData().getJavaPackages();
				}
			};
		} else {
			jdep = new jdepend.textui.JDepend(new PrintWriter(fw)) {
				protected ArrayList<JavaPackage> getPackagesList() {
					return (ArrayList<JavaPackage>) DepclipsePlugin
							.getJDependData().getJavaPackages();
				}
			};
		}
		jdep.analyze();
	}

}
