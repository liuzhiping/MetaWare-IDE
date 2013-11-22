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
package com.arc.cdt.toolchain.arc4;



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

public class Arc4OptionEnablementManager extends OptionEnablementManager  {

    private static String[] XLIB_OPTIONS = {
        "arc4.compiler.options.bs",
        "arc4.compiler.options.norm",
        "arc4.compiler.options.swap",
        "arc4.compiler.options.mult32",
        "arc4.compiler.options.minmax",
        
        "arc4.asm.options.bs",
        "arc4.asm.options.norm",
        "arc4.asm.options.swap",
        "arc4.asm.options.mult32",
        "arc4.asm.options.minmax",
        
        "arc4.linker.options.bs",
        "arc4.linker.options.norm",
        "arc4.linker.options.swap",
        "arc4.linker.options.mult32",
        "arc4.linker.options.minmax",
    };
 
    public Arc4OptionEnablementManager() {
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
            if (optionId.equals("arc4.asm.options.xmac")){
                String value = (String)mgr.getValue(optionId);
                boolean v = value.endsWith(".xmac16") ||
                    value.endsWith(".xmac24") ||
                    value.endsWith(".xmacd16");
                setEnabled("arc4.compiler.options.adds",v);
                setEnabled("arc4.asm.options.adds",v);
            }
            else if (optionId.equals("arc4.compiler.options.nosdata")){
                boolean v = mgr.getValue(optionId).equals(Boolean.FALSE);
                setEnabled("arc4.compiler.options.sdata0",v);
            }
            else if (optionId.equals("arc.link.options.profiling")){
                boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
                setEnabled("arc.arc4.linker.options.xtimer",v);
            }   
            else if (optionId.endsWith(".xlib")){
                // Set "xlib" options
                boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
                if (v){
                    for (String s: XLIB_OPTIONS){
                        setOptionValue(s,true);
                        setEnabled(s,false);
                    }
                }
                else {
                    for (String s: XLIB_OPTIONS){
                        setEnabled(s,true);
                    }                   
                }
            }
        }

        @Override
        public void onOptionEnablementChanged (IOptionEnablementManager mgr, String optionID) {
            // Nothing to do.

        }
    }
}
