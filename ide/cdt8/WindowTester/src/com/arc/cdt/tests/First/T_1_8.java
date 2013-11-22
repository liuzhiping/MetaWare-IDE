package com.arc.cdt.tests.First;
import org.eclipse.swt.widgets.Tree;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;

public class T_1_8 extends UIArcTestCaseSWT {
    
    public static final String CATEGORY = INSTALLATION;
    public static final String DESCRIPTION = "Confirm that C and C++ projects have ARC as targets";

	/**
	 * Check the New project dialog for its contents.
	 * Main test method.
	 */
	public void testT_1_8() throws Exception {
	    IUIContext ui = getUI();
	    switchToCPerspective();
        ui.click(new MenuItemLocator("File/New/C Project"));
        ui.wait(new ShellShowingCondition(".*Project"));
        ui.click(new TreeItemLocator("Executable/Empty Project", new LabeledLocator(Tree.class, "Project [tT]ype:")));
        ui.click(new TableItemLocator("ARCompact"));
        ui.click(new TableItemLocator("ARCtangent 4"));
        ui.click(new ButtonLocator("Cancel"));
        ui.click(new MenuItemLocator("File/New/C\\+\\+ Project"));
        ui.wait(new ShellShowingCondition(".*Project"));
        ui.click(new TreeItemLocator("Executable/Empty Project", new LabeledLocator(Tree.class, "Project [tT]ype:")));
        ui.click(new TableItemLocator("ARCompact"));
        ui.click(new TableItemLocator("ARCtangent 4"));
        ui.click(new ButtonLocator("Cancel"));
	}

}