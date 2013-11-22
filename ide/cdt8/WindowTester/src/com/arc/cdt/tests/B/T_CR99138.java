

package com.arc.cdt.tests.B;


import org.eclipse.ui.texteditor.ITextEditor;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;


public class T_CR99138 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that source display is updated correctly as we step into an inlined function located in a header file.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    /*
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp () throws Exception {
        super.setUp();
        IUIContext ui = getUI();
        ui.ensureThat(new WorkbenchLocator().hasFocus());
    }

    /**
     * Main test method.
     */
    public void testT_CR99138() throws Exception {
        IUIContext ui = getUI();
        this.launchDebugger("InlineTest.elf",true);
        this.waitUntilDebuggerStops(30000);
        this.clickStepIntoButton();
        this.pause(ui,500);
        ITextEditor edit = EclipseUtil.getActiveTextEditor();
        assertTrue(edit.getEditorInput().getName().endsWith("inline.cc"));
        this.clickStepIntoButton();
        this.pause(ui,500);
        edit = EclipseUtil.getActiveTextEditor();
        assertTrue(edit.getEditorInput().getName().endsWith("inline.h"));
        for (int i = 0; i < 9; i++) {
            this.clickStepIntoButton();
            this.pause(ui,500);
        }
        
        edit = EclipseUtil.getActiveTextEditor();
        assertTrue(edit.getEditorInput().getName().endsWith("inline.cc"));
        
        this.terminateDebugger();
    }


}