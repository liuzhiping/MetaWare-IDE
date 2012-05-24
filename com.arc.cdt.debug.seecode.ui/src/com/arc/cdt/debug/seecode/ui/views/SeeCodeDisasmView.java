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

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;


/**
 * This implements the debugger Disassembly view by creating the
 * SeeCode display with disassembly.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class SeeCodeDisasmView extends SeeCodeCustomView {
    public static String DISASM_VIEW_ID = "com.arc.cdt.debug.seecode.ui.views.disasm";
    /**
     * Sends "disasm" to engine to create display.
     */
    public SeeCodeDisasmView() {
        super();
        setType("disasm");
    }
    @Override
    public void init (IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        setPartName("Disassembly"); // Get rid of "(MetaWare)" suffix.
    }
    
    @Override
    public String getTitleToolTip(){
        return "MetaWare debugger disassembly display";       
    }

}
