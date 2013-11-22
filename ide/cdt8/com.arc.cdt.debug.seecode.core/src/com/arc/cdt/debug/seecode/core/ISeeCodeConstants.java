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
 * Manifest constants that are used by this plugin, or by other plugins that depend on this one.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ISeeCodeConstants {

    /**
     * Preference key for the engine timeout value.
     */
    public static final String PREF_REQUEST_TIMEOUT = SeeCodePlugin.PLUGIN_ID + ".prefRequestTimeout";
    /**
     * Preference key for the engine load-program timeout value.
     */
    public static final String PREF_REQUEST_LOAD_TIMEOUT = SeeCodePlugin.PLUGIN_ID + ".prefRequestLoadTimeout";
    /**
     * Preference key for the engine launch timeout value.
     */
    public static final String PREF_REQUEST_LAUNCH_TIMEOUT = SeeCodePlugin.PLUGIN_ID + ".prefRequestLaunchTimeout";
    
    /**
     * Preference key for how to manage version inconsistencies of the bundled debugger engine and
     * the one in the toolset. That is, the "build ID" of libcrout.so.
     */
    public static final String PREF_ENGINE_VERSION_MANAGEMENT = SeeCodePlugin.PLUGIN_ID + ".prefEngineVersionManagement";
    
    /**
     * Value of {@link #PREF_ENGINE_VERSION_MANAGEMENT} to prompt user if the bundled engine is
     * older than the installed tool set version.
     */
    public static final int ENGINE_VERSION_PROMPT = 0;
    /**
     * Value of {@link #PREF_ENGINE_VERSION_MANAGEMENT} to always use bundled engine, even if it is
     * older than tool set version.
     */
    public static final int ENGINE_VERSION_USE_BUNDLED = 1;
    /**
     * Value of {@link #PREF_ENGINE_VERSION_MANAGEMENT} to always use tool set engine.
     */
    public static final int ENGINE_VERSION_USE_TOOLSET = 2;
    /**
     * Value of {@link #PREF_ENGINE_VERSION_MANAGEMENT} to always use the latest engine.
     */
    public static final int ENGINE_VERSION_USE_LATEST = 3;
    /**
     * Minimum number of seconds to wait for engine's response.
     */
    public static final int MIN_REQUEST_TIMEOUT = 1;
    /**
     * Maximum number of seconds to wait for engine's response.
     */
    public static final int MAX_REQUEST_TIMEOUT = Integer.MAX_VALUE/1000;
    /**
     * Default time in seconds to wait for engine to respond to request.
     */
    public static final int DEF_REQUEST_TIMEOUT = 30;
    /**
     * Default time in seconds to wait for engine to connect when launching debug session.
     */
    public static final int DEF_REQUEST_LAUNCH_TIMEOUT = 60;
    /**
     * Default time in seconds to wait for engine to load a program
     */
    public static final int DEF_REQUEST_LOAD_TIMEOUT = 120;
    /**
     * Default setting as to whether or not to alert user that debugger license is about to
     * expire.
     */
    public static final boolean DEF_PREF_LICENSE_EXPIRATION_ALERT = true;
    /**
     * Preference key for whether or not to alert user if debugger license is about to expire.
     */
    public static final String PREF_LICENSE_EXPIRATION_ALERT = SeeCodePlugin.PLUGIN_ID + ".prefLicenseExpirationAlert";
    /**
     * Preference key for the number of days remaining on license before we alert the user.
     */
    public static final String PREF_LICENSE_EXPIRATION_DAYS = SeeCodePlugin.PLUGIN_ID + ".prefLicenseExpirationDays";
    /**
     * Default number of days left on license before we alert him.
     */
    public static final int DEF_PREF_LICENSE_EXPIRATION_DAYS = 15;
    
    /**
     * Preference key for the maximum error log size (in bytes)
     */
    public static final String PREF_MAX_ERROR_LOG_SIZE = SeeCodePlugin.PLUGIN_ID + ".prefMaxErrorLogSize";
    
    /**
     * Default maximum error log size (in bytes).
     */
    public static final int DEF_MAX_ERROR_LOG_SIZE = 200000;
    
    public static final int MAX_ERROR_LOG_SIZE = 1024*1024*10;
    public static final int MIN_ERROR_LOG_SIZE = 1024*32;

}
