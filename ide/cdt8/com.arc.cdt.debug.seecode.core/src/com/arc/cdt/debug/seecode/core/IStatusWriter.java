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
 * A callback interface supplied by the UI package
 * to write the status line.
 * @author David Pickens
 */
public interface IStatusWriter {
    /**
     * Write the status line.
     * @param msg the message to be reported.
     */
    void setStatus(String msg);
}
