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
package com.arc.seecode.engine;

/**
 * Returns the version strings pertaining to the SeeCode engine.
 * <P>
 * <B>NOTE:</B> this class is shared between the SeeCode GUI and
 * the Eclipse/SeeCode integration plugins. Don't rename unless you
 * do it in both places.
 * @author David Pickens
 */
public class Version {
    private Version(){}
    /**
     * Returns version information of the SeeCode engine "crout.dll".
     * The resulting array will have 3 elements:
     * <ul>
     * <li>The Product version (e.g. &quot;8.0.2&quot;)
     * <li>The build ID (e.g. &quot;103020061640&quot;)
     * <li>Engineering version (e.g. &quot;1.4.3&quot;)
     * </ul>
     * <P>
     * For this method to work, the engine DLL must have been previously
     * loaded.
     * @return an array of string representing version information of
     * the SeeCode engine.
     */
    public static native String[] getEngineVersionStrings();

}
