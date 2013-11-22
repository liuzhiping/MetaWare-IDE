package com.arc.cdt.tests.A;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.WidgetReference;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;

public class T_2_45 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test file/memory/fill dialog of the debugger.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_2_45() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		launchDebugger("Test1.elf",true);
		/* This uses MS Win file dialog so cannot be done at this time
		ui.click(new MenuItemLocator("Debugger/File-memory-fill operation..."));
		ui.wait(new ShellShowingCondition("File/Memory/Fill operation"));
		ui.click(new NamedWidgetLocator("file2mem"));
		ui.enterText("0x20000");
		ui.click(new NamedWidgetLocator("from_file_dialog"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("File/Memory/Fill operation"));
		ui.click(new MenuItemLocator("Debugger/File-memory-fill operation..."));
		ui.wait(new ShellShowingCondition("File/Memory/Fill operation"));
		ui.click(new NamedWidgetLocator("mem2file"));
		ui.enterText("0x10000");
		ui.enterText("0x800");
		ui.click(new NamedWidgetLocator("to_file_dialog"));
		ui.enterText("memfill.txt");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("File/Memory/Fill operation"));
	    */
		this.showSeeCodeView("Memory");
		this.enterTextWithinNamedWidget("mem.combo.data_addr_eval", "0x16000", false);
		EclipseUtil.activateEclipseWindow(); // Windows is periodically losing focus of Eclipse frame
		ui.click(new XYLocator(new WidgetReference<Object>(ui.getActiveWindow()),400,40)); // Hack to get around Windows Bug
		ui.click(new MenuItemLocator("Debugger/File-memory-fill operation..."));
		ui.wait(new ShellShowingCondition("File/Memory/Fill operation"));
		ui.click(new NamedWidgetLocator("fillmem"));
		ui.click(new NamedWidgetLocator("fillmem_addr"));
		ui.keyClick(WT.CTRL,'A');
		ui.enterText("0x16000");
		ui.click(new NamedWidgetLocator("fillmem_len"));
		ui.keyClick(WT.CTRL,'A');
		ui.enterText("0x200");
		ui.click(new NamedWidgetLocator("f_1_byte"));
		ui.click(new NamedWidgetLocator("f_byte_value"));
		ui.keyClick(WT.CTRL,'A');
		ui.enterText("0xFE");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("File/Memory/Fill operation"));
		ui.wait(milliseconds(200));
		checkMemory();
		ui.click(new MenuItemLocator("Debugger/File-memory-fill operation..."));
		ui.wait(new ShellShowingCondition("File/Memory/Fill operation"));
		ui.click(new NamedWidgetLocator("f_2_byte"));
		ui.click(new NamedWidgetLocator("f_byte_value"));
		ui.keyClick(WT.CTRL,'A');
		ui.enterText("0xCAFE");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("File/Memory/Fill operation"));
		ui.wait(milliseconds(200));
		checkMemory();
		ui.click(new MenuItemLocator("Debugger/File-memory-fill operation..."));
		ui.wait(new ShellShowingCondition("File/Memory/Fill operation"));
		ui.click(new NamedWidgetLocator("f_4_byte"));
		ui.click(new NamedWidgetLocator("f_byte_value"));
		ui.keyClick(WT.CTRL,'A');
		ui.enterText("0xDEADBEEF");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("File/Memory/Fill operation"));
		ui.wait(milliseconds(200));
		checkMemory();
		ui.click(new MenuItemLocator("Debugger/File-memory-fill operation..."));
		ui.wait(new ShellShowingCondition("File/Memory/Fill operation"));
		ui.click(new NamedWidgetLocator("f_hex_string"));
		ui.click(new NamedWidgetLocator("f_string_value"));
		ui.keyClick(WT.CTRL,'A');
		ui.enterText("abcd1234");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("File/Memory/Fill operation"));
		ui.wait(milliseconds(200));
		checkMemory();
		ui.click(new MenuItemLocator("Debugger/File-memory-fill operation..."));
		ui.wait(new ShellShowingCondition("File/Memory/Fill operation"));
		ui.click(new NamedWidgetLocator("f_string"));
		ui.click(new NamedWidgetLocator("f_string_value"));
		ui.keyClick(WT.CTRL,'A');
		ui.enterText("\"Why is a mouse when it spins?\"");
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("File/Memory/Fill operation"));
		ui.wait(milliseconds(200));
		checkMemory();
		terminateDebugger();
	    switchToCPerspective();
	}
	
	private int instance = 0;
	
	private void checkMemory() throws WidgetSearchException{
		this.compareSeeCodeView("T_2_45." + ++instance, "mem#1");
	}

}