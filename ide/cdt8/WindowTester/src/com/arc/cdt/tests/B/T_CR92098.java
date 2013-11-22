package com.arc.cdt.tests.B;

import org.eclipse.swt.widgets.Tree;

import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;

public class T_CR92098 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that ARC targets exists for each kind of C/C++ Project.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_92098() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		IUIContext ui = getUI();
		deleteProject("NEW");
		ui.click(new MenuItemLocator("File/New/C Project"));
		ui.wait(new ShellShowingCondition(".*Project"));
		ui.enterText("NEW");
		ui.click(new TreeItemLocator(
			"Executable/Hello World MetaWare C++ Project",
			new LabeledLocator(Tree.class, "Project [tT]ype:")));
		ui.click(new TableItemLocator("ARCompact"));
		ui.click(new TableItemLocator("ARCtangent 4"));
		ui.click(new TreeItemLocator("Executable/Hello World (MetaWare )?C\\+\\+ Project",
			new LabeledLocator(Tree.class, "Project [tT]ype:")));
		//ui.click(new TableItemLocator("Cygwin GCC"));
		ui.click(new TreeItemLocator("Executable/Hello World ANSI C Project",
			new LabeledLocator(Tree.class, "Project [tT]ype:")));
		ui.click(new TableItemLocator("ARCompact"));
		ui.click(new TableItemLocator("ARCtangent 4"));
		//ui.click(new TableItemLocator("Cygwin GCC"));
		ui.click(new TreeItemLocator("Executable/Empty Project",
			new LabeledLocator(Tree.class, "Project [tT]ype:")));
		ui.click(new TableItemLocator("ARCompact"));
		ui.click(new TableItemLocator("ARCtangent 4"));
		//ui.click(new TableItemLocator("Cygwin GCC"));
		ui.click(new TreeItemLocator("Shared Library/Empty Project", new LabeledLocator(
			Tree.class, "Project [tT]ype:")));
		//ui.click(new TableItemLocator("Cygwin GCC"));
		ui.click(new TreeItemLocator("Static Library/Empty Project", new LabeledLocator(
			Tree.class, "Project [tT]ype:")));
		ui.click(new TableItemLocator("ARCompact"));
		ui.click(new TableItemLocator("ARCtangent 4"));
		//ui.click(new TableItemLocator("Cygwin GCC"));
		ui.click(new TreeItemLocator("Makefile project/Empty Project", new LabeledLocator(
			Tree.class, "Project [tT]ype:")));
		
//		ui.click(new TreeItemLocator(
//			"Makefile project/Hello World C++ Project", new LabeledLocator(
//				Tree.class, "Project [tT]ype:")));
		ui.click(new ButtonLocator("Cancel"));
	}

}