

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.ILocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.XYLocator;


public class T_CR94955 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test profiling counters can be dynamically renamed.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String PROJECT = "Queens_AC";

    private static final String LAUNCH_NAME = PROJECT + ".elf hwprof";

    /**
     * Main test method.
     */
    public void testT_CR94955 () throws Exception {

        switchToCPerspective();

        // Must be built for ARC 700
        this.bringUpBuildSettings(PROJECT, new IUIToolOptionSetter() {

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                CDTUtil.setCompilerSwitch("-arc700", settings, true);
            }
        });
        
        IUIContext ui = getUI();

        this.buildProject(PROJECT);
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH_NAME, false);
        this.waitUntilDebuggerStops(20000);
        this.showSeeCodeView("Source");
        this.showSeeCodeView("Command-line input");
       
        //this.showSeeCodeView("Profiling"); // required because of current bug in Tom's implementation


        IWidgetReference srcView = EclipseUtil.findView(SEECODE_VIEW_ID, "source");
        Point size = EclipseUtil.getSize(srcView);
        ILocator contextLoc = new XYLocator(srcView,size.x*5/6,size.y/2);
        
        ui.contextClick(contextLoc, "Profiling"); // Force creation of menu

        final Menu popupMenu = EclipseUtil.getPopupMenu(srcView);
        if (popupMenu == null) {
            EclipseUtil.dumpControl((Control)srcView.getWidget());
            Assert.assertTrue(false);
            return;
        }

        final List<String> selectedItems = new ArrayList<String>();
        popupMenu.getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                for (MenuItem item : popupMenu.getItems()) {
                    if ("Profiling".equals(item.getText())) {
                        for (MenuItem profItem : item.getMenu().getItems()) {
                            if (profItem.getSelection()) {
                                selectedItems.add(profItem.getText());
                            }
                        }
                        break;
                    }
                }

            }
        });
        ui.keyClick(WT.ESC);
        ui.wait(milliseconds(200));
        ui.keyClick(WT.ESC);
        for (String item : selectedItems) {
            ui.contextClick(contextLoc, "Profiling/" + item);
        }
        ui.contextClick(contextLoc, "Profiling"); // Force creation of menu

        final Menu popupMenu2 = EclipseUtil.getPopupMenu(srcView);
        Assert.assertTrue(popupMenu2 != null);
        if (popupMenu2 == null) return; // Keep compiler happy
        this.compareWidget("T_CR94955.1", popupMenu2, "Source view popup after clearing");


        selectedItems.clear();
        popupMenu2.getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                for (MenuItem item : popupMenu2.getItems()) {
                    if ("Profiling".equals(item.getText())) {
                        int cnt = 0;
                        for (MenuItem profItem : item.getMenu().getItems()) {
                            if (cnt > 6 && cnt % 2 == 1){
                                selectedItems.add(profItem.getText());
                            }
                            if (cnt > 13) break;
                            cnt++;
                        }
                        break;
                    }
                }
            }
        });
        ui.keyClick(WT.ESC);
        ui.wait(milliseconds(200));
        ui.keyClick(WT.ESC);
        ui.wait(milliseconds(200));
        
        for (String item : selectedItems) {
            ui.contextClick(contextLoc, "Profiling/" + item);
        }
        
        this.showSeeCodeView("Hardware/Hardware profiler *");
        
        ui.contextClick(contextLoc, "Profiling"); // Force creation of menu

        Menu popupMenu3 = EclipseUtil.getPopupMenu(srcView);
        Assert.assertTrue(popupMenu3 != null);
        ui.keyClick(WT.ESC);
        ui.wait(milliseconds(200));
        ui.keyClick(WT.ESC);
        
        this.compareWidget("T_CR94955.2", popupMenu3, "Source view popup after selection");
        this.compareWidget("T_CR94955.3", srcView,"Source after selection");
        
        this.enterDebugCommandLine("read prof_setup2");
        ui.wait(milliseconds(1000));
        
        ui.contextClick(contextLoc, "Profiling"); // Force creation of menu
        Menu popupMenu4 = EclipseUtil.getPopupMenu(srcView); // May have changed.
        Assert.assertTrue(popupMenu4 != null);
        ui.keyClick(WT.ESC);
        ui.wait(milliseconds(200));
        ui.keyClick(WT.ESC);
        this.compareWidget("T_CR94955.4",popupMenu4,"Popup after recomputing");
        this.compareWidget("T_CR94955.5", srcView,"Source after recomputing");
        
        this.terminateDebugger();

    }

}