/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 * 				 Jens Cornelis - added Methods for JDependData
 *******************************************************************************/
package de.plugins.eclipse.depclipse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import jdepend.framework.ClassFileParser;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xml.sax.SAXParseException;

import com.thoughtworks.xstream.converters.ConversionException;

import de.plugins.eclipse.depclipse.model.JDependData;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;
import de.plugins.eclipse.depclipse.rules.IRuleParser;
import de.plugins.eclipse.depclipse.rules.ProjectRules;
import de.plugins.eclipse.depclipse.rules.XMLRuleParser;

/**
 * The main plugin class to be used in the desktop. Singleton Implementation.
 * 
 * @author Andrei Loskutov
 */
public class DepclipsePlugin extends AbstractUIPlugin {
    //The shared instance.
    private static DepclipsePlugin plugin;
    public static final String ID = "de.plugins.eclipse.depclipse";  //$NON-NLS-1$
    public static final String ICON_PATH = "icons/";                         //$NON-NLS-1$
    /** Map containing preloaded ImageDescriptors */
    private Map<String, ImageDescriptor> imageDescriptors = new HashMap<String, ImageDescriptor>(13);
    public static final String IMG_REFRESH = "refresh.gif";  //$NON-NLS-1$
    public static final String IMG_CLOSE = "close.gif";  //$NON-NLS-1$
    private JDependAdapter jdepend;
    private JDependData jdependData;
    private IRuleParser ruleParser;
    
    /**
     * The constructor. 
     */
    public DepclipsePlugin() {
        super();
        DepclipsePlugin.plugin = this;
       
    }

    /** Call this method to retrieve the currently active Workbench page.
	  *
	  * @return the active workbench page.
	  */
	public static IWorkbenchPage getActivePage() {
	    IWorkbenchWindow window = getDefault().getWorkbench().getActiveWorkbenchWindow();
	    if (window == null) {
	        return null;
	    }
	    return window.getActivePage();
	}

	/**
	 * Returns the shared instance of this JDepend4EclipsePlugin.
	 */
	public static DepclipsePlugin getDefault() {
	    return plugin;
	}

	/** Call this method to retrieve the (cache) ImageDescriptor for the given id.
	  *
	  * @param id the id of the image descriptor.
	  * @return the ImageDescriptor instance.
	  */
	public ImageDescriptor getImageDescriptor(String id) {
	    ImageDescriptor imageDescriptor = (ImageDescriptor) imageDescriptors
	            .get(id);
	    if (imageDescriptor == null) {
	        imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
	                getDefault().getBundle().getSymbolicName(), ICON_PATH + id);
	        imageDescriptors.put(id, imageDescriptor);
	    }
	    return imageDescriptor;
	}

	/**
     * Returns an JDepend Instance.
     * Every time this method is called, a new Instance of JDepend
     * is created and returned.
     * 
     * @return a new Instance of JDepend
     */
    public static JDependAdapter getJDependAdapter() {
        if(getDefault().jdepend == null) {
        	JDependAdapter jdepend = new JDependAdapter();
        	getDefault().jdepend = jdepend;
        }      
        return getDefault().jdepend;
    }


    /**
     * Returns this JDependData. If not yet existing, a new JDependData
     * instance is created.
     * 
     * @return JDependData instance
     */
    public static JDependData getJDependData() {
		if(getDefault().jdependData == null) {
			getDefault().jdependData = new JDependData();
		}
		return getDefault().jdependData;
	}

	public static ClassFileParser getJDependClassFileParserInstance() {
        return new ClassFileParser(getDefault().jdependData.getFilter());
    }

    /**
	 * Deserializes and returns ProjectRules for selected Project. Creates new file, if no file existed yet. Uses
	 * the previously set instance of IRuleParser. 
	 * 
     * @throws IOException 
     * @throws CoreException 
     * @throws SAXParseException 
	 */
	public static ProjectRules getProjectRules(IProject project) throws IOException, CoreException, SAXParseException, ConversionException {	
		IRuleParser parser = getRuleParser();
		String filename = getDefault().getPreferenceStore().getString(JDependPreferenceConstants.FORBIDDEN_DEPENDENCIES_FILE);
		getJDependData().setProjectRules(parser.getProjectRules(project.getFile(filename)));
		return getJDependData().getProjectRules();
	}
	
	/**
	 * Returns the IRuleParser. Returns an XMLRuleParser if no other
	 * IRuleParser has been set
	 * 
	 * @return IRuleParser
	 */
	public static IRuleParser getRuleParser() {
		if(getDefault().ruleParser == null) {
			// up to now, there is no other parser than XML
			getDefault().ruleParser = new XMLRuleParser();
		}
		return getDefault().ruleParser;
	}

	/** 
	 * Serializes the given ProjectRules using the set implementation of IRuleParser.
	 * 
	 * @param project
	 * @param the to be serialized ProjectRules
	 * @throws IOException
	 * @throws CoreException
	 * @throws SAXParseException
	 */
	protected static void setProjectRules(IProject project, ProjectRules rules) throws IOException, CoreException, SAXParseException {	
		IRuleParser parser = getRuleParser();
		String filename = getDefault().getPreferenceStore().getString(JDependPreferenceConstants.FORBIDDEN_DEPENDENCIES_FILE);
		parser.setProjectRules(project.getFile(filename), rules);
	}

	/**
	 * Sets the given IRuleParser as the RuleParser for JDepend4Eclipse
	 * 
	 * @param ruleParser Implementation of IRuleParser to be set
	 */
	public static void setProjectRuleParser(IRuleParser ruleParser) {
		getDefault().ruleParser = ruleParser;
		
	}

	/**
     * Handles exceptions.
     *
     * @param t Exception that should be handled.
     */
    public static void handle(Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = ((InvocationTargetException) t).getTargetException();
        }

        IStatus error = null;
        if (t instanceof CoreException) {
            error = ((CoreException) t).getStatus();
        } else if (t instanceof ConversionException) {
        	error = new Status(IStatus.ERROR, DepclipsePlugin.ID, 1, "JDR-File Error", t); //$NON-NLS-1$
        }else {
            error = new Status(IStatus.ERROR, DepclipsePlugin.ID, 1, "JDepend error", t);  //$NON-NLS-1$
        }
        DepclipsePlugin.log(error);
    }

	public static void logError(Throwable t, String message) {
	    getDefault().getLog().log(new Status(IStatus.ERROR, ID, 0, message, t));
	}

	/** Call this method to log the given status.
	  *
	  * @param status the status to log.
	  */
	private static void log(IStatus status) {
	    getDefault().getLog().log(status);
	}

}
