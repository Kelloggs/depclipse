/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DependencyRule defines a rootPackage, which is the package for which this rule is
 * defined. You can add multiple packages as allowedDependencies.
 * 
 * @author Jens Cornelis
 */
public abstract class AbstractPackageRule {
	
	String rootPackage;
	Set<String> efferentPackages;
	Set<String> orphanedDependencies;
	
	/**
	 * Constructor with only the rootPackage defined
	 * 
	 * @param rootPackage name of the rootPackage
	 */
	public AbstractPackageRule(String rootPackage) {
		this.rootPackage = rootPackage;
		efferentPackages = new HashSet<String>();
		orphanedDependencies = new HashSet<String>();
	}
	
	/**
	 * Constructor with rootPackage and List of allowedDependencies
	 * 
	 * @param rootPackage name
	 * @param efferentPackages List<String> of the names of the allowedDependencies
	 */
	public AbstractPackageRule(String rootPackage, List<String> efferentPackages) {
		this(rootPackage);
		this.efferentPackages.addAll(efferentPackages);
	}
	
	/**
	 * Returns the name of the rootPackage of this rule
	 * @return name of rootPackage
	 */
	public String getRootPackage() {
		return rootPackage;	
	}
	
	/**
	 * Sets the name of the rootPackage of this rule
	 * @param name of rootPackage
	 */
	public void setRootPackage(String name) {
		rootPackage = name;
	}
	
	/**
	 * Gets a list of the efferent Dependencies of this rule
	 * @return List<String> names of allowedDependencies
	 */
	public List<String> getEfferentPackages() {
		return Arrays.asList(efferentPackages.toArray(new String[0]));
	}
	
	/**
	 * Adds the given List to efferent Dependencies 
	 * @param names of efferent Dependencies as List<String>
	 */
	public void addEfferentPackages(List<String> names) {
		efferentPackages.addAll(names);
	}
	
	/**
	 * Adds the name of a package to the efferent Dependencies
	 * @param name of package to be added to efferent Dependencies
	 */
	public void addEfferentPackage(String name) {
		efferentPackages.add(name);
	}
	
	/**
	 * Removes the package from the list of efferent Dependencies
	 * @param name of the to be remove package
	 */
	public void removeEfferentPackage(String name) {
		efferentPackages.remove(name);
	}

	/**
	 * Marks the given depPackage as orphaned/not orphaned
	 * 
	 * @param depPackage to be declared as orphaned/ not orphaned
	 * @param orphaned boolean (true if orphaned/ false if not)
	 */
	public void setOrphaned(String depPackage, boolean orphaned) {
		if(orphaned) {
			orphanedDependencies.add(depPackage);
		} else {
			orphanedDependencies.remove(depPackage);
		}	
	}
	
	/**
	 * Removes the orphaned dependencies for this package
	 */
	public void removeOrphaned() {
		for(String orph : orphanedDependencies) {
			efferentPackages.remove(orph);
		}
		orphanedDependencies.clear();
	}

	/**
	 * Returns boolean value whether or not the dependency of the rootPackage to
	 * this depPackage is orphaned.
	 * 
	 * @param depPackage
	 * @return boolean
	 */
	public boolean isOrphaned(String depPackage) {
		return orphanedDependencies.contains(depPackage);
	}
}
