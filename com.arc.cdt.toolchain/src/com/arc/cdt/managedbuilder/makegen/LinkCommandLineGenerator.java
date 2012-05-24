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
package com.arc.cdt.managedbuilder.makegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;


/**
 * Overrides the linker command line generator so that we can
 * refer to the link step with the "$(LINK)" macro instead of the
 * absolute command.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
@SuppressWarnings("restriction")
public class LinkCommandLineGenerator implements IManagedCommandLineGenerator {

   //private String linkCommand = null;
    private List<String> dependencies = new ArrayList<String>(0);

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param tool
     * @param commandName
     * @param flags
     * @param outputFlag
     * @param outputPrefix
     * @param outputName
     * @param inputResources
     * @param commandLinePattern
     * @return the command info for invoking High C linker via the compiler
     * driver.
     */
    public IManagedCommandLineInfo generateCommandLineInfo (
            ITool tool,
            String commandName,
            String[] flags,
            String outputFlag,
            String outputPrefix,
            String outputName,
            String[] inputResources,
            String commandLinePattern) {
        IManagedCommandLineGenerator gen = ManagedCommandLineGenerator.getCommandLineGenerator();
//        if (commandName.equals(linkCommand))
//            commandName = "$(LINK)";
        if (inputResources == null) inputResources = new String[0];  // CDT 4.0 calls this will null inputResources
        // Append libraries from other projects.
        List<String> inputs = new ArrayList<String>(inputResources.length+this.dependencies.size());
        inputs.addAll(Arrays.asList(inputResources));
        if (this.dependencies.size() > 0) {                      
            // PLace dependencies before $(LIBS) in case the dependent
            // project rely on these libraries
            int insertIndex = inputs.indexOf("$(LIBS)");
            if (insertIndex < 0) insertIndex = inputs.size();
            for (String s: this.dependencies) {
                if (s.toLowerCase().endsWith(".lib") ||
                    s.toLowerCase().endsWith(".a") ||
                    s.toLowerCase().endsWith(".dll") ||
                    s.toLowerCase().endsWith(".so"))
                    inputs.add(insertIndex++,s);
            }
        }
//        if (inputs.indexOf("$(USER_OBJS)") < 0 )
//            // Don't know why GNU make generator doesn't add these to linker input:
//            inputs.add("$(USER_OBJS)");
//        if (inputs.indexOf("$(LIBS)") < 0 )
//            inputs.add("$(LIBS)");
        inputResources = inputs.toArray(new String[inputs.size()]);
        return gen.generateCommandLineInfo(
                tool,
                commandName,
                flags,
                outputFlag,
                outputPrefix,
                outputName,
                inputResources,
                commandLinePattern);
    }
    
    /**
     * Called from the constructor to HighCMakefileGenerator to
     * inform us of what command has been set to "$(LINK)" macro.
     * @param linkCommand
     */
    public void setLinkCommand(String linkCommand){
        //this.linkCommand = linkCommand;
    }
    
    public void setDependencyOutputs(List<String> dependencies){
        this.dependencies = new ArrayList<String>(dependencies);
    }

}
