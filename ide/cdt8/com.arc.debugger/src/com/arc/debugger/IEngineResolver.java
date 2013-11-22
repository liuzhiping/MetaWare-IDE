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
package com.arc.debugger;

/**
 * Callback interface that is called when it has been determined that
 * the bundled debugger engine is a an earlier version that the
 * tool set debugger engine.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IEngineResolver {
    
    /**
     * Called when it is discovered that the tool set debugger engine is
     * later than the bundled one. If it returns true, the tool set engine
     * will be used; otherwise the bundled one.
     * @param bundledEngineId the build ID of the bundled engine.
     * @param toolsetEngineId the build ID of the tool set engine.
     * @param toolsetPath the path to the engine DLL in the tool set.
     * @return true if the tool set engine is to be used; false if the bundled engine is to be used.
     */
    boolean useToolSetEngine(int bundledEngineId, int toolsetEngineId, String toolsetPath);

}
