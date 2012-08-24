/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.rules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PackageRule for a rootPackage containing all its prohibited Dependencies.
 * 
 * @author Jens Cornelis
 */
public class ProhibitedPackageRule extends AbstractPackageRule {
	private Set<String> confirmedEfferentPackages;
	
	public ProhibitedPackageRule(String rootPackage) {
		super(rootPackage);
		confirmedEfferentPackages = new HashSet<String>();
	}
	
	public ProhibitedPackageRule(String rootPackage, List<String> efferentPackages) {
		super(rootPackage, efferentPackages);
		confirmedEfferentPackages = new HashSet<String>();
	}

	/**
	 * Returns true, if the user has confirmed the efferent package
	 * in this package rule. Otherwise false is returned
	 * 
	 * @param packageName name of the searched efferent package
	 * @return boolean
	 */
	public boolean isConfirmed(String packageName) {
		return confirmedEfferentPackages.contains(packageName);
	}

	/**
	 * Set the package with the given name as confirmed.
	 * 
	 * @param packageName to be marked as confirmed
	 */
	public void setConfirmed(String packageName, boolean flag) {
		if(flag) {
			confirmedEfferentPackages.add(packageName);
		} else {
			confirmedEfferentPackages.remove(packageName);
		}
	}

}
