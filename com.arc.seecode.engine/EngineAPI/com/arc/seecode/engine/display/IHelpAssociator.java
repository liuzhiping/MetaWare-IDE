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

import com.arc.widgets.IWindow;

/**
 * Associate help ID with a dialog.
 */
public interface IHelpAssociator {
    /**
     * Indicate that the given dialog is associated with the given help ID.
     * @param dialog the dialog.
     * @param id the help ID.
     */
    void associateHelp(IWindow dialog, String id);
}
