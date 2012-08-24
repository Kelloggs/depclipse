/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.rules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Concrete implementations of IRuleParser for reading and writing
 * ProjectRules to an XML File. The Structure of the XML file is defined
 * in the Converters (PackageRuleConverter and ProjectRulesConverter).
 * 
 * @author Jens Cornelis
 *
 */
public class XMLRuleParser implements IRuleParser {

	/* (non-Javadoc)
	 * @see de.plugins.eclipse.depclipse.xml.IRuleParser#getRuleDefinitions(org.eclipse.core.resources.IFile)
	 */
	public ProjectRules getProjectRules(IFile filename) throws IOException, CoreException, ConversionException {
		if(!filename.exists()) {
			ProjectRules emptyRules = new ProjectRules();
			setProjectRules(filename, emptyRules);			
		}
		BufferedReader in = new BufferedReader(new FileReader(filename.getLocation().toFile()));
		XStream xstream = xstreamSetup();		
		try {
			ProjectRules retVal = (ProjectRules) xstream.fromXML(in);
			return retVal;
		} catch (ConversionException e) {
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see de.plugins.eclipse.depclipse.xml.IRuleParser#setRuleDefinition(org.eclipse.core.resources.IFile, de.plugins.eclipse.depclipse.xml.ProjectRules)
	 */
	public void setProjectRules(IFile filename, ProjectRules rules) throws IOException, CoreException {
		if(!filename.exists()) {
			File file = filename.getLocation().toFile();
			file.createNewFile();		
		}
		BufferedWriter in = new BufferedWriter(new FileWriter(filename.getLocation().toFile()));
		XStream xstream = xstreamSetup();
		String tmp =  xstream.toXML(rules);
		in.write(tmp);
		in.close();	
		filename.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	private XStream xstreamSetup() {
		XStream xstream = new XStream(new DomDriver());
		xstream.registerConverter(new ProjectRulesConverter());
		xstream.alias("ProjectRules", ProjectRules.class); //$NON-NLS-1$
		return xstream;
	}
}
