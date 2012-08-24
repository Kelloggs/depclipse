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

import org.eclipse.core.runtime.Assert;
import org.junit.Test;

import de.plugins.eclipse.depclipse.rules.AbstractPackageRule;
import de.plugins.eclipse.depclipse.rules.ProhibitedPackageRule;


public class ProhibitedPackageRuleTest {

	@Test
	public void testAddPackageMultipleTimes() {
		
		String packageName = "Neu";
		AbstractPackageRule newRule = new ProhibitedPackageRule(packageName);
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		newRule.addEfferentPackage("A");
		
		assertEquals("Although 'A' was added multiple times, it is expected to be in List just once", 1 , newRule.getEfferentPackages().size());
	}
	
	@Test
	public void AddUnconfirmedEfferentPackage() {
		ProhibitedPackageRule newRule = new ProhibitedPackageRule("Neu");
		newRule.addEfferentPackage("A");
		
		Assert.isTrue(!newRule.isConfirmed("A"));
	}
	
	@Test
	public void confirmEfferentPackage() {
		ProhibitedPackageRule newRule = new ProhibitedPackageRule("Neu");
		newRule.addEfferentPackage("A");
		newRule.setConfirmed("A", true);
		
		Assert.isTrue(newRule.isConfirmed("A"));		
	}
	
}
