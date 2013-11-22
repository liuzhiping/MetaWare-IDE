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


import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineGenerator;


/**
 * Overrides the class for High C compiler so that "$(CC)" macro can be used to refer to the compiler. This makes the
 * makefiles easier to port.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp </a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
@SuppressWarnings("restriction")
public class HighCCommandLineGenerator implements IManagedCommandLineGenerator {

    /**
     * {@inheritDoc}Implementation of overridden (or abstract) method.
     * @param tool
     * @param commandName
     * @param flags
     * @param outputFlag
     * @param outputPrefix
     * @param outputName
     * @param inputResources
     * @param commandLinePattern
     * @return the commandline info for invoking High C compiler.
     */
    @Override
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
        if (flags != null){
            //Quote flags with '(' in them (e.g. -Hpragma=foo(123) )
            // But know if they are already quoted (-Hpragma="foo(123")
            String newFlags[] = null;
            for (int i = 0; i < flags.length; i++){
                String flag = flags[i];
                if ((flag.indexOf('(') >= 0 || 
                        flag.indexOf(' ') >= 0) && flag.indexOf('\"') < 0){
                    if (newFlags == null){
                        newFlags = new String[flags.length];
                        System.arraycopy(flags,0,newFlags,0,flags.length);
                    }
                    newFlags[i] = "\"" + flag + "\"";
                }
            }
            if (newFlags != null) flags = newFlags;
        }

//        if (commandName.endsWith(" -c")){
//            commandName = "$(CC) -c";
//        }
//        else
//            commandName = "$(CC)";
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

}
