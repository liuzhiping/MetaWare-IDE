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
package com.arc.cdt.toolchain.vc;



import com.arc.cdt.toolchain.IOptionEnablementManager;
import com.arc.cdt.toolchain.OptionEnablementManager;


/**
 * Handles ARC4 options. Determines which is enabled or disabled when things are set. Also ties similar compiler,
 * assembler, and linker options together.
 *
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public class VcOptionEnablementManager extends OptionEnablementManager  {

 
    public VcOptionEnablementManager() {
        addObserver(new Observer());
        setEnabled("arc.assembler.options.endian",false);
        setEnabled("arc.compiler.options.endian",false);
        setEnabled("arc.linker.options.endian",false);
    }

    class Observer implements IOptionEnablementManager.IObserver {
        /**
         * Called when an option value changes. Enable or disable any options that are dependent on this one.
         * @param mgr
         * @param optionId
         */
        @Override
        public void onOptionValueChanged (IOptionEnablementManager mgr, String optionId) {
            if (optionId.endsWith(".version")){
                String value = (String)mgr.getValue(optionId);
                boolean isVC03 = value.endsWith(".vc03");
                boolean isVC01 = value.endsWith(".vc01");
                for (String id : getOptionIds()) {
                    if (id.endsWith("fdouble4")) {
                        setEnabled(id, isVC03);
                    }
                    else if (id.endsWith(".version") && !id.equals(optionId)){
                        mgr.set(id, value);
                    }
                }
                setEnabled("com.arc.cdt.toolchain.vc.asm.vc01",isVC01);
            }
        }

        @Override
        public void onOptionEnablementChanged (IOptionEnablementManager mgr, String optionID) {
            // Nothing to do.

        }
    }
}
