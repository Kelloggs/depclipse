/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.rules;

import java.util.List;

/**
 * PackageRule for a rootPackage containing all its allowed efferent
 * dependencies.
 * 
 * @author Jens Cornelis
 */
public class AllowedPackageRule extends AbstractPackageRule {

	public AllowedPackageRule(String rootPackage) {
		super(rootPackage);
	}
	
	public AllowedPackageRule(String rootPackage, List<String> efferentPackages) {
		super(rootPackage, efferentPackages);
	}
}
