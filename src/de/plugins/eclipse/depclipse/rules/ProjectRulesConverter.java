/*******************************************************************************
 * Copyright (c) 2010 Jens Cornelis.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor: Jens Cornelis - initial API and implementation
 *******************************************************************************/

package de.plugins.eclipse.depclipse.rules;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for usage with XMLRuleParser. Converts an instance of
 * ProjectRules to a nice and easy to read XML format.
 * 
 * This Converter takes care, that the HashMap looks nice as XML and iterates
 * over the PackageRules in this instance of ProjectRules.
 * 
 * Output:
 * <ProjectRules>
 * 		<Allowed/>
 * 		<Prohibited/ >
 * 		[...Handled in PackageRuleConverter...]
 * <ProjectRules>
 * 
 * @author Jens Cornelis
 *
 */
public class ProjectRulesConverter implements Converter {

	@Override
	public void marshal(Object clazz, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		ProjectRules rules = (ProjectRules) clazz;
		writer.startNode("JDepend4Eclipse-Version"); //$NON-NLS-1$
		writer.setValue("1.3.0-alpha"); //$NON-NLS-1$
		writer.endNode();
		writer.startNode("Allowed"); //$NON-NLS-1$
		for(AllowedPackageRule packageRule : rules.getAllowedPackageRules().values()) {
			context.convertAnother(packageRule, new AllowedPackageRuleConverter());
		}
		writer.endNode();
		
		writer.startNode("Prohibited"); //$NON-NLS-1$
		for(ProhibitedPackageRule packageRule : rules.getProhibitedPackageRules().values()) {
			context.convertAnother(packageRule, new ProhibitedPackageRuleConverter());
		}
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		ProjectRules rules = new ProjectRules();
		reader.moveDown();
		reader.moveUp();
		reader.moveDown();
		if("Allowed".equals(reader.getNodeName())) { //$NON-NLS-1$
			while(reader.hasMoreChildren()) {
				reader.moveDown();
				AllowedPackageRule packageRule = (AllowedPackageRule) context.convertAnother(rules, AllowedPackageRule.class, new AllowedPackageRuleConverter());
				try {
					rules.addAllowedPackageRule(packageRule);
				} catch (IllegalAccessException e) {			
				} 
				reader.moveUp();
			}
		}
		reader.moveUp();
			
		reader.moveDown();
		if("Prohibited".equals(reader.getNodeName())) { //$NON-NLS-1$
			while(reader.hasMoreChildren()) {
				reader.moveDown();
				ProhibitedPackageRule packageRule = (ProhibitedPackageRule) context.convertAnother(rules, ProhibitedPackageRule.class, new ProhibitedPackageRuleConverter());
				try {
					rules.addProhibitedPackageRule(packageRule);
				} catch (IllegalAccessException e) {	
				} 
				reader.moveUp();
			}
		}
		reader.moveUp();
		return rules;
	}

	@SuppressWarnings("rawtypes") 
	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(ProjectRules.class);
	}

}
