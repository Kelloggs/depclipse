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

import java.util.Arrays;

import org.junit.Test;

import de.plugins.eclipse.depclipse.rules.AllowedPackageRule;
import de.plugins.eclipse.depclipse.rules.ProhibitedPackageRule;
import de.plugins.eclipse.depclipse.rules.ProjectRules;


public class ProjectRulesTest {

	@Test
	public void testGetAllowedPackageRule() {
		ProjectRules rules = new ProjectRules();
		
		String packageName = "Neu";
		
		AllowedPackageRule test = rules.getAllowedPackageRule(packageName);
		
		assertFalse("Expected, that getting a non existing Rules does nothing", rules.doAllowedRulesContain(packageName));		
		assertEquals("test object was expected to be null", null, test);
	}
	
	@Test
	public void testAddAllowedPackageRule() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		
		String packageName = "Neu";
		AllowedPackageRule newRule = new AllowedPackageRule(packageName);
		rules.addAllowedPackageRule(newRule);
		
		assertTrue("Expected, that the added Rule is in the List of ProjectRules", rules.doAllowedRulesContain(packageName));		
	}
	
	@Test
	public void testRemoveAllowedPackageRule() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		
		String packageName = "Neu";
		AllowedPackageRule newRule = new AllowedPackageRule(packageName);
		rules.addAllowedPackageRule(newRule);
		rules.removeAllowedPackageRule(newRule);
		
		assertFalse("Expected, that the formerly added Rule was removed", rules.doAllowedRulesContain(packageName));
	}
	
	@Test  (expected=IllegalAccessException.class)
	public void testTryOverwriteAllowedPackage() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		
		String packageName = "Erstes";
		AllowedPackageRule newRule = new AllowedPackageRule(packageName);
		newRule.addEfferentPackage("Soll noch da sein");
		rules.addAllowedPackageRule(newRule);
		
		AllowedPackageRule newRule2 = new AllowedPackageRule(packageName);
		newRule.addEfferentPackage("Soll nicht überschreiben");
		rules.addAllowedPackageRule(newRule2);
	}
	
	@Test
	public void testGetProhibitedPackageRule() {
		ProjectRules rules = new ProjectRules();
		
		String packageName = "Neu";
		
		assertEquals(null, rules.getProhibitedPackageRule(packageName));
		
		assertFalse("Expected, that the get of a non existing Package does nothing", rules.doProhibitedRulesContain(packageName));		
	}
	
	@Test
	public void testAddProhibitedPackageRule() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		
		String packageName = "Neu";
		rules.addProhibitedPackageRule(packageName);
		
		assertTrue("Expected, that the added Rule is in the List of ProjectRules", rules.doProhibitedRulesContain(packageName));		
	}
	
	@Test
	public void testRemoveProhibitedPackageRule() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		
		String packageName = "Neu";
		rules.addProhibitedPackageRule(packageName);
		ProhibitedPackageRule newRule = rules.getProhibitedPackageRule(packageName);
		rules.removeProhibitedPackageRule(newRule);
		
		assertFalse("Expected, that the formerly added Rule was removed", rules.doProhibitedRulesContain(packageName));
	}
	
	@Test  (expected=IllegalAccessException.class)
	public void testTryOverwriteProhibitedPackage() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		
		String packageName = "Erstes";
		ProhibitedPackageRule newRule = new ProhibitedPackageRule(packageName);
		newRule.addEfferentPackage("Soll noch da sein");
		rules.addProhibitedPackageRule(newRule);
		
		ProhibitedPackageRule newRule2 = new ProhibitedPackageRule(packageName);
		newRule.addEfferentPackage("Soll nicht überschreiben");
		rules.addProhibitedPackageRule(newRule2);
	}
	
	@Test
	public void updateExistingProhibitedPackageRule() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		ProhibitedPackageRule newRule = new ProhibitedPackageRule("A");
		newRule.addEfferentPackage("B");
		rules.addProhibitedPackageRule(newRule);
		
		rules.updateProhibitedPackageRule("A", Arrays.asList("C", "B"));		

		assertEquals(Arrays.asList("B", "C" ), rules.getProhibitedPackageRule("A").getEfferentPackages() );	
	}
	
	@Test
	public void confirmSingleProhibitedDepenency() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		ProhibitedPackageRule testee = new ProhibitedPackageRule("A", Arrays.asList("B", "C"));
		rules.addProhibitedPackageRule(testee);
		
		rules.confirmProhibitedDependency("A", "B");
		
		assertTrue(rules.getProhibitedPackageRule("A").isConfirmed("B"));
		assertFalse(rules.getProhibitedPackageRule("A").isConfirmed("C"));
	}
	
	@Test
	public void removeOrphaned() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		ProhibitedPackageRule proRule = new ProhibitedPackageRule("A");
		proRule.addEfferentPackage("B");
		proRule.addEfferentPackage("C");
		proRule.addEfferentPackage("D");
		proRule.setOrphaned("B", true);
		proRule.setOrphaned("C", true);
		rules.addProhibitedPackageRule(proRule);
		rules.removeOrphanedDependencies();
		
		assertEquals(Arrays.asList("D"), rules.getProhibitedPackageRule("A").getEfferentPackages());
	}
	
	@Test
	public void removeOrphanedLastEfferent() throws IllegalAccessException {
		ProjectRules rules = new ProjectRules();
		ProhibitedPackageRule proRule = new ProhibitedPackageRule("A");
		proRule.addEfferentPackage("C");
		proRule.setOrphaned("C", true);
		rules.addProhibitedPackageRule(proRule);
		rules.removeOrphanedDependencies();
		
		assertEquals(null, rules.getProhibitedPackageRule("A"));
	}
}
