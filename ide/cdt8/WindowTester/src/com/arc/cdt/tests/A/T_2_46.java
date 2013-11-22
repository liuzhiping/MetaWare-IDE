package com.arc.cdt.tests.A;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.eclipse.swt.widgets.Scrollable;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;

public class T_2_46 extends UIArcTestCaseSWT {
	public static final String CATEGORY = DEBUGGER_INTEGRATION;
	public static final String DESCRIPTION = "Verify that Memory Search view works";

	/**
	 * Main test method.
	 */
	public void testT_2_46() throws Exception {
		IUIContext ui = getUI();
		EclipseUtil.setMaximizedForActiveShell(ui, true);
		// Maximize so that memory search display has enough width to set
		// the fields properly.
		try {
			launchDebugger("Queens_AC.elf", true);
			this.waitUntilDebuggerStops(15000);
			ui.click(new MenuItemLocator("Debugger/Search memory"));
			IWidgetReference scroller = (IWidgetReference) ui
					.find(new NamedWidgetLocator("memsearch.scroller"));
			enterComboText("memsearch.address", "0x10000");
			//EclipseUtil.scrollRight(ui, scroller);
			enterComboText("memsearch.length", "0x10000");
			EclipseUtil.resetHorizontalScroller(ui, (Scrollable) scroller
					.getWidget());
			ui.click(new ButtonLocator("Char string"));
			enterComboText("memsearch.a_char_string", "Size too big");
			ui.wait(milliseconds(500));
			String console1 = this.getDebuggerConsoleContent();
			ui.click(new NamedWidgetLocator("memsearch.search"));
			ui.wait(milliseconds(500));
			String console2 = this.getDebuggerConsoleContent();
			Assert.assertTrue(console2.length() > console1.length());
			String newStuff = console2.substring(console1.length());
			Assert.assertTrue(endsWithExpectedStuff(newStuff));

			ui.click(new ButtonLocator("Integer value"));
			enterComboText("memsearch.value", "0x64656d75");
			ui.click(new NamedWidgetLocator("memsearch.search"));
			ui.wait(milliseconds(500));
			String console3 = this.getDebuggerConsoleContent();
			Assert.assertTrue(console3.length() > console2.length());
			newStuff = console3.substring(console2.length());
			Assert.assertTrue(endsWithExpectedStuff(newStuff));

			//EclipseUtil.scrollRight(ui, scroller);
			ui.click(new ButtonLocator("Hex string"));
			this.pause(ui,500);
			enterComboText("memsearch.a_hex_string", "3b202564");

			EclipseUtil.resetHorizontalScroller(ui, (Scrollable) scroller
					.getWidget());
			ui.click(new NamedWidgetLocator("memsearch.search"));
			ui.wait(milliseconds(500));
			String console4 = this.getDebuggerConsoleContent();
			Assert.assertTrue(console4.length() > console3.length());
			newStuff = console4.substring(console3.length());
			Assert.assertTrue(endsWithExpectedStuff(newStuff));
			this.terminateDebugger();
		} finally {
			EclipseUtil.setMaximizedForActiveShell(ui, false);
		}
	}

	private boolean endsWithExpectedStuff(String newStuff) {
		Matcher matcher = Pattern
				.compile(
						"(Process started)?Searching .*starting at 0x10000\\.\\nFound at 0x.*\\n")
				.matcher(newStuff);
		if (!matcher.matches()) {
			this.writeStringToFile("T_2_46.txt", newStuff);
		}
		return matcher.matches();
	}

	private void enterComboText(String name, String text)
			throws WidgetSearchException {
		IWidgetReference ref = EclipseUtil.findWidgetWithName(getUI(), name);
		getUI().click(ref);
		getUI().keyClick(WT.CTRL, 'a');
		getUI().enterText(text);
	}

}