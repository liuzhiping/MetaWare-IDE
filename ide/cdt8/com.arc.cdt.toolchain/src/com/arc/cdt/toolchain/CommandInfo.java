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
package com.arc.cdt.toolchain;

import java.io.File;


/**
 * A method for determining if a command exists under JRE 1.5 or later.
 * <P>
 * CUSTOMIZATION
 * <P>
 * @author David Pickens
 */
public class CommandInfo {

    /**
     * Return whether or not a command exists.
     * <P>
     * Called to determine if a toolchain is supported.
     * @param cmd the command
     * @return whether or not a command exists.
     */
    public static boolean commandExists (String cmd) {
        // There may be arguments so only grab up to the whitespace
        if (cmd.indexOf(' ') > 0) {
            cmd = cmd.substring(0, cmd.indexOf(' '));
        }
        if (isWindows() && !cmd.toLowerCase().endsWith(".exe"))
            cmd = cmd + ".exe";
        File f = new File(cmd);
        if (f.isAbsolute())
            return f.exists();
        String path = System.getenv("PATH");
        if (path == null)
            return true; // punt
        String paths[] = path.split(File.pathSeparator);
        for (String p : paths) {
            if (new File(p, cmd).isFile())
                return true;
        }
        return false;
    }
    
    /**
     * Determine whether or not we're running on Microsoft Windows.
     * @return true if we're running under Microsoft Windows.
     */
    public static boolean isWindows(){
        return System.getProperty("os.name").indexOf("indow") > 0;
    }

}
