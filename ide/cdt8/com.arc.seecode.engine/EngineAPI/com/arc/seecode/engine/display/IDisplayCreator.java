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

import com.arc.seecode.display.ISeeCodeTextViewer;

/**
 * Callback that is passed to {@link SeeCodeTextViewerCallback}
 * to create a display.
 * @author David Pickens
 */
public interface IDisplayCreator {
    /**
     * Create a display.
     * @param kind the kind of display.
     * @param viewer an existing viewer that the new
     * display should somehow be associated  (e.g., be placed
     * in the same frame).
     */
    void createDisplay(String kind, ISeeCodeTextViewer viewer);
    
    /**
     * If a display of the given kind exists, bring it to top and in focus; otherwise,
     * create it.
     * @param kind
     */
    void activateDisplay(String kind);

}
