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
package com.arc.seecode.internal.display.canvas;

import java.net.URL;

import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IImage;

/**
 * @author David Pickens
 */
class Images {
    static IImage PC_ARROW_IMAGE;

    static IImage OLDPC_ARROW_IMAGE;

    static IImage WATCH_IMAGE;

    static IImage WATCH_DISABLED_IMAGE;

    static IImage LOCK_IMAGE;

    static IImage FOCUS_IMAGE;

    static IImage BKPT_IMAGE;

    static IImage BKPT_HARDWARE_IMAGE;

    static IImage BKPT_DISABLED_IMAGE;

    static IImage BKPT_MULTIPLE_IMAGE;

    static IImage BKPT_SAME_FUNC_IMAGE;

    static IImage BKPT_SAME_FUNC_DISABLED_IMAGE;

    static IImage BKPT_THREAD_SPECIFIC_IMAGE;

    static IImage BKPT_CAN_SET;

    static IImage BKPT_OTHER_THREAD_IMAGE;

    static IImage EXPAND_IMAGE;

    static IImage CONTRACT_IMAGE;

    static IImage MARKED_IMAGE;

    static IImage FOUND_IMAGE;

    static void init(IComponentFactory f) {
        if (PC_ARROW_IMAGE == null) {

            PC_ARROW_IMAGE = makeImage(f,"pc_arrow.gif");
            OLDPC_ARROW_IMAGE = makeImage(f,"oldpc_arrow.gif");
            WATCH_IMAGE = makeImage(f,"watch.gif");
            WATCH_DISABLED_IMAGE = makeImage(f,"watch_disabled.gif");
            LOCK_IMAGE = makeImage(f,"lock.gif");
            FOCUS_IMAGE = makeImage(f,"focus.gif");

            BKPT_IMAGE = makeImage(f,"break.gif");
            BKPT_HARDWARE_IMAGE = makeImage(f,"break_hard.gif");
            BKPT_DISABLED_IMAGE = makeImage(f,"break_disabled.gif");
            BKPT_MULTIPLE_IMAGE = makeImage(f,"break_multiple.gif");
            BKPT_CAN_SET = makeImage(f,"break_can_set.gif");
            BKPT_SAME_FUNC_IMAGE = makeImage(f,"break_same_func.gif");
            BKPT_SAME_FUNC_DISABLED_IMAGE = makeImage(f,
                    "break_same_func_disabled.gif");
            BKPT_THREAD_SPECIFIC_IMAGE = makeImage(f,
                    "break_thread_specific.gif");
            BKPT_OTHER_THREAD_IMAGE = makeImage(f,"break_other_thread.gif");

            EXPAND_IMAGE = makeImage(f,"plus.gif");
            CONTRACT_IMAGE = makeImage(f,"minus.gif");
            MARKED_IMAGE = makeImage(f,"marked.gif");
            FOUND_IMAGE = makeImage(f,"found.gif");
        }
    }
    
    private static IImage makeImage(IComponentFactory f,String name){
        URL url = Images.class.getResource(name);
        if (url == null) {
            return null;
            //throw new IllegalArgumentException("Image file " + name + " is missing!");
        }
        return f.makeImage(url);       
    }
}
