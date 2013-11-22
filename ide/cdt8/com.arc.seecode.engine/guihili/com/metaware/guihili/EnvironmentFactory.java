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
package com.metaware.guihili;

import java.io.File;

import com.metaware.guihili.builder.Environment;

/**
 * Factory for making a Guihili environment.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class EnvironmentFactory {
    /**
     * Create an environment for guihili.
     * @param workingDir working directory from which relative paths are computed.
     * @param env replacement environment for the OS environment strings; or <code>null</code>
     * if client's OS enviroment is to be used.
     * @return new environment.
     */
    public static IEnvironment create(File workingDir, String env[]){
        return Environment.create(workingDir,env);
    }
}
