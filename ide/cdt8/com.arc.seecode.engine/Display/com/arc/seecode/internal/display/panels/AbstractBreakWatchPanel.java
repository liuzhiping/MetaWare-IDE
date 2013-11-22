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

import com.arc.seecode.display.IContext;
import com.arc.seecode.display.icons.LabelsAndIcons;
import com.arc.widgets.IToolItem;

/**
 * A common base class for the Break- and Watch-point displays.
 * 
 * @author David Pickens
 */
class AbstractBreakWatchPanel extends ExtensionsPanel {

    private IContext fContext;

    /**
     *  
     */
    public AbstractBreakWatchPanel() {
        super();
    }

    protected void addStaticComponents2(String who_am_i, boolean is_watch,
            boolean is_brklist) {

        if (is_brklist) {
            makeButtonAndMenuItem("Set/remove","set_remove_button",
                    "Set/remove " + who_am_i);
            makeButtonAndMenuItem("All","Set/remove All","set_remove_all_button",
                    "Set/remove all " + who_am_i + "s shown");
        } else {
            makeButtonAndMenuItem("Remove","remove_button",
                    "Remove " + who_am_i);
            makeButtonAndMenuItem("All","Remove All","remove_all_button",
                    "Remove all " + who_am_i + "s shown");
        }
        addSeparator();
        addMenuSeparator();

        makeButtonAndMenuItem("enable_disable","enable","Enable or disable " + who_am_i +
                " on selected line");
        makeButtonAndMenuItem("All","enable","Enable or disable all " + who_am_i +"s");
        addSeparator();
        addMenuSeparator();
        
        if (!is_brklist && who_am_i.startsWith("w")) {
            
            //makeButtonAndMenuItem("Set","Set " + who_am_i, who_am_i.startsWith("b")?"show_bp":"show_wp","Bring up " + who_am_i + " dialog");
            IToolItem b = getToolBarBuilder().makeButton();
            b.setName(this.getViewer().getDisplayKind() + ".button.setwatch"); // For GUI tester
            b.setToolTipText("Set watchpoint");
            LabelsAndIcons.setButtonAttributes("Set", b, this.getWidgetFactory(),getSCDir());
            b.addObserver(new IToolItem.IObserver() {

                @Override
                public void itemChanged(IToolItem item) {
                    new WatchpointDialog(getWidgetFactory(),fContext).show();

                }        });
        }

        if (is_watch) {
            makeButtonAndMenuItem("Examine","examine_button","Examine content of selected line");
        }       
    }
    
    @Override
    public void setContext(IContext threadInfo){
        fContext = threadInfo;
    }

}
