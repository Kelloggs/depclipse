/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.rules;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import de.plugins.eclipse.depclipse.rules.AllowedPackageRule;
import de.plugins.eclipse.depclipse.rules.IRuleParser;
import de.plugins.eclipse.depclipse.rules.ProhibitedPackageRule;
import de.plugins.eclipse.depclipse.rules.ProjectRules;
import de.plugins.eclipse.depclipse.rules.XMLRuleParser;
import de.plugins.eclipse.depclipse.testcommons.JDepend4EclipseTest;

public class XMLRuleParserTest extends JDepend4EclipseTest {
	
	@Before
	public void setUp() {
		env.addFile(env.getProject(PROJECT_NAME).getFullPath(),
				"DependencyRules.jdr", xmlRules);
	}

	@After
	public void tearDown() {
		env.removeFile(env.getProject(PROJECT_NAME).getFile(
				"DependencyRules.jdr").getFullPath());
	}
	
	@Test 
	public void setProhibitedRuleDefinitions() throws IllegalAccessException, IOException, CoreException {
		IFile testFile = project.getFile("DependencyRules.jdr");
		IRuleParser parser = new XMLRuleParser();
		
		ProjectRules rules = new ProjectRules();
		List<String> testListA = Arrays.asList("b", "c");
		List<String> testListB = Arrays.asList("a", "c");
		Collections.sort(testListA);
		Collections.sort(testListB);
		
		rules.addProhibitedPackageRule("a");
		ProhibitedPackageRule rule1 = rules.getProhibitedPackageRule("a");
		rule1.addEfferentPackages(testListA);
		rules.addProhibitedPackageRule("b");
		ProhibitedPackageRule rule2 = rules.getProhibitedPackageRule("b");
		rule2.addEfferentPackages(testListB);
		
		// write rules to XML FILE
		parser.setProjectRules(testFile, rules);
		
		// deserialize from file and see if expected objects are instantiated
		rules = parser.getProjectRules(testFile);
		ProhibitedPackageRule result = rules.getProhibitedPackageRule("a");
		List<String> resultListA = result.getEfferentPackages();
		Collections.sort(resultListA);
		assertEquals("a", result.getRootPackage());
		assertEquals(testListA, resultListA);
		
		result = rules.getProhibitedPackageRule("b");
		List<String> resultListB = result.getEfferentPackages();
		Collections.sort(resultListB);
		assertEquals("b", result.getRootPackage());
		assertEquals(testListB, resultListB);
	}
	
	@Test
	public void serializeOrphanedProhibited() throws IllegalAccessException, IOException, CoreException {
		IFile testFile = project.getFile("DependencyRules.jdr");
		IRuleParser parser = new XMLRuleParser();
		
		ProjectRules rules = new ProjectRules();
		List<String> testListA = Arrays.asList("b", "c");

		rules.addProhibitedPackageRule("a");
		ProhibitedPackageRule rule = rules.getProhibitedPackageRule("a");
		rule.addEfferentPackages(testListA);
		rule.setOrphaned("b", true);
		
		// write rules to XML FILE
		parser.setProjectRules(testFile, rules);
		
		// deserialize from file and see if expected objects are instantiated
		ProjectRules testee = parser.getProjectRules(testFile);
		assertTrue(testee.getProhibitedPackageRule("a").isOrphaned("b"));
		assertFalse(testee.getProhibitedPackageRule("a").isOrphaned("c"));
	}
	
	@Test
	public void serializeOrphanedAllowed() throws IllegalAccessException, IOException, CoreException {
		IFile testFile = project.getFile("DependencyRules.jdr");
		IRuleParser parser = new XMLRuleParser();
		
		ProjectRules rules = new ProjectRules();
		List<String> testListA = Arrays.asList("b", "c");

		rules.addAllowedPackageRule("a");
		AllowedPackageRule rule = rules.getAllowedPackageRule("a");
		rule.addEfferentPackages(testListA);
		rule.setOrphaned("b", true);
		
		// write rules to XML FILE
		parser.setProjectRules(testFile, rules);
		
		// deserialize from file and see if expected objects are instantiated
		ProjectRules testee = parser.getProjectRules(testFile);
		assertTrue(testee.getAllowedPackageRule("a").isOrphaned("b"));
		assertFalse(testee.getAllowedPackageRule("a").isOrphaned("c"));
	}
	
	
	@Test 
	public void getProhibitedRuleDefinitions() throws IOException, CoreException, SAXParseException {
		
		IFile testFile = project.getFile("DependencyRules.jdr");
		IRuleParser parser = new XMLRuleParser();
		ProjectRules rules = parser.getProjectRules(testFile);
		
		
		ProhibitedPackageRule result = rules.getProhibitedPackageRule("a");
		List<String> resultList = result.getEfferentPackages();
		List<String> expectedList = Arrays.asList("b");
		Collections.sort(expectedList);
		Collections.sort(resultList);
		assertEquals("a", result.getRootPackage());
		assertEquals(expectedList, resultList);
		
		result = rules.getProhibitedPackageRule("c");
		resultList = result.getEfferentPackages();
		expectedList = Arrays.asList("a");
		Collections.sort(expectedList);
		Collections.sort(resultList);
		assertEquals("c", result.getRootPackage());
		assertEquals(expectedList, resultList);
	}

	@Test 
	public void setAllowedRuleDefinitions() throws IOException, CoreException, SAXParseException, IllegalAccessException {
		IFile testFile = project.getFile("DependencyRules.jdr");
		IRuleParser parser = new XMLRuleParser();
		
		ProjectRules rules = new ProjectRules();
		List<String> testListA = Arrays.asList("b", "c");
		List<String> testListB = Arrays.asList("a", "c");
		Collections.sort(testListA);
		Collections.sort(testListB);
		
		rules.addAllowedPackageRule(new AllowedPackageRule("a", testListA));
		rules.addAllowedPackageRule(new AllowedPackageRule("b", testListB));
		
		// write rules to XML FILE
		parser.setProjectRules(testFile, rules);
		
		// deserialize from file and see if expected objects are instantiated
		rules = parser.getProjectRules(testFile);
		AllowedPackageRule result = rules.getAllowedPackageRule("a");
		List<String> resultListA = result.getEfferentPackages();
		Collections.sort(resultListA);
		assertEquals("a", result.getRootPackage());
		assertEquals(testListA, resultListA);
		
		result = rules.getAllowedPackageRule("b");
		List<String> resultListB = result.getEfferentPackages();
		Collections.sort(resultListB);
		assertEquals("b", result.getRootPackage());
		assertEquals(testListB, resultListB);
	}
	
	@Test
	public void serializeUnConfirmedProhibitedRule() throws IllegalAccessException, IOException, CoreException {
		IFile testFile = project.getFile("DependencyRules.jdr");
		IRuleParser parser = new XMLRuleParser();
		
		ProjectRules rules = new ProjectRules();
		List<String> testListA = Arrays.asList("b", "c");
		Collections.sort(testListA);
		
		rules.addProhibitedPackageRule("a");
		rules.getProhibitedPackageRule("a").addEfferentPackages(testListA);
		
		parser.setProjectRules(testFile, rules);
		
		rules = parser.getProjectRules(testFile);
		ProhibitedPackageRule result = rules.getProhibitedPackageRule("a");
		
		for(String efferentPackage : result.getEfferentPackages()) {
			Assert.isTrue(!result.isConfirmed(efferentPackage));
		}
	}
	
	@Test
	public void serializeConfirmedProhibitedRule() throws IllegalAccessException, IOException, CoreException {
		IFile testFile = project.getFile("DependencyRules.jdr");
		IRuleParser parser = new XMLRuleParser();
		
		ProjectRules rules = new ProjectRules();
		List<String> testListA = Arrays.asList("b", "c");
		Collections.sort(testListA);
		rules.addProhibitedPackageRule("a");
		ProhibitedPackageRule testee = rules.getProhibitedPackageRule("a");
		testee.addEfferentPackages(testListA);
		testee.setConfirmed("b", true);
		
		
		parser.setProjectRules(testFile, rules);
		
		rules = parser.getProjectRules(testFile);
		ProhibitedPackageRule result = rules.getProhibitedPackageRule("a");
		
		Assert.isTrue(result.isConfirmed("b"));
	}
	
	@Test 
	public void getAllowedRuleDefinitions() throws IOException, CoreException, SAXParseException, IllegalAccessException {
		
		IFile testFile = project.getFile("DependencyRules.jdr");
		IRuleParser parser = new XMLRuleParser();
		ProjectRules rules = parser.getProjectRules(testFile);
		
		AllowedPackageRule result = rules.getAllowedPackageRule("a");
		assertEquals(null, result);
		
		result = rules.getAllowedPackageRule("b");
		List<String> resultList = result.getEfferentPackages();
		List<String> expectedList = Arrays.asList("a");
		Collections.sort(expectedList);
		Collections.sort(resultList);
		assertEquals("b", result.getRootPackage());
		assertEquals(expectedList, resultList);
		
		result = rules.getAllowedPackageRule("b2");
		resultList = result.getEfferentPackages();
		expectedList = Arrays.asList("a", "b");
		Collections.sort(expectedList);
		Collections.sort(resultList);
		assertEquals("b2", result.getRootPackage());
		assertEquals(expectedList, resultList);
		
		result = rules.getAllowedPackageRule("c");
		resultList = result.getEfferentPackages();
		expectedList = Arrays.asList("b");
		Collections.sort(expectedList);
		Collections.sort(resultList);
		assertEquals("c", result.getRootPackage());
		assertEquals(expectedList, resultList);
	}
	
	protected static final String xmlRules =
		"<ProjectRules>"
		+ "<JDepend4Eclipse-Version>1.3.0-alpha</JDepend4Eclipse-Version>"
		+ "<Allowed>"
		+ "<PackageRule>\n"
		+ "<RootPackage>b</RootPackage>\n"
		+ "<DepPackage><Name>a</Name><Orphaned>false</Orphaned></DepPackage>\n"
		+ "</PackageRule>\n"
		+ "<PackageRule>\n"
		+ "<RootPackage>b2</RootPackage>\n"
		+ "<DepPackage><Name>a</Name><Orphaned>false</Orphaned></DepPackage>\n"
		+ "<DepPackage><Name>b</Name><Orphaned>false</Orphaned></DepPackage>\n"
		+ "</PackageRule>\n"
		+ "<PackageRule>\n"
		+ "<RootPackage>c</RootPackage>\n"
		+ "<DepPackage><Name>b</Name><Orphaned>false</Orphaned></DepPackage>\n"
		+ "</PackageRule>\n"
		+ "</Allowed>\n"	
		+ "<Prohibited>"
		+ "<PackageRule>\n"
		+ "<RootPackage>a</RootPackage>\n"
		+ "<DepPackage>" +
				"<Name>b</Name><Confirmed>false</Confirmed><Orphaned>false</Orphaned></DepPackage>\n"
		+ "</PackageRule>\n"
		+ "<PackageRule>\n"
		+ "<RootPackage>c</RootPackage>\n"
		+ "<DepPackage>" +
				"<Name>a</Name><Confirmed>false</Confirmed><Orphaned>false</Orphaned></DepPackage>\n"
		+ "</PackageRule>\n"
		+ "</Prohibited>"
		+ "</ProjectRules>";
			
}
