/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.testcommons;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;
import de.plugins.eclipse.depclipse.views.PackageTreeView;

public class JDepend4EclipseTest {

	protected IProject project;
	protected static final String PROJECT_NAME = "myTestProject";
	protected static final String VIEW_ID = "de.plugins.eclipse.depclipse.views.PackageTreeView";
	protected PackageTreeView view;
	protected IWorkbench workbench = PlatformUI.getWorkbench();
	public static TestingEnvironment env;

	public JDepend4EclipseTest() {
		super();
	}

	@BeforeClass
	public static void setUpClass() throws CoreException {
		env = new TestingEnvironment();
		env.openEmptyWorkspace();
		env.resetWorkspace();

		IPath projectPath = env.addProject(PROJECT_NAME);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		// set up class hierarchy
		env.addClass(root, "a", "First", textFirstAJava);
		env.addClass(root, "b", "SecondB", textSecondBJava);
		env.addClass(root, "b2", "SecondB2", textSecondB2Java);
		env.addClass(root, "c", "Third", textThirdCJava);
		env.fullBuild();
	}

	@AfterClass
	public static void tearDownClass() throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		root.delete(true, null);
	}

	@Before
	public void setUpTest() throws CoreException {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				PROJECT_NAME);
		view = (PackageTreeView) getViewPart();
	}

	private IViewPart getViewPart() throws CoreException {
		IViewPart showView = workbench.getActiveWorkbenchWindow()
				.getActivePage().showView(VIEW_ID);
		return showView;
	}

	@After
	public void tearDownTest() {
		// set filters in preferenceStore back to defaults
		DepclipsePlugin.getDefault().getPreferenceStore().setValue(
				JDependPreferenceConstants.PREF_ACTIVE_FILTERS_LIST,
				DepclipsePlugin.getDefault().getPreferenceStore()
						.getDefaultString(
								JDependPreferenceConstants.PREF_ACTIVE_FILTERS_LIST));
	}

	private static final String textFirstAJava = "package a; \n\n"
			+ "import java.util.List;\n\n" + "public class First {}";
	private static final String textSecondBJava = "package b; \n\n"
			+ "import a.First;\n\n" + "public class SecondB { \n"
			+ "public void needFirst(First c){}\n" + "}";
	private static final String textSecondB2Java = "package b2;\n\n"
			+ "import a.First;\n" + "import b.SecondB;\n\n" 
			+ "public class SecondB2 {\n"
			+ "public void needFirst(First c){}\n" 
			+ "public void needSecondB(SecondB c){}\n" + "}";
	private static final String textThirdCJava = "package c;\n\n"
			+ "import b.SecondB;\n\n" + "public class Third { \n"
			+ "public void needSecondB(SecondB c){}\n " + "}";
}