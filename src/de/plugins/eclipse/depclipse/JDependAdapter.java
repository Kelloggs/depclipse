/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Jens Cornelis - initial API and implementation
 * 			     Andrei Loskutov - some methods from original JDepend4Eclipse implementation
 *******************************************************************************/
package de.plugins.eclipse.depclipse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.xml.sax.SAXParseException;

import com.thoughtworks.xstream.converters.ConversionException;

import de.plugins.eclipse.depclipse.model.JDependData;
import de.plugins.eclipse.depclipse.model.TreeFolder;
import de.plugins.eclipse.depclipse.model.TreeLeaf;
import de.plugins.eclipse.depclipse.model.TreeObject;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;
import de.plugins.eclipse.depclipse.rules.ProjectRules;

import jdepend.framework.JDepend;
import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageComparator;

/**
 * Adapter for JDepend. 
 * 
 * @author Andrei Loskutov
 * @author Jens Cornelis
 */
public class JDependAdapter {

	/**
	 * Runs JDepend for the given Resources. Results are written to
	 * the given JDependData-Instance.
	 * 
	 * @throws IOException
	 * @throws CoreException
	 * @throws SAXParseException 
	 * @throws ConversionException
	 */
	public void startAnalyze(JDependData jdependData, IResource[] resource)
			throws IOException, CoreException, SAXParseException, ConversionException {

		IPreferenceStore preferences = DepclipsePlugin.getDefault()
				.getPreferenceStore();

		TreeFolder newRoot = new TreeFolder();
		newRoot.setIResource(resource[0]);

		String[] activeFilters = preferences.getString(
				JDependPreferenceConstants.PREF_ACTIVE_FILTERS_LIST).split(","); //$NON-NLS-1$
		jdependData.setFilters(activeFilters);

		// set the filters for this JDepend instance
		JDepend jdepend = new JDepend(jdependData.getFilter());

		jdependData.clearResourceMap();

		for (int i = 0; i < resource.length; i++) {
			TreeFolder[] tp = createTree(jdependData, resource[i]);
			for (int j = 0; j < tp.length; j++) {
				// tree object check if child exist
				if (tp[j].hasChildren()) {
					newRoot.addChild(tp[j]);
				}
			}
		}

		List<JavaPackage> packages = runJDepend(jdepend, newRoot.getChildren());
		updateCycleInfo(newRoot, packages);

		// if the preference is set to use ProhibitedDependencies functions,
		// the ProjectRules and ProhibitedDependencies are updated
		if (preferences
				.getBoolean(JDependPreferenceConstants.USE_FORBIDDEN_DEPENDENCIES)) {
			updateProhibitedDependencies(newRoot, packages);
		}

		jdependData.setRoot(newRoot);
		jdependData.setJavaPackages(packages);

		// set model dirty
		DepclipsePlugin.getJDependData().setDirty();
	}

	private void updateProhibitedDependencies(TreeFolder newRoot,
		List<JavaPackage> packages) throws IOException, CoreException, SAXParseException, ConversionException {
		// deserialize ProjectRules and update them
		ProjectRules oldRules = DepclipsePlugin
				.getProjectRules(newRoot.getIResource().getProject());
		oldRules.updateProjectRules(packages);
			
		for (JavaPackage jp : packages) {			
			TreeFolder tmpFolder = (TreeFolder) newRoot.findChild(jp);
			if (tmpFolder != null) {
				tmpFolder.checkAgainstDependencyRule(oldRules, jp
						.getEfferents());
				if(tmpFolder.getBadDependencies().size() > 0) {
					oldRules.updateProhibitedPackageRule(tmpFolder.getPackageName(), tmpFolder.getBadDependencies());
                }
				Set<JavaClass> classes = jp.getClasses();
				Map<String, JavaClass> classMap = new HashMap<String, JavaClass>();
				
				// TODO: Smells!
				for(JavaClass clazz : classes.toArray(new JavaClass[0])) {
					// Quick'n'Dirty solution for not looking at internal classes
					if(!clazz.getName().contains("$")) {
						classMap.put(clazz.getSourceFile(), clazz);
					}
				}
				
				for (TreeObject to : tmpFolder.getChildren()) {	
					// Check, if classMap contains this TreeObject/Class.
					// This caused Exception, when Package contains class-file without implementation of class
					// (e.g. if the whole class is commented).
					if(classMap.containsKey(to.getName())) {
						to.checkAgainstDependencyRule(oldRules, classMap.get(to.getName()).getImportedPackages());
					}
				}
			}
		}
		// save the Rules back to the file
		DepclipsePlugin.setProjectRules(newRoot.getIResource().getProject(), oldRules);		
	}

	private void updateCycleInfo(TreeFolder newRoot, List<JavaPackage> packages) {
		// update cycle info
		// set cycle to true, if one of packages is under the founded tree root
		for (JavaPackage jp : packages) {
			boolean cycle = jp.containsCycle();
			if (cycle) {
				if (jp.getName().length() == 0) {
					newRoot.setContainsCycle(cycle);
					continue;
				}
				TreeObject child = newRoot.findChild(jp);
				if (child != null && !child.isLeaf()) {
					((TreeFolder) child).setContainsCycle(cycle);
				}
			}
		}
	}

	/**
	 * Create object tree and initialize map with new IResource->TreeObject
	 * pairs.
	 * 
	 * @param resource
	 * @return TreeParent
	 */
	private TreeFolder[] createTree(JDependData jdependData, IResource resource) {
		if (resource.getType() != IResource.FOLDER) {
			return new TreeFolder[0];
		}
		IContainer folder = (IContainer) resource;

		IResource[] resources = null;

		try {
			resources = folder.members();
		} catch (CoreException e) {
			DepclipsePlugin.handle(e);
		}

		if (resources == null || resources.length == 0) {
			return new TreeFolder[0];
		}

		boolean isPackageRoot = false;
		IJavaElement javaElement = JavaCore.create(folder);

		if (javaElement != null) {
			try {
				isPackageRoot = TreeObject.isPackageRoot(javaElement
						.getJavaProject(), folder);
			} catch (JavaModelException e) {

			}
		} else {
			isPackageRoot = false;
		}

		if (!isPackageRoot && jdependData.resourceMapContains(folder)) {
			return new TreeFolder[0];
		}

		TreeFolder tree = (TreeFolder) jdependData.getFromResourceMap(folder);
		ArrayList<TreeFolder> treeRoots = null;
		// add parent package, if not exist
		if (tree == null) {
			tree = new TreeFolder(javaElement);
			treeRoots = new ArrayList<TreeFolder>();
			treeRoots.add(tree);
			jdependData.putToResourceMap(folder, tree);

		}
		if (treeRoots == null) {
			return new TreeFolder[0];
		}
		TreeFolder[] results;
		TreeLeaf tleaf;
		IJavaElement javaChild;
		String tname;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType() == IResource.FOLDER) {
				results = createTree(jdependData, resources[i]);
				for (int j = 0; j < results.length; j++) {
					if (!treeRoots.contains(results[j])) {
						treeRoots.add(results[j]);
					}
				}
			} else {
				tname = resources[i].getName();
				if (tname.endsWith(".java")) { //$NON-NLS-1$
					javaChild = JavaCore.create(resources[i]);
					if (javaChild == null) {
						continue;
					}
					tleaf = new TreeLeaf(javaChild);
					jdependData.putToResourceMap(resources[i], tleaf);
					tree.addChild(tleaf);
				} else if (tname.endsWith(".class")) { //$NON-NLS-1$
					tleaf = new TreeLeaf(resources[i]);
					jdependData.putToResourceMap(resources[i], tleaf);
					tree.addChild(tleaf);
				}
			}
		}

		return (TreeFolder[]) treeRoots
				.toArray(new TreeFolder[treeRoots.size()]);
	}

	private List<JavaPackage> runJDepend(JDepend jdepend,
			TreeObject[] treeObjects) {
		for (int i = 0; i < treeObjects.length; i++) {
			try {
				if (!treeObjects[i].isLeaf()) {
					TreeFolder folder = (TreeFolder) treeObjects[i];
					// add roots
					ArrayList<String> dirs = folder.getClassesLocation();
					for (int j = 0; j < dirs.size(); j++) {
						jdepend.addDirectory("" + dirs.get(j)); //$NON-NLS-1$
					}
				}
			} catch (Exception e) {
				// if directory doesn't exist, may be project need to be rebuild
				DepclipsePlugin.handle(e);
			}
		}
		final List<JavaPackage> packages = new ArrayList<JavaPackage>(jdepend
				.analyze());

		Collections.sort(packages, new PackageComparator(PackageComparator
				.byName()));
		return packages;
	}
}
