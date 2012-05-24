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
package com.arc.cdt.debug.seecode.ui.views;




/**
 * <B>NOTE</B> This class has be replaced.
 * <P>
 * This implements the debugger Register view by creating the
 * SeeCode display populated by registers.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 * @deprecated
 */
@SuppressWarnings("dep-ann")
public class SeeCodeRegView extends SeeCodeCustomView {
   
    public static String REG_VIEW_ID = "com.arc.cdt.debug.seecode.ui.views.reg";

    /**
     * Set the display type to "reg" which signals the engine to
     * populate with register table.
     */
    public SeeCodeRegView() {
        super();
        setType("reg");

    }
}
