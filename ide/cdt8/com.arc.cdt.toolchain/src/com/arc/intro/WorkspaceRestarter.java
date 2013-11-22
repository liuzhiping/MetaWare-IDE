/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.intro;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;


/**
 * Provides a method for restarting Eclipse within a different workspace. Too bad that Eclipse doesn't offer an API to
 * do this. We had to copy-and-paste from the Eclipse internals.
 * <p>
 * <B>NOTE:</B> this code was copied verbatim from the Eclipse 3.2.1 version of
 * <code>org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction.buildCommandLine</code>
 * <P>
 * If things stop working in a future version, you might need to re-copy-and-paste!
 * <P>
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class WorkspaceRestarter {
    
    public static final String RESTART_ID = "com.arc.RESTART";
    //
    // FROM HERE ON WE COPIED-and-PASTE from
    // org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction
    // What else can we do???

    private static final String PROP_VM = "eclipse.vm"; //$NON-NLS-1$

    private static final String PROP_VMARGS = "eclipse.vmargs"; //$NON-NLS-1$

    private static final String PROP_COMMANDS = "eclipse.commands"; //$NON-NLS-1$

    private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

    private static final String PROP_EXIT_DATA = "eclipse.exitdata"; //$NON-NLS-1$

    private static final String CMD_DATA = "-data"; //$NON-NLS-1$

    private static final String CMD_VMARGS = "-vmargs"; //$NON-NLS-1$

    private static final String NEW_LINE = "\n"; //$NON-NLS-1$
    
    private static final String CMD_PERSPECTIVE = "-perspective";
    
    private static final String C_PERSPECTIVE = "org.eclipse.cdt.ui.CPerspective";

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#dispose()
     */
    public static void restartInWorkspace (String workspacePath) {

        String command_line = buildCommandLine(workspacePath);
        if (command_line == null) {
            return;
        }

        System.setProperty(PROP_EXIT_CODE, Integer.toString(24));
        System.setProperty(PROP_EXIT_DATA, command_line);
        // Restart Eclipse.
        PlatformUI.getWorkbench().restart();
    }

    /**
     * Create and return a string with command line options for eclipse.exe that will launch a new workbench that is the
     * same as the currently running one, but using the argument directory as its workspace.
     * <P>
     * <B>NOTE:</B> this code was copied verbatim from the Eclipse 3.2.1 version of
     * <code>org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction.buildCommandLine</code>
     * <P>
     * If things stop working in a future version, you might need to re-copy-and-paste!
     * @param workspace the directory to use as the new workspace
     * @return a string of command line options or null on error
     */
    private static String buildCommandLine (String workspace) {
        String property = System.getProperty(PROP_VM);
        if (property == null) {
            MessageDialog.openError(
                PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                "Internal error",
                NLS.bind("Unable to relaunch the platform because the {0} property has not been set.", PROP_VM));
            return null;
        }

        StringBuilder result = new StringBuilder(512);
        result.append(property);
        result.append(NEW_LINE);

        // append the vmargs and commands. Assume that these already end in \n
        String vmargs = System.getProperty(PROP_VMARGS);
        if (vmargs != null) {
            result.append(vmargs);
        }

        // append the rest of the args, replacing or adding -data as required
        property = System.getProperty(PROP_COMMANDS);
        if (property == null) {
            result.append(CMD_DATA);
            result.append(NEW_LINE);
            result.append(workspace);
            result.append(NEW_LINE);
        }
        else {
            // find the index of the arg to replace its value
            int cmd_data_pos = property.lastIndexOf(CMD_DATA);
            if (cmd_data_pos != -1) {
                cmd_data_pos += CMD_DATA.length() + 1;
                result.append(property.substring(0, cmd_data_pos));
                result.append(workspace);
                result.append(property.substring(property.indexOf('\n', cmd_data_pos)));
            }
            else {
                result.append(CMD_DATA);
                result.append(NEW_LINE);
                result.append(workspace);
                result.append(NEW_LINE);
                result.append(property);
            }
        }
        // Specify that when the IDE restarts, it is in the C perspective.
        result.append(CMD_PERSPECTIVE);
        result.append(NEW_LINE);
        result.append(C_PERSPECTIVE);
        result.append(NEW_LINE);

        // put the vmargs back at the very end (the eclipse.commands property
        // already contains the -vm arg)
        if (vmargs != null) {
            result.append(CMD_VMARGS);
            result.append(NEW_LINE);
            result.append("-D" + RESTART_ID + "=1");
            result.append(NEW_LINE);
            result.append(vmargs);        
        }
           
        return result.toString();
    }
}
