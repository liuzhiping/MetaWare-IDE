/*
 * ICDISessionConfiguration2
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2007 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package org.eclipse.cdt.debug.core.cdi;

/**
 * Extend session configuration to include session process name.
 *<P>
 *CUSTOMIZATION
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICDISessionConfiguration2 extends ICDISessionConfiguration {
    /**
     * Return the name of the session process (e.g, "GDB Debugger Engine").
     * <P>
     * @return the name of the debugger process.
     * @throws CDIException
     */
    String getSessionProcessName();
}