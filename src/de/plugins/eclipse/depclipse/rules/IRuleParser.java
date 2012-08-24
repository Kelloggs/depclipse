/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.rules;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.xml.sax.SAXParseException;


/**
 * Interface for different Implementations for datahandling of
 * ProjectRules resources
 * 
 * @author Jens Cornelis
 *
 */
public interface IRuleParser {

	/**
	 * Returns an Instance of ProjectRules with all the PackageRules defined
	 * for this Eclipse-Project. Interface can be implemented for different file
	 * or data formats.
	 * 
	 * @param filename IFile of the Resource, where the ProjectRules are saved
	 * @return ProjectRules for this Eclipse Project
	 * @throws FileNotFoundException
	 * @throws IOException 
	 * @throws CoreException 
	 * @throws SAXParseException 
	 */
	public abstract ProjectRules getProjectRules(IFile filename)
			throws FileNotFoundException, IOException, CoreException;

	/**
	 * Saves the ProjectRules to the given IFile filename.
	 * 
	 * @param filename IFile of the Resource, where the ProjectRules shall be saved
	 * @param rules ProjectRules to be saved
	 * @throws IOException
	 * @throws CoreException 
	 */
	public abstract void setProjectRules(IFile filename, ProjectRules rules)
			throws IOException, CoreException;

}