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
package com.arc.cdt.toolchain.arm;



import com.arc.cdt.toolchain.IOptionEnablementManager;
import com.arc.cdt.toolchain.OptionEnablementManager;


/**
 * Handles ARM options. Determines which is enabled or disabled when things are set. Also ties similar compiler,
 * assembler, and linker options together.
 *
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public class ArmOptionEnablementManager extends OptionEnablementManager  {

 
    public ArmOptionEnablementManager() {
        addObserver(new Observer());
    }

    class Observer implements IOptionEnablementManager.IObserver {
        /**
         * Called when an option value changes. Enable or disable any options that are dependent on this one.
         * @param mgr
         * @param optionId
         */
        @Override
        public void onOptionValueChanged (IOptionEnablementManager mgr, String optionId) {
            if (optionId.equals("arm.compiler.options.target.version")){
                String value = (String)mgr.getValue(optionId);
                boolean v = !value.endsWith(".v3") &&
                    !value.endsWith(".v3m") ;
                setEnabled("com.arc.cdt.toolchain.arm.thumb",v);
                setEnabled("com.arc.cdt.toolchain.arm.inter",v);
                setEnabled("arm.link.options.thumb",v);
            }       
        }

        @Override
        public void onOptionEnablementChanged (IOptionEnablementManager mgr, String optionID) {
            // Nothing to do.

        }
    }
}
