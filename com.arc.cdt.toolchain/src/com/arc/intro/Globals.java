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


import java.io.File;


public class Globals {

    /** The relative location of the installed sample workspace, based on the install location of Eclipse. */
    public static final String SAMPLES_DIR_RELATIVE = ".." + File.separator;

    /** The name of the directory in the user's home dir where workspaces are stored. */
    public static final String USER_DATA_DIR_NAME = "mide";

    /** The name of the directory where the Samples Workspace exists. */
    public static final String SAMPLES_WORKSPACE_NAME = "Samples Workspace";
}
