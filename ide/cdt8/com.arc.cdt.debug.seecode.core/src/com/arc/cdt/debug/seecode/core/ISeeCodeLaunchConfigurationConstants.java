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
package com.arc.cdt.debug.seecode.core;

/**
 * Additional Launch Configuration constants that are esoteric to SeeCode.
 * @author David Pickens
 */
public interface ISeeCodeLaunchConfigurationConstants {
	/**
	 * The name under which we store the argument list to 
	 * send to the Swahili script from which we extract the
	 * engine configuration string.
	 */
	public static final String ATTR_SWAHILI_ARGS = SeeCodePlugin.getUniqueIdentifier() + ".SWAHILI_ARGS";
	/**
	 * The name under which we store the property map of the
	 * Guihili-based options dialog (debugger launch configuration).
	 */
	public static final String ATTR_GUIHILI_PROPERTIES = SeeCodePlugin.getUniqueIdentifier() + ".GUIHILI_PROPS";
    
    /**
     * The name under which we store the target CPU so that we can
     * see things change when the project's type is dynamically changed.
     */
    public static final String ATTR_TARGET_CPU = SeeCodePlugin.getUniqueIdentifier() + ".CPU";
    
    /**
     * The path of the VDK configuration file.
     */
    public static final String ATTR_VDK_CONFIG_FILE = SeeCodePlugin.getUniqueIdentifier() + "VDK_CONFIG_PATH";
    
    /**
     * The name under which we store the number of CMPD processes for a CMPD session.
     * If this attribute doesn't exist, or if its value is not greater than 1, then 
     * we do not have a CMPD session.
     */
    public static final String ATTR_CMPD_COUNT = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_COUNT";
    
    /**
     * THe name under which we store the combo history for the CMPD prelaunch command.
     */
    public static final String ATTR_CMPD_PRELAUNCH_APP_HISTORY = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_APP_HISTORY";
    /**
     * The name under which we store the combo history for the CMPD prelaunch working directory.
     */
    public static final String ATTR_CMPD_PRELAUNCH_WD_HISTORY = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_WD_HISTORY";
    
    /**
     * THe name under which we store the app for the CMPD prelaunch.
     */
    public static final String ATTR_CMPD_PRELAUNCH_APP = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_APP";
    /**
     * The name under which we store the CMPD prelaunch working directory.
     */
    public static final String ATTR_CMPD_PRELAUNCH_WD = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_WD";
    
    /**
     * The name under which we store the CMPD prelaunch delay value.
     */
    public static final String ATTR_CMPD_PRELAUNCH_DELAY = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_DELAY";
    
    /**
     * The name under which we store the CMPD prelaunch checkbox
     */
    public static final String ATTR_CMPD_PRELAUNCH = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_PRELAUNCH";
    
    
    
    /**
     * This attribute key, appended with an integer index, is the project name for the particular
     * CMPD process number, starting at 0.
     */
    public static final String ATTR_CMPD_PROJECT_NAME = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_PROJECT_NAME_";
    
    /**
     * This attribute key, appended with an integer index, is the process name for the particular
     * CMPD process number, starting at 0.
     */
    public static final String ATTR_CMPD_PROCESS_NAME = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_PROCESS_NAME_";
    
    /**
     * This attribute key, appended with an integer index, is the number of instances of
     * a particular program invocation. Index starts at 0. Newer configurations don't use this in the wake
     * of supporting the {@link #ATTR_CMPD_PROCESS_ID_LIST}.
     */
    public static final String ATTR_CMPD_PROCESS_INSTANCE_COUNT = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_PROCESS_INSTANCE_COUNT_";
    
    /**
     * This attribute key, appended with an integer index, is an encoding of the CMPD process list.
     * It replaces the {@link #ATTR_CMPD_PROCESS_INSTANCE_COUNT} attribute.
     */
    public static final String ATTR_CMPD_PROCESS_ID_LIST = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_PROCESS_ID_LIST_";
    
    /**
     * This attribute key, appended with an integer index, is the process path for the particular
     * CMPD process number, starting at 0.
     */
    public static final String ATTR_CMPD_PROCESS_PATH = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_PROCESS_PATH_";
    
    /**
     * This attribute key, appended with an integer index, is the target CPU for the particular
     * CMPD process number, starting at 0.
     */
    public static final String ATTR_CMPD_TARGET_CPU = SeeCodePlugin.getUniqueIdentifier() + ".CMPD_TARGET_CPU_";
}
