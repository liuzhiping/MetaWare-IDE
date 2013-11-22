package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.MenuItemLocator;

public class T_CR91918 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that existence of MetaWare Toolkit Documentation menu item.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
	/**
	 * Main test method.
	 * @throws WidgetSearchException 
	 */
	public void testT_CR91918() throws WidgetSearchException {
		getUI().click(new MenuItemLocator("Help/MetaWare Toolkit Documentation..."));
		// may want to test that MenuItemLocator points to file "contents.pdf"
	}

}