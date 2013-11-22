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
 * Callback to check if the user needs to be alerted when the debugger license
 * is soon to expire.
 */
public interface ILicenseExpirationChecker {
    /**
     * Given the number of days until the license expires, alert the user if necessary
     * with a warning box.
     * @param days days remaining until the license expires.
     */
    void checkLicenseExpiration(int days);
}
