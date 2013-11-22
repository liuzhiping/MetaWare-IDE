/*
 * T_CR90191
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
import org.eclipse.ui.IViewPart;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.IConditionMonitor;
import com.windowtester.runtime.condition.IHandler;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;


public class T_CR90191_2 extends UIArcTestCaseSWT {
    private static final String TEST = "T_CR90191_2";

    public static final String DESCRIPTION = "Test terminal emulator with CMPD.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    /**
     * Test terminal simulator.
     */
    public void testT_CR90191_2() throws Exception {
        switchToDebugPerspective(); 
        this.setCanonicalSize();
        IUIContext ui = getUI();
        // We don't care if it doesn't build. The exe is already there...
        IConditionMonitor monitor = (IConditionMonitor) ui.getAdapter(IConditionMonitor.class);
        monitor.add(new ShellShowingCondition("Errors in Workspace"), new IHandler() {

            @Override
            public void handle (IUIContext ui1) throws WidgetSearchException {
                ui1.click(new ButtonLocator("Yes"));
            }
        });
        launchDebugger("UART CMPD",false);
        IViewPart views[] = new IViewPart[0];
        //Wait until the 4 terminal views materialize
        for (int i = 0; i < 80; i++) {
            ui.wait(milliseconds(250));
            views = EclipseUtil.findOpenViews(TERMINAL_VIEW_ID);
            if (views.length == 4) break;
        }
        ui.wait(milliseconds(4000)); // Wait for all processes to resume from startup.
        assertTrue(views.length == 4);
        String[] texts = new String[] {"One", "Two", "Three", "Four" };
        int i = 0;
        for (IViewPart view: views){
            EclipseUtil.makeViewVisible(view);            
            ui.wait(milliseconds(200));
            IWidgetReference ref = EclipseUtil.getViewReference(view);
            EclipseUtil.makeViewVisible(view); // in case Console has overwritten
            EclipseUtil.setFocus(ref);
            ui.wait(milliseconds(200));
            EclipseUtil.makeViewVisible(view); // in case Console has overwritten
            ui.click(ref);
            this.enterText(texts[i++] + "\n");
            ui.wait(milliseconds(300));
            EclipseUtil.makeViewVisible(view);
            this.compareWidget(TEST + "." + i, ref);
        }
        terminateDebugger();
    }
}
