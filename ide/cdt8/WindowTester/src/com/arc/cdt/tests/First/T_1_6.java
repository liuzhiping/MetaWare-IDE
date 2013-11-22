package com.arc.cdt.tests.First;
import org.eclipse.swt.widgets.Composite;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;

public class T_1_6 extends UIArcTestCaseSWT {
    public static final String CATEGORY = INSTALLATION;
    public static final String DESCRIPTION = "Confirms that the \"About...\" boxes look okay";

	/**
	 * Checks the "About..." boxes.
	 */
	public void testT_1_6() throws Exception {
		IUIContext ui = getUI();
		ui.click(new MenuItemLocator("Help/About MetaWare IDE"));
		ui.wait(new ShellShowingCondition("About MetaWare IDE"));
		ui.click(new ButtonLocator("", 0, new SWTWidgetLocator(
				Composite.class)));
		ui.wait(new ShellShowingCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("", 0, new SWTWidgetLocator(
				Composite.class)));
		ui.wait(new ShellShowingCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("", 2, new SWTWidgetLocator(
				Composite.class)));
		ui.wait(new ShellShowingCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("", 3, new SWTWidgetLocator(
				Composite.class)));
		ui.wait(new ShellShowingCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("", 4, new SWTWidgetLocator(
				Composite.class)));
		ui.wait(new ShellShowingCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("", 5, new SWTWidgetLocator(
				Composite.class)));
		ui.wait(new ShellShowingCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE Features"));
		/*
		ui.click(new ButtonLocator("&Feature Details"));
		ui.wait(new ShellShowingCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE Features"));
		ui.click(new ButtonLocator("&Plug-in Details"));
		ui.wait(new ShellShowingCondition("About MetaWare IDE Plug-ins"));
		ui.click(new ButtonLocator("Close"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE Plug-ins"));
		ui.click(new ButtonLocator("&Configuration Details"));
		ui.wait(new ShellShowingCondition("Configuration Details"));
		ui.click(new ButtonLocator("&Close"));
		ui.wait(new ShellDisposedCondition("Configuration Details"));
		*/
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE"));
	}

}