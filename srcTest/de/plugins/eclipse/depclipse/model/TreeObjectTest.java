/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.model;

import static org.junit.Assert.*;

import static org.easymock.EasyMock.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import jdepend.framework.JavaPackage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.model.TreeFolder;
import de.plugins.eclipse.depclipse.model.TreeObject;
import de.plugins.eclipse.depclipse.rules.IRuleParser;
import de.plugins.eclipse.depclipse.rules.ProjectRules;
import de.plugins.eclipse.depclipse.testcommons.JDepend4EclipseTest;

public class TreeObjectTest extends JDepend4EclipseTest {
	IRuleParser mock;
	
	public void mockSetUp() throws FileNotFoundException, IOException, CoreException, IllegalAccessException {
		// for this test, we have to ensure, that xml allows everything first
		ProjectRules rules = new ProjectRules();
		rules.addAllowedPackageRule("b");
		rules.getAllowedPackageRule("b").addEfferentPackages(Arrays.asList("a"));
		rules.addAllowedPackageRule("b2");
		rules.getAllowedPackageRule("b2").addEfferentPackages(Arrays.asList("b", "a"));
		rules.addAllowedPackageRule("c");
		rules.getAllowedPackageRule("c").addEfferentPackages(Arrays.asList("b"));
		
		mock = createMock(IRuleParser.class);
		expect(mock.getProjectRules(isA(IFile.class))).andReturn(rules).atLeastOnce();
		mock.setProjectRules(isA(IFile.class), isA(ProjectRules.class));
		expectLastCall().atLeastOnce();
		replay(mock);
		DepclipsePlugin.setProjectRuleParser(mock);	
	}

	public void mockTearDown() {
		verify(mock);
		DepclipsePlugin.setProjectRuleParser(null);
	}

//	@Test
//	public void breaksDependencyRule_True() throws IOException, CoreException, IllegalAccessException, SAXParseException {
//
//		testLogic("c - Allowed: a/b2", "c", Arrays.asList("a", "b2"), 1);
//		testLogic("b - Allowed: b2/c", "b", Arrays.asList("b2", "c"), 1);
//		testLogic("b - Allowed: none", "b", new ArrayList<String>(), 1);
//		testLogic("b2 - Allowed: none", "b2", new ArrayList<String>(), 2);
//		testLogic("b2 - Allowed: a", "b2", Arrays.asList("a"), 1);
//	}

	@Test
	public void breaksDependencyRule_False() throws IOException, CoreException, IllegalAccessException, SAXParseException {

		testLogic("a - Allowed: b/b2/c", "a", new ArrayList<String>(), 0);
		testLogic("b - Allowed: a", "b", Arrays.asList("a"), 0);
		testLogic("c - Allowed: b", "c", Arrays.asList("b"), 0);
		testLogic("c - Allowed: a/b", "c", Arrays.asList("a", "b"), 0);
		testLogic("b - Allowed: a/c", "b", Arrays.asList("a", "c"), 0);
		testLogic("b2 - Allowed: a/b/c", "b2",
				Arrays.asList("a", "b", "c"), 0);
	}
	
	@Test
	public void testAgainstNotExistingRule() {
		TreeObject to = new TreeFolder();
		
		ProjectRules rules = new ProjectRules();
		List<JavaPackage> list = new ArrayList<JavaPackage>();
		list.add(new JavaPackage("b"));
		list.add(new JavaPackage("c"));
		
		// no allowed packages - should be true though
		assertTrue(to.checkAgainstDependencyRule(rules, list));
	}

	private void testLogic(String testname, String rootPackage,
			List<String> allowedDependencies, int expectedBadDependencies)
			throws IOException, CoreException, IllegalAccessException, SAXParseException {
		mockSetUp();
		// set up rule
		ProjectRules rules = new ProjectRules();
		
		rules.addAllowedPackageRule(rootPackage);
		rules.getAllowedPackageRule(rootPackage).addEfferentPackages(allowedDependencies);

		// assign project folder
		IResource[] inputResources = new IResource[1];
		inputResources[0] = project.getFolder("/src");

		// use jdepend to analyze
		DepclipsePlugin.getJDependAdapter().startAnalyze(DepclipsePlugin.getJDependData(), inputResources);
		TreeObject testObject;

		// check if the rules were applied correctly
		for (JavaPackage pack : DepclipsePlugin.getJDependData().getJavaPackages()) {
			if (pack.getName().equalsIgnoreCase(rootPackage)) {
				testObject = DepclipsePlugin.getJDependData().getRoot()
						.findChild(pack);
				if (expectedBadDependencies == 0) {
					assertFalse(
							testname
									+ ": 0 bad Dependencies expected, but breaksDependencyRule returned true",
							testObject.checkAgainstDependencyRule(rules, pack
									.getEfferents()));
				} else {
					assertTrue(
							testname
									+ ": bad Dependencies expected, but breaksDependencyRule returned false",
							testObject.checkAgainstDependencyRule(rules, pack
									.getEfferents()));
				}
				assertEquals(
						testname
								+ ": The amount of bad dependencies didnt fit the expected value",
						expectedBadDependencies, testObject
								.getBadDependencies().size());
			}
		}
		mockTearDown();
	}
}
