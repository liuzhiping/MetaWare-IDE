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
package com.arc.seecode.internal.display.panels;


/**
 * The toolbar of a SeeCode Hardware Stack display. It is almost
 * identical to a memory display, so we merely subclass it.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class HwstackPanel extends MemPanel {

    /**
     * Invoked by reflection.
     * 
     */
    public HwstackPanel() {
        super();
    }

    @Override
    public void finish () {
        this.makeButtonAndMenuItem("Change","change","Change value at selected address");
    }
}
