

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;


public class T_CR93983 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that arrow keys, page-up, page-down, home, and end keys work in MetaWare displays";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "Queens_AC.elf";

    /**
     * Main test method.
     */
    public void testT_CR93983 () throws Exception {
       
        switchToDebugPerspective();
        this.launchDebugger(LAUNCH_NAME,false);
        IUIContext ui = getUI();       
        this.waitUntilDebuggerStops(20000);
        
        this.showSeeCodeView("Source");
        
        this.clearProfilingColumns("source");
        
        ui.click(EclipseUtil.findView(SEECODE_VIEW_ID,"source"));
        
        this.compareSeeCodeView("T_CR93983.0","source");
        
        ui.keyClick(WT.END);
        ui.wait(milliseconds(500));
        this.compareSeeCodeView("T_CR93983.1","source");
        
        ui.keyClick(WT.HOME);
        ui.wait(milliseconds(300));
        this.compareSeeCodeView("T_CR93983.2","source");
        
        for (int i = 0; i < 10; i++){
            ui.keyClick(WT.ARROW_DOWN);
            ui.wait(milliseconds(200));
        }
        ui.wait(milliseconds(300));
        this.compareSeeCodeView("T_CR93983.3","source");
        
        ui.keyClick(WT.PAGE_DOWN);
        ui.wait(milliseconds(300));
        ui.keyClick(WT.PAGE_DOWN);
        ui.wait(milliseconds(300));
        
        this.compareSeeCodeView("T_CR93983.4","source");
        
        for (int i = 0; i < 10; i++) {
            ui.keyClick(WT.ARROW_RIGHT);
            ui.wait(milliseconds(100));
        }
        
        this.compareSeeCodeView("T_CR93983.5","source");
        
        for (int i = 0; i < 5; i++) {
            ui.keyClick(WT.ARROW_LEFT);
            ui.wait(milliseconds(100));
        }
        this.compareSeeCodeView("T_CR93983.6","source");
        
        for (int i = 0; i < 5; i++) {
            ui.keyClick(WT.ARROW_LEFT);
            ui.wait(milliseconds(100));
        }
        ui.keyClick(WT.PAGE_UP);
        ui.wait(milliseconds(300));
        ui.keyClick(WT.PAGE_UP);
        ui.wait(milliseconds(300));
        this.compareSeeCodeView("T_CR93983.7","source");
        
        ui.keyClick(WT.END);
        ui.wait(milliseconds(400));
        for (int i = 0; i < 10; i++){
            ui.keyClick(WT.ARROW_UP);
            ui.wait(milliseconds(200));
        }
        this.compareSeeCodeView("T_CR93983.8","source");
        
        this.terminateDebugger();
        
    }

}