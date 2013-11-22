/*
 * IUIToolOptionSetter
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
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
package com.arc.cdt.testutil;

import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;


/**
 * A callback by means which a client can set and query build settings.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IUIToolOptionSetter {
    
    /**
     * This is invoked with the Build Properties dialog open and with
     * "C/C++ Build-->Settings" page showing.
     * @param ui the WindowTester context.
     * @param settings callback by which client can set and query values.
     * @throws WidgetSearchException 
     */
    public void run(IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException;

}
