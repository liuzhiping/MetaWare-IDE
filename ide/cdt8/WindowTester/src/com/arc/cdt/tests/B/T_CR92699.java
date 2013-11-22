/*
 * T_CR92699
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
package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.ButtonLocator;


public class T_CR92699 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test the changing of a project's toolchain.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
    private static final String PROJECT_NAME = "Array"; // somewhat arbitrary
    /**
     * Tests change of toolchain and then change back.
     */
    public void testT_cr92699 () throws Exception {
    	switchToCPerspective();
        this.bringUpPropertiesDialog(PROJECT_NAME, new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Tool [cC]hain [eE]ditor"));
                EclipseUtil.setComboBox(ui,"ARCompact");

                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                IToolOptionSetting toolSetting = EclipseUtil.makeToolOptionsSettings(ui);
                CDTUtil.setCompilerOption(toolSetting, "ARC Core Version", "ARC 700");
                ui.click(computeTreeItemLocator("MetaWare ARC C\\/C++ Compiler/Processor\\/Extensions"));
                compareShellContent("T_CR92699.1");
                ui.click(new ButtonLocator("&Apply"));
                ui.wait(milliseconds(1000)); // For Progress bar to finish
                
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Tool [Cc]hain [eE]ditor"));
                EclipseUtil.setComboBox(ui,"ARCtangent 4");
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Tool [cC]hain [eE]ditor"));
                EclipseUtil.setComboBox(ui,"ARCompact");
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                ui.click(computeTreeItemLocator("MetaWare ARC C\\/C++ Compiler/Processor\\/Extensions"));
                compareShellContent("T_CR92699.2");
                              
                
            }});
        
    }

}
