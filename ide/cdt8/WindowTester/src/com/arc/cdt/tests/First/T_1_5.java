package com.arc.cdt.tests.First;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.locator.MenuItemLocator;

public class T_1_5 extends UIArcTestCaseSWT {
    public static final String CATEGORY = INSTALLATION;
    public static final String DESCRIPTION = "Confirms that the \"Build Automatically\" menu item is unchecked under the \"Project\" menu";

	/**
	 * Confirm that Built Automatically menu item is checked.
	 */
	public void testT_1_5() throws Exception {
		IUIContext ui = getUI();
		ui.assertThat(new MenuItemLocator("Project/Build Automatically").isSelected(false));
	}

}