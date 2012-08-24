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

import org.junit.Test;

import de.plugins.eclipse.depclipse.rules.AbstractPackageRule;
import de.plugins.eclipse.depclipse.rules.AllowedPackageRule;


public class AllowedPackageRuleTest {

	@Test
	public void testAddPackageMultipleTimes() {
		
		String packageName = "Neu";
		AbstractPackageRule newRule = new AllowedPackageRule(packageName);
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		
		assertEquals("Although 'A' was added multiple times, it is expected to be in List just once", 1 , newRule.getEfferentPackages().size());
	}
	
	@Test
	public void setOrphanedPropertyTest() {
		AbstractPackageRule newRule = new AllowedPackageRule("Test");
		newRule.addEfferentPackage("A");
		
		newRule.setOrphaned("A", true);
		
		assertTrue(newRule.isOrphaned("A"));
	}
	
	@Test
	public void newPackageRuleIsNotOrphanedByDefault() {
		AbstractPackageRule newRule = new AllowedPackageRule("Test");
		newRule.addEfferentPackage("A");
		
		assertFalse(newRule.isOrphaned("A"));
	}
}
