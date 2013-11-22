/*
 * T_CMPD
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


package com.arc.cdt.testutil;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.widgets.internal.swt.TextColumn;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WaitTimedOutException;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.ICondition;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

/**
 * Subclassed by tests that run graphics pipeline with ISS and with XISS.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public abstract class T_CMPD extends UIArcTestCaseSWT {

    public T_CMPD() {
        super();
    }
    
    protected abstract String getLaunchName();

    /**
     * Tests CMPD commands from command line.
     */
    public void test_CMPD () throws Exception {
        IUIContext ui = getUI();
        ICondition errorDialog = new ShellShowingCondition("Errors in Workspace");
        
        this.switchToDebugPerspective();
        this.launchDebugger(getLaunchName(), false);
        
        try {
            ui.wait(errorDialog,3000,300);
            ui.click(new ButtonLocator("Yes"));
        } catch(WaitTimedOutException x){
        	//Project must have built okay!
        }
    
    
        this.showView(COMMAND_VIEW_ID);
        ui.wait(milliseconds(1000));
        clickLaunch();
        this.showSeeCodeView("Image");
        
        
        ui.contextClick(new SWTWidgetLocator(TextColumn.class, new ViewLocator(
            "com.arc.cdt.debug.seecode.ui.views.SeeCodeCustomView")), "Configure/Reload prior parameters");
        clickLaunch();
        assertProcessState("grmain",true);
        assertProcessState("displaymain",true);
        assertProcessState("rendermain0",true);
        assertProcessState("rendermain1",true);
        
        clickLaunch();
        this.enterDebugCommandLine("stop");
        assertProcessState("grmain",false);
        assertProcessState("displaymain",false);
        assertProcessState("rendermain0",false);
        assertProcessState("rendermain1",false);      
        
        this.enterDebugCommandLine("[3]run");
        
        assertProcessState("grmain",false);
        assertProcessState("displaymain",false);
        assertProcessState("rendermain0",true);
        assertProcessState("rendermain1",false);
        
        clickLaunch();
        this.clickResumeButton();
        assertProcessState("grmain",true);
        assertProcessState("displaymain",true);
        assertProcessState("rendermain0",true);
        assertProcessState("rendermain1",true);
        
        ui.click(computeProcessLocator("displaymain"));
        this.clickStopButton();
        assertProcessState("grmain",true);
        assertProcessState("displaymain",false);
        assertProcessState("rendermain0",true);
        assertProcessState("rendermain1",true);
        
        this.enterDebugCommandLine("[4]stop");
        assertProcessState("grmain",true);
        assertProcessState("displaymain",false);
        assertProcessState("rendermain0",true);
        assertProcessState("rendermain1",false);
        
        ui.click(computeProcessLocator("displaymain"));
        this.clickResumeButton();
        assertProcessState("grmain",true);
        assertProcessState("displaymain",true);
        assertProcessState("rendermain0",true);
        assertProcessState("rendermain1",false);
        
        clickLaunch();
        this.enterDebugCommandLine("[3]stop");
        assertProcessState("grmain",true);
        assertProcessState("displaymain",true);
        assertProcessState("rendermain0",false);
        assertProcessState("rendermain1",false);
        
        clickLaunch();
        this.enterDebugCommandLine("run");
        assertProcessState("grmain",true);
        assertProcessState("displaymain",true);
        assertProcessState("rendermain0",true);
        assertProcessState("rendermain1",true);
        
        this.clickTerminateButton();
    
    }

    private void clickLaunch () throws WidgetSearchException {
        getUI().click(new TreeItemLocator(getLaunchName() + " [C\\/C++ Multiprocess Application]", new ViewLocator(DEBUG_VIEW_ID)));
    }

    private void assertProcessState (String processName, boolean running) throws WidgetSearchException {
        IWidgetLocator ref = computeProcessLocator(processName);
        
        boolean suspended = running;
        String text = "";
        for (int i = 0; i < 10 && suspended == running; i++) {
            text = EclipseUtil.getText(ref);
            suspended = (text.indexOf("(Suspended)") > 0);
            // May take awhile for state to change
            if (suspended == running){
                getUI().wait(milliseconds(500));
            }
        }
        if (suspended == running){
            System.out.println("!!!Node for \"" + processName + "\" is \"" + text + "\"");
            Assert.fail("Node for \"" + processName + "\" is \"" + text + "\"");
        }
    }

    private IWidgetLocator computeProcessLocator (String processName) throws WidgetSearchException {
        return this.computeTreeItemLocator(getLaunchName()+".*/"+processName + ".*");
    }

}