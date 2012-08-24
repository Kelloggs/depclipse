/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.model.TreeObject;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;
import de.plugins.eclipse.depclipse.preferences.JDependPreferencePage;
import de.plugins.eclipse.depclipse.rules.IRuleParser;
import de.plugins.eclipse.depclipse.rules.ProjectRules;
import de.plugins.eclipse.depclipse.testcommons.JDepend4EclipseTest;

public class JDependTest extends JDepend4EclipseTest {
	private static IRuleParser mock;
	
	@BeforeClass
	public static void mockSetUp() throws FileNotFoundException, IOException, CoreException, IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		rules.addAllowedPackageRule("c");
		rules.getAllowedPackageRule("c").addEfferentPackages(Arrays.asList("a", "b2"));
		
		mock = createMock(IRuleParser.class);
		expect(mock.getProjectRules(isA(IFile.class))).andReturn(rules).atLeastOnce();
		mock.setProjectRules(isA(IFile.class), isA(ProjectRules.class));
		expectLastCall().atLeastOnce();
		replay(mock);
		DepclipsePlugin.setProjectRuleParser(mock);	
	}

	@AfterClass
	public static void mockTearDown() {
		verify(mock);
		DepclipsePlugin.setProjectRuleParser(null);
	}

	@Test
	public void analyzeNumberOfPackages() throws PartInitException {
		int expectedListLength = 4; // 4 packages in test project

		IResource[] inputResources = new IResource[1];
		inputResources[0] = project.getFolder("/src");

		try {
			DepclipsePlugin.getJDependAdapter().startAnalyze(DepclipsePlugin.getJDependData(), inputResources);
			assertEquals("Project contains 4 Packages", expectedListLength,	
					DepclipsePlugin.getJDependData().getJavaPackages().size());
		} catch (Exception e) {
			fail("An error occurred!");
		}

	}

	@Test
	public void testDefaultFilters() throws PartInitException {
		int filterfaults = 0;

		IResource[] inputResources = new IResource[1];
		inputResources[0] = project.getFolder("/src");

		try {
			DepclipsePlugin.getJDependAdapter().startAnalyze(DepclipsePlugin.getJDependData(),inputResources);
			// check if there are packages in result, which were supposed to be
			// filtered
			for (JavaPackage tmp : DepclipsePlugin.getJDependData().getJavaPackages()) {
				if (!DepclipsePlugin.getJDependData().getFilter().accept(
						tmp.getName())) {
					filterfaults++;
				}
			}

			assertEquals(
					"There were packages in result, which were expected to be filtered.",
					0, filterfaults);
		} catch (Exception e) {
			fail("An error occurred!");
		}
	}

	@Test
	public void testUserDefinedFilters() throws PartInitException {
		int filterfaults = 0;
		int expectedListLength = 3;

		IResource[] inputResources = new IResource[1];
		inputResources[0] = project.getFolder("/src");

		// add package a to filter and safe in preferenceStore
		DepclipsePlugin.getJDependData().addPackageToFilter("a");
		PackageFilter myFilter = DepclipsePlugin.getJDependData()
				.getFilter();
		DepclipsePlugin.getDefault().getPreferenceStore().setValue(
				JDependPreferenceConstants.PREF_ACTIVE_FILTERS_LIST,
				JDependPreferencePage.serializeList(myFilter.getFilters()
						.toArray(new String[0])));

		try {
			DepclipsePlugin.getJDependAdapter().startAnalyze(DepclipsePlugin.getJDependData(),inputResources);
			// check if there are packages in result, which were supposed to be
			// filtered
			for (JavaPackage tmp : DepclipsePlugin.getJDependData().getJavaPackages()) {
				if (!myFilter.accept(tmp.getName())) {
					filterfaults++;
				}
			}
			assertEquals(
					"There were packages in result, which were expected to be filtered.",
					0, filterfaults);
			assertEquals("The filtered packages were expected to count 3",
					expectedListLength, DepclipsePlugin.getJDependData().getJavaPackages().size());
		} catch (Exception e) {
			fail("An error occurred!");
		}

	}

	@Test
	public void testSimpleProhibitedDependencies() {
		IResource[] inputResources = new IResource[1];
		inputResources[0] = project.getFolder("/src");

		// run analyze
		try {
			DepclipsePlugin.getJDependAdapter().startAnalyze(DepclipsePlugin.getJDependData(), inputResources);

			// IContainer folder = (IContainer) resource;
			IContainer tmp = project.getFolder("/src/c");
			TreeObject testObject = DepclipsePlugin.getJDependData()
					.getFromResourceMap(tmp);
			assertTrue(testObject.breaksDependencyRule());
		} catch (Exception e) {
			fail("An error occurred!");
		}

	}
	
	@Test
	public void filteredProjectPackage() {
		IResource[] inputResources = new IResource[1];
		inputResources[0] = project.getFolder("/src");
			
		// add b to filter
		DepclipsePlugin.getJDependData().addPackageToFilter("b");
		
		// run analyze
		try {
			DepclipsePlugin.getJDependAdapter().startAnalyze(DepclipsePlugin.getJDependData(), inputResources);
			List<JavaPackage> packs = DepclipsePlugin.getJDependData().getJavaPackages();
			boolean inThere = false;
			for(JavaPackage thisPack : packs) {
				if(thisPack.getName().equals("b")) {
					inThere = true;
				}
			}		
			assertFalse(inThere);
		} catch (Exception e) {
			fail("An error occurred!" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void filteredSelectedProjectPackage() {
		IResource[] inputResources = new IResource[5];
		inputResources[0] = project.getFolder("/src");	

		inputResources[1] = project.getFile("a.java");
		inputResources[2] = project.getFile("b.java");
		inputResources[3] = project.getFile("b2.java");
		inputResources[4] = project.getFile("c.java");
		
		// fake user selection
		DepclipsePlugin.getJDependData().setSelection(inputResources);
		// add b to filter
		DepclipsePlugin.getJDependData().addPackageToFilter("b");
		
		// run analyze
		try {
			List<IResource> list = Arrays.asList(DepclipsePlugin.getJDependData().getSelectedResources());
			for(IResource res : list) {
				if (res.getName().equals("b.java")) {
					fail("Filtered element was returned as selected object");
				}
			}

		} catch (Exception e) {
			fail("An error occurred!" + e.getMessage());
			e.printStackTrace();
		}
	}
}
