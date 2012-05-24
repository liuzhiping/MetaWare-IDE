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
package com.arc.mw.util;

/**
 * Interface that implements a method that the
 * update timer calls at each firing.
 *
 *@author     David Pickens
 *@created    March 12, 2002
 */
public interface ITimedUpdate {
	/**
	 * Called each time the timer fires, if active.
	 */
    void timedUpdate();
    }
