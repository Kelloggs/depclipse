/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;

import de.plugins.eclipse.depclipse.model.JDependData;
import de.plugins.eclipse.depclipse.rules.ProjectRules;


public class JDependDataTest {
	
	@Test
	public void removeProhibitedPackageRule() throws IllegalAccessException {
		// create ProjectRules with one Rule
		ProjectRules rules = new ProjectRules();
		rules.addProhibitedPackageRule("A");
		rules.getProhibitedPackageRule("A").addEfferentPackages(Arrays.asList("B", "C"));
		
		// add projectRules to model
		JDependData model = new JDependData();
		model.setProjectRules(rules);
		
		// remove one efferent Package
		List<String[]> tmpList = new ArrayList<String[]>();
		tmpList.add(new String[] {"A", "C"});
		model.allowProhibitedDependency(tmpList);
		List<String> resultList = model.getProjectRules().getProhibitedPackageRule("A").getEfferentPackages();
		assertEquals(Arrays.asList("B"), resultList);	
	}
	
	@Test
	public void removeAllowedPackageRule() throws IllegalAccessException {
		// create ProjectRules with one Rule
		ProjectRules rules = new ProjectRules();
		rules.addAllowedPackageRule("A");
		rules.getAllowedPackageRule("A").addEfferentPackages(Arrays.asList("B", "C"));
		
		// add projectRules to model
		JDependData model = new JDependData();
		model.setProjectRules(rules);
		
		// remove one efferent Package
		List<String[]> tmpList = new ArrayList<String[]>();
		tmpList.add(new String[] {"A", "C"});
		model.prohibitAllowedDependency(tmpList);
		List<String> resultList = model.getProjectRules().getAllowedPackageRule("A").getEfferentPackages();
		assertEquals(Arrays.asList("B"), resultList);	
	}
}
