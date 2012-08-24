/*******************************************************************************
 * Copyright (c) 2007 Andrei Loskutov.
 * Copyright (c) 2010 Jens Cornelis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.plugins.eclipse.depclipse.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.part.IPageBookViewPage;

import de.plugins.eclipse.depclipse.DepclipsePlugin;
import de.plugins.eclipse.depclipse.actions.ToggleJDependOutputAction;
import de.plugins.eclipse.depclipse.preferences.JDependPreferenceConstants;

/**
 * @author Andrei Loskutov
 */
public class JDependConsole extends MessageConsole  implements PropertyChangeListener{

    static JDependConsole console;

    boolean disposed;

    private static class RemoveAction extends Action {
        public RemoveAction() {
            super(Messages.JDependConsole_Close_JDepend_console, DepclipsePlugin.getDefault()
                    .getImageDescriptor(DepclipsePlugin.IMG_CLOSE));
        }

        public void run() {
            IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
            if (console != null) {
                manager.removeConsoles(new IConsole[] { console });
                console = null;
            }
        }
    }

    private JDependConsole(String name, ImageDescriptor imageDescriptor,
            boolean autoLifecycle) {
        super(name, imageDescriptor, autoLifecycle);
        DepclipsePlugin.getJDependData().addPropertyChangeListener(this);
    }

    protected void dispose() {
        if (!disposed) {
            disposed = true;
            super.dispose();
        }
        DepclipsePlugin.getJDependData().removePropertyChangeListener(this);
    }

    public static class JDependConsoleFactory implements IConsoleFactory {
        
    	public void openConsole() {
            showConsole();
        }

    }

    public static JDependConsole showConsole() {
        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
        boolean exists = false;
        if (console != null) {
            IConsole[] existing = manager.getConsoles();
            for (int i = 0; i < existing.length; i++) {
                if (console == existing[i]) {
                    exists = true;
                }
            }
        } else {
            console = new JDependConsole(Messages.JDependConsole_JDepend_Console_Title, null, true);
        }
        if (!exists) {
            manager.addConsoles(new IConsole[] { console });
        }
        manager.showConsoleView(console);
        return console;
    }

    public static void showConsole(final String fileOutput) {
        if(fileOutput == null){
            return;
        }

        final JDependConsole cons = showConsole();
        cons.clearConsole();
        
        IOConsoleOutputStream stream = cons.newMessageStream();
        
        try {
			stream.write(fileOutput);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static class JDependConsolePageParticipant implements IConsolePageParticipant {

        private RemoveAction removeAction;

        private ToggleJDependOutputAction xmlAction;

        public void activated() {
            // noop
        }

        public void deactivated() {
            // noop
        }

        public void dispose() {
            removeAction = null;
            xmlAction = null;
        }

        public void init(IPageBookViewPage page, IConsole console1) {
            removeAction = new RemoveAction();
            
            xmlAction = new ToggleJDependOutputAction(Messages.JDependConsole_Toggle_XML_Text, IAction.AS_CHECK_BOX);
            xmlAction.setImageDescriptor(DepclipsePlugin.getDefault()
                    .getImageDescriptor("asXml.gif")); //$NON-NLS-1$
            xmlAction.setChecked(DepclipsePlugin.getDefault()
                            .getPreferenceStore().getBoolean(JDependPreferenceConstants.PREF_OUTPUT_XML));
             
            IActionBars bars = page.getSite().getActionBars();
            bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP,
                    removeAction);
            bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP,
                    xmlAction);
        }

        @SuppressWarnings({ "rawtypes" })
		public Object getAdapter(Class adapter) {
            return null;
        }
    }

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("fileOutput")) { //$NON-NLS-1$
			showConsole((String) evt.getNewValue());
		}
		
	}
}
