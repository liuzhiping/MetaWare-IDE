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
package com.arc.seecode.engine.config;

import com.arc.seecode.engine.internal.config.Swahili;


/**
 * Factory for argument processing.
 * @author David Pickens
 */
public class ArgProcessorFactory {
    /**
     * Create an argument processor that is able to process
     * SeeCode arguments.
     * @return the argument processor.

     */
    public static IArgProcessor makeArgProcessor(){
        return new Swahili();  
    }
    /**
     * Return the path of the Swahili driver.
     * <P>
     * This method is used by the swahili processor
     * to process arguments but we must expose it because
     * the CDT application launcher needs to access it
     * to run an ARC application.
     * @param target the target processor (e.g., "ac", "arc", etc.).
     * @return the path of the "scac.exe" command, or 
     * null if can't be located.
     */
    public static String getSCEXE(String target){
        return Swahili.computeSCEXE(target);
        
    }
}
