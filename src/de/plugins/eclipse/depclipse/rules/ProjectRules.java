/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jdepend.framework.JavaPackage;

/**
 * Structure containing all defined prohibited and allowed Dependencies.
 * 
 * @author Jens Cornelis
 */
public class ProjectRules {
	private Map<String, AllowedPackageRule> allowedPackageRules;
	private Map<String, ProhibitedPackageRule> prohibitedPackageRules;
	
	public ProjectRules() {
		allowedPackageRules = new HashMap<String, AllowedPackageRule>();
		prohibitedPackageRules = new HashMap<String, ProhibitedPackageRule>();
	}

	/**
	 * Returns a Map of the allowed PackageRules in this instance of
	 * ProjectRules. 
	 * 
	 * Key: Name of rootPackage
	 * Values: list of PackageRules
	 * 
	 * @return map of the packageRules
	 */
	public Map<String, AllowedPackageRule> getAllowedPackageRules() {
		return allowedPackageRules;
	}
	
	/**
	 * Adds a AllowedPackageRule to these ProjectRules. This method is only
	 * meant for usage when parsing rules with implementation of IRuleParser.
	 * Therefore visibility should be kept protected.
	 * 
	 * @param the AllowedPackageRule to be added
	 * @throws IllegalAccessException thrown when PackageRule for this RootPackage already exists
	 */
	protected void addAllowedPackageRule(AllowedPackageRule packageRule) throws IllegalAccessException {
		if(allowedPackageRules.containsKey(packageRule.getRootPackage())) {
			throw new IllegalAccessException("PackageRule for this Package already existing"); //$NON-NLS-1$
		}
		allowedPackageRules.put(packageRule.getRootPackage(), packageRule);
	}
	
	/**
	 * Adds a AllowedPackageRule for the Package with the given name to this ProjectRules. 
	 * 
	 * @param rootPackage Name of the rootPackage to be added
	 * @throws IllegalAccessException thrown if Rule for this Package already exists
	 */
	public void addAllowedPackageRule(String rootPackage) throws IllegalAccessException {
		AllowedPackageRule newRule = new AllowedPackageRule(rootPackage);
		addAllowedPackageRule(newRule);
	}

	/**
	 * Removes an AllowedPackageRule from this ProjectRules.
	 * 
	 * @param packageRule to be removed
	 */
	public void removeAllowedPackageRule(AllowedPackageRule packageRule) {
		allowedPackageRules.remove(packageRule.getRootPackage());
	}
	
	/**
	 * Returns the AllowedPackageRule for the package with the given rootPackage-Name
	 * 
	 * @param rootPackage name of the searched package
	 * @return the PackageRule or null if no AllowedPackageRule is existing for this rootPackage
	 */
	public AllowedPackageRule getAllowedPackageRule(String rootPackage) {
		if(allowedPackageRules.containsKey(rootPackage)) {
			return allowedPackageRules.get(rootPackage);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Searches for a AllowedPackageRule for the package with the given name. 
	 * True if a PackageRule exists. False if non-existent.
	 * 
	 * @param rootPackage name of the searched rootPackage
	 * @return true if a AllowedPackageRule exists for this rootPackage. False if non-existent.
	 */
	public boolean doAllowedRulesContain(String rootPackage) {
		return allowedPackageRules.containsKey(rootPackage);		
	}

	/**
	 * Adds a ProhibitedPackageRule to this ProjectRules. This method should only be used
	 * when parsing ProjectRules using IRuleParser. The visibility should therefore be kept
	 * protected.
	 * 
	 * @param packageRule to be added
	 * @throws IllegalAccessException thrown if a Rule for this Package already exists
	 */
	protected void addProhibitedPackageRule(ProhibitedPackageRule packageRule) throws IllegalAccessException {
		if(prohibitedPackageRules.containsKey(packageRule.getRootPackage())) {
			throw new IllegalAccessException("PackageRule for this Package already existing"); //$NON-NLS-1$
		}
		prohibitedPackageRules.put(packageRule.getRootPackage(), packageRule);
		
	}

	/**
	 * Adds a ProhibitedPackageRule with the given name to this ProjectRules
	 * 
	 * @param rootPackage the name of the to be added package
	 * @throws IllegalAccessException if a rule for this package is already existing
	 */
	public void addProhibitedPackageRule(String rootPackage) throws IllegalAccessException  {
		ProhibitedPackageRule newRule = new ProhibitedPackageRule(rootPackage);
		addProhibitedPackageRule(newRule);	
	}

	/**
	 * Removes the given ProhibitedPackage rule from this ProjectRules.
	 * 
	 * @param packageRule to be removed
	 */
	public void removeProhibitedPackageRule(ProhibitedPackageRule packageRule) {
		prohibitedPackageRules.remove(packageRule.getRootPackage());
		
	}

	/**
	 * Returns true, if a ProhibitedPackageRule for the package with the given name
	 * is existing in this ProjectRules.
	 * 
	 * @param packageName
	 * @return boolean existing
	 */
	public boolean doProhibitedRulesContain(String packageName) {
		return prohibitedPackageRules.containsKey(packageName);
	}

	/**
	 * Returns the ProhibitedPackageRule for the package with the given name.
	 * 
	 * @param packageName
	 * @return the ProhibitedPackageRule for the packageName
	 */
	public ProhibitedPackageRule getProhibitedPackageRule(String packageName) {
		if(prohibitedPackageRules.containsKey(packageName)) {
			return prohibitedPackageRules.get(packageName);
		}
		else {
			return null;
		}		
	}

	/**
	 * Returns all ProhibitedPackageRules in this ProjectRules
	 * 
	 * @return the ProhibitedPackageRules
	 */
	public Map<String, ProhibitedPackageRule> getProhibitedPackageRules() {
		return prohibitedPackageRules;
	}

	/**
	 * Convenience Method for updating the ProjectRules. This should be used when running
	 * JDepend to update the ProjectRules and look for entries, which are not anymore existing
	 * in the project.
	 * 
	 * @param packages List<JavaPackage> of JavaPackages to compare Rules with
	 */
	public void updateProjectRules(List<JavaPackage> packages) {
		// reset rules
		List<AbstractPackageRule> rules = new ArrayList<AbstractPackageRule>();
		rules.addAll(getAllowedPackageRules().values());
		rules.addAll(getProhibitedPackageRules().values());
		for(AbstractPackageRule rule : rules) {
			for(String efferent : rule.getEfferentPackages()) {
				rule.setOrphaned(efferent, true);
			}
		}
		
		// and update them
		for(JavaPackage jp : packages) {
			ProhibitedPackageRule proDep = getProhibitedPackageRule(jp.getName());
			AllowedPackageRule allDep = getAllowedPackageRule(jp.getName());
			List<String> actualEfferents = new ArrayList<String>();
			for(JavaPackage pack : jp.getEfferents()) {
				actualEfferents.add(pack.getName());
			}
			if(proDep != null) {
				for(String packName : proDep.getEfferentPackages()) {
					if(actualEfferents.contains(packName)) {
						proDep.setOrphaned(packName, false);
					}
				}
			}
			if(allDep != null) {
				for(String packName : allDep.getEfferentPackages()) {
					if(actualEfferents.contains(packName)) {
						allDep.setOrphaned(packName, false);
					}
				}
			}
		}
	}
	
	/**
	 * Updated the ProhibitedPackageRule for the given package and its broken DependencyRules.
	 * 
	 * @param packageName
	 * @param prohibitedDependencies
	 */
	public void updateProhibitedPackageRule(String packageName, List<String> prohibitedDependencies) {
        if(!doProhibitedRulesContain(packageName)) {
            try {
                    addProhibitedPackageRule(packageName);
            } catch (IllegalAccessException e) {}
        }
        getProhibitedPackageRule(packageName).addEfferentPackages(prohibitedDependencies);
    }
	
	/**
	 * Moves the prohibited Dependency between rootPackage and depPackage to the
	 * AllowedPackageRules.
	 * 
	 * @param rootPackage
	 * @param depPackage
	 */
	public void allowProhibitedDependency(String rootPackage, String depPackage) {
		boolean orphaned = getProhibitedPackageRule(rootPackage).isOrphaned(depPackage);
		getProhibitedPackageRule(rootPackage).removeEfferentPackage(depPackage);
		if(getProhibitedPackageRule(rootPackage).getEfferentPackages().size() == 0) {
			removeProhibitedPackageRule(getProhibitedPackageRule(rootPackage));
		}
		if(!doAllowedRulesContain(rootPackage)) {
			try {
				addAllowedPackageRule(rootPackage);
			} catch (IllegalAccessException e) {
			}	
		} 	
		getAllowedPackageRule(rootPackage).addEfferentPackage(depPackage);
		getAllowedPackageRule(rootPackage).setOrphaned(depPackage, orphaned);
	}
	
	/**
	 * Moves the formerly allowed Dependency between rootPackage and depPackage to
	 * the ProhibitedPackageRules.
	 * 
	 * @param rootPackage
	 * @param depPackage
	 */
	public void prohibitAllowedDependency(String rootPackage, String depPackage) {
		boolean orphaned = getAllowedPackageRule(rootPackage).isOrphaned(depPackage);
		getAllowedPackageRule(rootPackage).removeEfferentPackage(depPackage);
		if(getAllowedPackageRule(rootPackage).getEfferentPackages().size() == 0) {
			removeAllowedPackageRule(getAllowedPackageRule(rootPackage));
		}
		if(!doProhibitedRulesContain(rootPackage)) {
			try {
				addProhibitedPackageRule(rootPackage);
			} catch (IllegalAccessException e) {
			}	
		} 	
		getProhibitedPackageRule(rootPackage).addEfferentPackage(depPackage);
		getProhibitedPackageRule(rootPackage).setOrphaned(depPackage, orphaned);
		getProhibitedPackageRule(rootPackage).setConfirmed(depPackage, true);
	}

	/**
	 * Sets the dependency between rootPackage and depPackage as confirmed by 
	 * the user
	 * 
	 * @param rootPackage
	 * @param depPackage
	 */
	public void confirmProhibitedDependency(String rootPackage,
			String depPackage) {
		getProhibitedPackageRule(rootPackage).setConfirmed(depPackage, true);
		
	}

	/**
	 * Checks the PackageRules of this ProjectRules for orphaned entries and removes them!
	 * Make sure to ask the user if he really wants to do this!
	 */
	public void removeOrphanedDependencies() {
		Iterator<AllowedPackageRule> itAllowed = getAllowedPackageRules().values().iterator();
		while(itAllowed.hasNext()) {
			AllowedPackageRule rule = itAllowed.next();
			rule.removeOrphaned();
			// if this rule has no more efferent packages, it can be removed 
			// from project rules to keep the file clean
			if(rule.getEfferentPackages().size() == 0) {
				itAllowed.remove();
			}
		}
		Iterator<ProhibitedPackageRule> itProhibited = getProhibitedPackageRules().values().iterator();
		while(itProhibited.hasNext()) {
			ProhibitedPackageRule rule = itProhibited.next();
			rule.removeOrphaned();
			// same as above
			if(rule.getEfferentPackages().size() == 0) {
				itProhibited.remove();
			}
		}
	}
}
