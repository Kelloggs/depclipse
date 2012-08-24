/*******************************************************************************
 * Copyright (c) 2005 Andrei Loskutov.
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.plugins.eclipse.depclipse.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.plugins.eclipse.depclipse.DepclipsePlugin;

/**
 * @author Andrei
 *
 */
public class JDependPreferenceInitializer extends AbstractPreferenceInitializer {

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore store = DepclipsePlugin.getDefault().getPreferenceStore();
        store.setDefault(JDependPreferenceConstants.PREF_ACTIVE_FILTERS_LIST, "javax.*,java.*");  //$NON-NLS-1$
        store.setDefault(JDependPreferenceConstants.PREF_INACTIVE_FILTERS_LIST, "com.ibm.*,com.sun.*,org.omg.*,sun.*,sunw.*");  //$NON-NLS-1$
        store.setDefault(JDependPreferenceConstants.PREF_USE_FILTERS, true);
        store.setDefault(JDependPreferenceConstants.PREF_USE_ALL_CYCLES_SEARCH, true);
        store.setDefault(JDependPreferenceConstants.PREF_OUTPUT_XML, false);
        store.setDefault(JDependPreferenceConstants.SAVE_TO_SHOW_OPTIONS, true);
        store.setDefault(JDependPreferenceConstants.SAVE_AS_XML, true);
        store.setDefault(JDependPreferenceConstants.USE_FORBIDDEN_DEPENDENCIES, true);
        store.setDefault(JDependPreferenceConstants.FORBIDDEN_DEPENDENCIES_FILE, "DependencyRules.jdr"); //$NON-NLS-1$
    }

}
