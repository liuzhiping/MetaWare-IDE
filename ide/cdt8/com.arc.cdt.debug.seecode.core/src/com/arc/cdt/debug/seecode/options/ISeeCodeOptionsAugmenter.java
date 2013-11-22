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
package com.arc.cdt.debug.seecode.options;

import java.util.Map;


/**
 * An object for computing the SeeCode arguments from the 
 * launch configuration and per-project build options.
 * @author David Pickens
 */
public interface ISeeCodeOptionsAugmenter {
    /**
     * Set up appropriate defaults based on settings.
     * Some defaults are dependent on property settings.
     * E.g. ARC_target defaults to ARCSIM if -av2 specified.
     */
    void augmentDefaults(Map<String,Object> propertyMap);
    enum PropState {
        DEFAULTS,    // Properties being set for first time
        LOADING,     // properties being set from persistent storage
        UPDATING,    // Properties being set from user action.
    }
    /**
     * Given a map of "guihili" properties, as derived
     * from the SeeCode launch configuration dialog, augment
     * them with any changes derived from the build configuration
     * (e.g, compiler options).
     * <P>
     * This is how such ARC compiler options as "-Xswap" or "-arc700"
     * are reflected back into the debugger's configuration.
     * @param propertyMap map of properties to be augmented.
     * @param applyingDefaults true if defaults are being applied.
     */
    void augmentProperties(Map<String,Object> propertyMap, PropState state);
    
    /**
     * Given the previously computed SeeCode engine arguments,
     * return a possibly modified list to reflect changes to
     * the build configuration since the launch was configured.
     * <P>
     * For such targets as ARC, this is how we reflect such
     * compiler options as "-arc700" into SeeCode.
     * @param arguments the originally computed arguments.
     * @return the argument list to be sent to the seecode engine
     * by augmenting the arguments with information from the
     * build configuration.
     */
    String[] augmentArguments(String arguments[]);

}
