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
package com.arc.seecode.engine.display;

import java.io.File;

/**
 * Given an options arg file prefix, return
 * the path of the args file and properties file.
 * @author David Pickens
 */
public interface IArgsFileLocator {
    /**
     * Given an options window file prefix, compute
     * the file where the ".args" file is to be
     * written.
     * @param prefix the per-option-window file prefix.
     * @return the file where the .args are to be written.
     */
    File computeArgsFile(String prefix);
    
    /**
     * Given an options window file prefix,
     * compute the file where the properties are to
     * be stored.
     * @param prefix the per-option-window file prefix.
     * @return the file where properties are to be stored.
     */
    File computePropertiesFile(String prefix);
}
