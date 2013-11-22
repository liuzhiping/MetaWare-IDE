package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_CR90726 extends UIArcTestCaseSWT {
	public final static String DESCRIPTION = "Confirm that SWT bug in \"Build step\" dialog has been fixed.";
	public final static String CATEGORY = GENERIC_CDT_TESTS;
		

	/**
	 * Main test method.
	 */
	public void testT_CR90726() {
	    // THIS TEST LEAVES Test1 SCREWED UP.
	    // It tests a condition that was fixed CDT 4.0
//		Clipboard clipboard;
//		Transferable content;
//		String cb_data;
//		registerPerspectiveConfirmationHandler();
//	    switchToCPerspective(); //in case previous test left in wrong perspective
//		IUIContext ui = getUI();
//		this.rightClickProjectMenu("Test1","Properties");
//		ui.wait(new ShellShowingCondition("Properties for Test1"));
//		ui.click(computeTreeItemLocator("C\\/C++ Build/Settings"));
//		ui.click(new TabItemLocator("Build steps"));
//		ui.click(new NamedWidgetLocator("preCmd"));
//		ui.keyClick(WT.CTRL, 'A');
//		ui.keyClick(WT.BS);
//		ui.enterText("aaa bbb");
//		ui.click(new NamedWidgetLocator("preDes"));
//		ui.keyClick(WT.CTRL, 'A');
//		ui.keyClick(WT.BS);
//		ui.enterText("xxx yyy");
//		ui.click(new ButtonLocator("&Apply"));
//		
//		ui.click(new XYLocator(new NamedWidgetLocator("preCmd"), 53, 9));
//		ui.keyClick(WT.BS);
//		ui.keyClick(WT.BS);
//		ui.keyClick(WT.BS);
//		ui.keyClick(WT.BS);
//		ui.enterText("z");
//		ui.keyClick(WT.CTRL, 'A');
//		ui.keyClick(WT.CTRL, 'C');
//		// Get data from clip board
//		ui.pause(1000);
//		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//		content = clipboard.getContents(null);
//		cb_data = "";
//		if(content != null)
//			cb_data = (String)content.getTransferData(DataFlavor.stringFlavor);
//		// verify output is correct
//		if(!cb_data.equals("aaaz")) {
//            writeStringToFile("T_CR90726a.txt", cb_data);
//            Assert.assertTrue(false);
//		}
//
//		ui.click(new XYLocator(new NamedWidgetLocator("preDes"), 51, 8));
//		ui.pause(500);
//		ui.keyClick(WT.BS);
//		ui.keyClick(WT.BS);
//		ui.keyClick(WT.BS);
//		ui.keyClick(WT.BS);
//		ui.enterText("w");
//		ui.keyClick(WT.CTRL, 'A');
//		ui.keyClick(WT.CTRL, 'C');
//		// Get data from clip board
//		ui.pause(1000);
//		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//		content = clipboard.getContents(null);
//		cb_data = "";
//		if(content != null)
//			cb_data = (String)content.getTransferData(DataFlavor.stringFlavor);
//		// verify output is correct
//		if(!cb_data.equals("xxxw")) {
//            writeStringToFile("T_CR90726b.txt", cb_data);
//            Assert.assertTrue(false);
//		}
//
//		// clear the two fields
//		ui.click(new XYLocator(new NamedWidgetLocator("preCmd"), 56, 5));
//		ui.keyClick(WT.CTRL, 'A');
//		ui.keyClick(WT.BS);
//		ui.click(new XYLocator(new NamedWidgetLocator("preDes"), 65, 6));
//		ui.keyClick(WT.CTRL, 'A');
//		ui.keyClick(WT.BS);
//		ui.click(new ButtonLocator("&Apply"));
//		ui.click(new ButtonLocator("Cancel"));
//		ui.wait(new ShellDisposedCondition("Properties for Test1"));
	}

}