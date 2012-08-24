/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;

import org.eclipse.core.resources.IResource;

import de.plugins.eclipse.depclipse.rules.ProjectRules;

/**
 * Model for JDepend4Eclipse. 
 * 
 * @author Jens Cornelis
 */
public class JDependData {
	private PackageFilter filter;
	private ProjectRules projectRules;
	private TreeFolder root;
	private List<JavaPackage> javaPackages;
	private Map<IResource, TreeObject> resourceMap;
	private IResource[] selection;
	private String jdependOutput;
	protected transient PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	/**
	 * Standard Constructor
	 */
	public JDependData() {
		filter = new PackageFilter();
    	resourceMap = new HashMap<IResource, TreeObject>();
	}

	/**
	 * Adds the specified package name to the collection
	 * of packages to be filtered.
	 *
	 * @param name Package name.
	 */
	public void addPackageToFilter(String name) {
	    if (name.endsWith(".*")) {  //$NON-NLS-1$
	        name = name.substring(0, name.length()-2);
	    }
	    if (name.length() > 0 && !filter.getFilters().contains(name)) {
	        filter.addPackage(name);
	    }
	}

	/**
	 * Get the Filters from the PreferenceStore and sets them for
	 * this JDependData instance.
	 */
	public void setFilters(String[] filters) {    
        for (int i = 0; i < filters.length; i++) {
            addPackageToFilter(filters[i]);
        }
	}

	/** 
	 * Gets the PackageFilter. It is not checked, if the preferences are set
	 * to "use filters" or not.
	 * 
	 * @return the PackageFilter
	 */
    public PackageFilter getFilter() {
        return filter;
    }
    
    /**
     * Returns the TreeFolder which is the Root Folder of this JDependData.
     * 
     * @return TreeFolder root
     */
    public TreeFolder getRoot() {
		return root;
	}

    /**
     * Sets the root Treefolder
     * 
     * @param root
     */
	public void setRoot(TreeFolder root) {
		this.root = root;
	}
	
	/**
	 * Sets the selected Elements. The selected Elements are of type IResource[]
	 * and are a subset of the complete IResources of this JDependData   
	 * 
	 * @param selection IResource[] - the selected IResources
	 */
	public void setSelection(IResource[] selection) {
		this.selection = selection;
		firePropertyChange("SELECTION_CHANGED", null, this); //$NON-NLS-1$
	}
	
	/**
	 * Returns the broken PackageRules for the selected IResource.
	 * 
	 * @return TreeMap<String, List<String>>
	 */
	public TreeMap<String, List<String>> getSelectedBrokenDependencyRules() {		
		TreeMap<String, List<String>> returnMap = new TreeMap<String, List<String>>();	
		if(selection != null) {
			for(IResource resource : selection) {
				TreeObject tempObject = resourceMap.get(resource);	
				returnMap.put(tempObject.getName(), tempObject.getBadDependencies());
			}				
		}
		return returnMap;		
	}

	/**
	 * Returns the selected Elements as IResource[]. 
	 * 
	 * @return IResource[] the selected Resource
	 */
	public IResource[] getSelectedResources() {
		List<IResource> retVal = new ArrayList<IResource>();
		for(IResource resource : selection) {
			if(resourceMap.containsKey(resource)) {
				JavaPackage tmp = getJavaPackage(resourceMap.get(resource).getPackageName());
				// could be null, if filtered
				if(tmp != null) {
					retVal.add(resource);
				}
			}
		}
		return retVal.toArray(new IResource[0]);
	}

	/**
	 * Returns the selected Elements as a List of JavaPackage instances
	 * 
	 * @return List<JavaPackage> the selected Elements
	 */
	public List<JavaPackage> getSelectedJavaPackages() {
		List<JavaPackage> result = new ArrayList<JavaPackage>();
		for(IResource resource : selection) {
			JavaPackage tmp = getJavaPackage(resourceMap.get(resource).getPackageName());
			// could be null, if a class is completely commented but existing
			if(tmp != null) {
				result.add(tmp);
			}
		}
		return result;
	}
	
	/**
     * Resets the ResourceMap for this JDependData
     */
	public void clearResourceMap() {
		resourceMap.clear();
	}
	
	/**
	 * Sets the JavaPackages
	 * 
	 * @param javaPackages to be set
	 */
	public void setJavaPackages(List<JavaPackage> javaPackages) {
		this.javaPackages = javaPackages;		
	}
	
	/**
	 * Gets the JavaPackages
	 * 
	 * @return List<JavaPackage>
	 */
	public List<JavaPackage> getJavaPackages() {
		return javaPackages;
	}
	
	private JavaPackage getJavaPackage(String name) {
		JavaPackage result = null;
		for(JavaPackage pack : javaPackages) {
			if(pack.getName().equals(name)) {
				result = pack;
			}
		}
		return result;
	}
	
	/**
	 * Checks, if the given IResource is contained in this resourceMap.
	 * <true> if the IResource is found 
	 * 
	 * @param resource to be looked up
	 * @return <true> if the IResource is found
	 */
	public boolean resourceMapContains(IResource resource) {
		return resourceMap.containsKey(resource);
	}
	
	/**
	 * Puts the given IResource (key) and the TreeObject (value)
	 * to this JDependDatas resourceMap
	 * 
	 * @param key the IResource
	 * @param value the TreeObject
	 */
	public void putToResourceMap(IResource key, TreeObject value) {
		resourceMap.put(key, value);
	}
	
	/**
	 * Gets TreeObject for the given IResource from the ResourceMap
	 * 
	 * @param key the IResource key
	 * @return TreeObject the corresponding TreeObject to the given key
	 */
	public TreeObject getFromResourceMap(IResource key) {
		return resourceMap.get(key);
	}
    
	/**
	 * Gets IResource[] resources
	 * 
	 * @return the IResources.
	 */
    public IResource[] getResources() {
    	return resourceMap.keySet().toArray(new IResource[0]);
    }
    
    /**
     * Sets the JDepend output
     * 
     * @param fileOutput JDepend output as String
     */
    public void setFileOutput(String fileOutput) {
		this.jdependOutput = fileOutput;      
		firePropertyChange("fileOutput", null, fileOutput); //$NON-NLS-1$
	}
    
    /**
     * Getter for jdependOutput
     * 
     * @return jdependOutput
     */
    public String getFileOutput() {
    	return jdependOutput;
    }

    /**
     * Setter method for projectRules.
     * 
     * @param projectRules
     */
	public void setProjectRules(ProjectRules projectRules) {
		this.projectRules = projectRules;
		firePropertyChange("projectRules", null, projectRules); //$NON-NLS-1$
	}
	
	/**
	 * Getter method for projectRules.
	 * 
	 * @return projectRules
	 */
	public ProjectRules getProjectRules() {
		return this.projectRules;
	}
	
	/**
	 * Delegate method to allow a prohibited Dependency in these projectRules.
	 * 
	 * @param allowPackages List<String[]> of to be allowed packages
	 */
	public void allowProhibitedDependency(List<String[]> allowPackages) {
		// TODO: ugly! parameter should be type safe!
		for(String[] tmp : allowPackages) {
			projectRules.allowProhibitedDependency(tmp[0], tmp[1]);
		}
		firePropertyChange("allowedDependencies", null, projectRules.getAllowedPackageRules()); //$NON-NLS-1$
		firePropertyChange("prohibitedDependencies", null, projectRules.getProhibitedPackageRules()); //$NON-NLS-1$
	}

	/**
	 * Delegate method to confirm the given dependencies.
	 * 
	 * @param updatePackages List<String[]> of to be updated packages
	 */
	public void confirmProhibitedDependency(List<String[]> updatePackages) {
		// TODO: ugly! parameter should be type safe!
		for(String[] tmp : updatePackages) {
			projectRules.confirmProhibitedDependency(tmp[0], tmp[1]);
		}
		firePropertyChange("prohibitedDependencies", null, projectRules.getProhibitedPackageRules()); //$NON-NLS-1$
	}

	/**
	 * Delegate method to prohibit an allowed Dependency in these projectRules.
	 * 
	 * @param prohibitPackages List<String[]> of to be prohibited packages
	 */
	public void prohibitAllowedDependency(List<String[]> prohibitPackages) {
		// TODO: ugly! parameter should be type safe!
		for(String[] tmp : prohibitPackages) {
			projectRules.prohibitAllowedDependency(tmp[0], tmp[1]);
		}
		firePropertyChange("allowedDependencies", null, projectRules.getAllowedPackageRules()); //$NON-NLS-1$
		firePropertyChange("prohibitedDependencies", null, projectRules.getProhibitedPackageRules()); //$NON-NLS-1$
	}

	/**
	 * Delegate method to remophe all orphaned dependencies from projectRules.
	 */
	public void removeOrphanedDependencies() {
		projectRules.removeOrphanedDependencies();
		firePropertyChange("allowedDependencies", null, projectRules.getAllowedPackageRules()); //$NON-NLS-1$
		firePropertyChange("prohibitedDependencies", null, projectRules.getProhibitedPackageRules());	 //$NON-NLS-1$
	}

	/**
     * Adds a property-change listener.
     * 
     * @param l the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l){
        if (l == null) {
            throw new IllegalArgumentException();
        }
        this.listeners.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a property change listener.
     * 
     * @param l the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l){
        this.listeners.removePropertyChangeListener(l);
    }
    
    /**
     * Notificates all listeners to a model-change
     * @param prop the property-id
     * @param old the old-instance of this JDependData
     * @param newValue the new instance of this JDependData
     */
    protected void firePropertyChange(String prop, Object old, Object newValue){
        if (this.listeners.hasListeners(prop)) {
            this.listeners.firePropertyChange(prop, old, newValue);
        }
    }

    /**
     * Sets the JDependData data model dirty and assures, that the
     * listeners are informed.
     */
	public void setDirty() {
		firePropertyChange("DATA_CHANGED", null, this); //$NON-NLS-1$
	}
}