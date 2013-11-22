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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tm.internal.terminal.textcanvas.ITextCanvasModel;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.eclipse.tm.terminal.model.LineSegment;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.condition.eclipse.ViewShowingCondition;


public class T_CR90191 extends UIArcTestCaseSWT {
    private static final String TEST = "T_CR90191";
    public static final String DESCRIPTION = "Test terminal simulator. (Also tests cr95397).";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    /**
     * Test terminal simulator.
     */
    public void testT_CR90191() throws Exception {
        EclipseUtil.setCanonicalSize();
        // We deliberately start in C perspective to test CR95397.
        // Before that bug was fixed, the terminal view would not show up when switching to
        // debug perspective.
        switchToCPerspective();
        IUIContext ui = getUI();
        launchDebugger("uarttest",false);
        ui.wait(new ViewShowingCondition(TERMINAL_VIEW_ID),15000);
        switchToDebugPerspective();
        IWidgetReference ref = EclipseUtil.findView(TERMINAL_VIEW_ID);
        if (ref.getWidget() instanceof IAdaptable){
        	ITextCanvasModel model = (ITextCanvasModel)((IAdaptable)ref.getWidget()).getAdapter(ITextCanvasModel.class);
        	if (model != null){
        		for (int i = 0; i < 40; i++) {
        			if (containsText(model)) break;
        			ui.wait(milliseconds(500));
        		}
        	}
        	else ui.wait(milliseconds(5000));
        }
        else
            ui.wait(milliseconds(5000)); // wait for prompt
        this.compareView(TEST + ".1", TERMINAL_VIEW_ID);
        EclipseUtil.setFocus(EclipseUtil.findView(TERMINAL_VIEW_ID));
        this.enterText("Hello!\n");
        ui.wait(milliseconds(5000));
        this.compareView(TEST + ".2", TERMINAL_VIEW_ID);
        terminateDebugger();
        ui.wait(milliseconds(2000));
        this.compareView(TEST + ".3", TERMINAL_VIEW_ID);
        switchToCPerspective();
    }
    
    private boolean containsText(ITextCanvasModel model){
    	ITerminalTextDataReadOnly t = model.getTerminalText();
    	int lines = t.getHeight();
    	int cols = t.getWidth();
    	for (int line = 0; line < lines; line++) {
    		LineSegment[] segs = t.getLineSegments(line, 0, cols);
    		if (segs.length > 1) return true;
    		if (segs.length == 1){
    			if (segs[0].getStyle() != null) return true;
    			if (segs[0].getText().length() > 0 && segs[0].getText().charAt(0) != '\0'){
    				return true;
    			}
    		}
    	}
    	return false;
    }
}
