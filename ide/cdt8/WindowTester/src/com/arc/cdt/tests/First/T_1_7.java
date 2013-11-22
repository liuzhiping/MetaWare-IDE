package com.arc.cdt.tests.First;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.TabItemLocator;

public class T_1_7 extends UIArcTestCaseSWT {
    public static final String CATEGORY = INSTALLATION;
    public static final String DESCRIPTION = "Confirm that JRE 1.6 is being accessed.";

	/**
	 * Confirms that JRE 1.6 is being used.
	 */
	public void testT_1_7() throws Exception {
		IUIContext ui = getUI();
		ui.click(new MenuItemLocator("Help/About MetaWare IDE"));
		ui.wait(new ShellShowingCondition("About MetaWare IDE"));
		
		ui.click(new ButtonLocator("Installation Details"));
		ui.wait(new ShellShowingCondition("MetaWare IDE Installation Details"));
		
		ui.click(new TabItemLocator("Configuration"));
		
		//ui.click(new ButtonLocator("&Configuration Details"));
		//ui.wait(new ShellShowingCondition("Configuration Details"));
		
		ui.click(new ButtonLocator("Copy to Clipboard"));
		ui.click(new ButtonLocator("&Close"));
		//ui.wait(new ShellDisposedCondition("Configuration Details"));
		ui.wait(new ShellDisposedCondition("MetaWare IDE Installation Details"));
		ui.click(new ButtonLocator("OK"));
		ui.wait(new ShellDisposedCondition("About MetaWare IDE"));
		String clipboard = EclipseUtil.getClipboardText();
		if (clipboard == null) clipboard = "";
		int index = clipboard.indexOf("java.home=");
		boolean complain = true;
		if (index > 0) {
		    int endIndex = clipboard.indexOf('\n',index);
		    if (endIndex > 0) {
		        String s = clipboard.substring(index,endIndex);
		        complain = false;
		        Assert.assertTrue("Wrong JRE: " + s, s.indexOf("1.6") > 0 || s.indexOf("jre6") > 0);
		    }
		}
		if (complain){
			this.writeStringToFile("T_1_7.txt",clipboard);
		    Assert.fail("Cannot detect JRE");
		}
	}

}