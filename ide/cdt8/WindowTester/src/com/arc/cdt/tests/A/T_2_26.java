/*
 * T_1_1
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
package com.arc.cdt.tests.A;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;

public class T_2_26 extends UIArcTestCaseSWT {
	public static final String CATEGORY = DEBUGGER_INTEGRATION;
	public static final String DESCRIPTION = "Verify that \"Select from one...\" dialog works from SeeCode's dissassembly, Memory, and Examine displays (automatable?)";

	public void testT_2_26() throws Exception {
		this.launchDebugger("Queens_AC.elf", true);
		IUIContext ui = getUI();		
		this.waitUntilDebuggerStops(10000);
		this.toggleInstructionStepMode();
		ui.click(new NamedWidgetLocator("disasm.combo.code_addr"));
		ui.enterText("m");
		ui.keyClick(WT.CR);
		ui.wait(new ShellShowingCondition("Select one, please..."),3000);
		EclipseUtil.clickListItem(ui,1,"memcmp \\[0x.*\\]");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Select one, please..."));
		
		this.showSeeCodeView("Memory");
		ui.wait(milliseconds(300));
		ui.click(2,EclipseUtil.findWidgetWithName(ui,"mem.combo.data_addr_eval"));
		ui.enterText("e");
		ui.keyClick(WT.CR);
		ui.wait(new ShellShowingCondition("Select one, please..."),3000);
		EclipseUtil.clickListItem(ui,2,"errno \\[0x.*\\]");
		ui.wait(new ShellDisposedCondition("Select one, please..."));
		
		this.showSeeCodeView("Examine");
		ui.wait(milliseconds(300));
		ui.click(2,new NamedWidgetLocator("examine.combo.examine_expr"));
		ui.enterText("e");
		ui.keyClick(WT.CR);
		ui.wait(new ShellShowingCondition("Select one, please..."),3000);
		EclipseUtil.clickListItem(ui,1,"errno \\[0x.*\\]");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("Select one, please..."));
		terminateDebugger();
	}

}
