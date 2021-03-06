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
 * Converter for usage with XMLRuleParser.
 * 
 * Output:
 * 
 * <PackageRules>
 * 		<RootPackage>NAME</RootPackage>
 * 		<DepPackage>
 * 			<Name>NAME</Name>
 * 			<Confirmed>BOOLEAN</Confirmed>
 * 			<Orphaned>BOOLEAN</Orphaned>
 * 		</DepPackage>
 * </PackageRules>
 * 
 * @author jens
 *
 */
public class ProhibitedPackageRuleConverter implements Converter {

	@Override
	public void marshal(Object clazz, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		ProhibitedPackageRule packageRule = (ProhibitedPackageRule) clazz;
		writer.startNode("PackageRule"); //$NON-NLS-1$
		writer.startNode("RootPackage"); //$NON-NLS-1$
		
		writer.setValue(packageRule.getRootPackage());
		writer.endNode();
		for(String depPackage : packageRule.getEfferentPackages()) {
			writer.startNode("DepPackage"); //$NON-NLS-1$
			writer.startNode("Name"); //$NON-NLS-1$
			writer.setValue(depPackage);
			writer.endNode();
			writer.startNode("Confirmed"); //$NON-NLS-1$
			writer.setValue(String.valueOf(packageRule.isConfirmed(depPackage)));
			writer.endNode();
			writer.startNode("Orphaned"); //$NON-NLS-1$
			writer.setValue(String.valueOf(packageRule.isOrphaned(depPackage)));
			writer.endNode();
			writer.endNode();
		}
		writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		reader.moveDown();
		ProhibitedPackageRule packageRule = new ProhibitedPackageRule(reader.getValue());
		reader.moveUp();
		while(reader.hasMoreChildren()) {
			reader.moveDown();
			reader.moveDown();
			String nameTemp = reader.getValue();
			packageRule.addEfferentPackage(nameTemp);
			reader.moveUp();
			reader.moveDown();
			packageRule.setConfirmed(nameTemp, Boolean.valueOf(reader.getValue()));
			reader.moveUp();
			reader.moveDown();
			packageRule.setOrphaned(nameTemp, Boolean.valueOf(reader.getValue()));
			reader.moveUp();
			reader.moveUp();
		}
		return packageRule;
	}

	@SuppressWarnings("rawtypes") 
	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(ProhibitedPackageRule.class);
	}

}
