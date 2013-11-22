

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Canvas;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.locator.ILocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;


public class T_CR96438 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that Jump To Line and \"Set PC\" operations work as expected.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH = "inlinecase";

    /**
     * Main test method.
     */
    public void testT_CR96438() throws Exception {
        this.switchToDebugPerspective();
        this.launchDebugger(LAUNCH,false);
        EclipseUtil.closeAllEditors();
        this.waitUntilDebuggerStops(20000);
        
        IUIContext ui = getUI();
        EclipseUtil.openEditor("inlinecase/header.h");
        
        //BUG in CDT: race condition can prevent selection provider from being set before
        // enablement of "Jump to Line" menu item. So it is grayed out. We must Re-select.
        String debugSelection = EclipseUtil.getDebugViewSelectionString();
        ui.click(computeTreeItemLocator(LAUNCH + ".*/MetaWare Debugger.*/Thread.*/" +
            EclipseUtil.escapeString(debugSelection)));
        
        String pcValue1 = getValueFromRegisterView("pc");
        
        ui.click(new CTabItemLocator("header.h"));
        ui.contextClick(new XYLocator(new SWTWidgetLocator(StyledText.class, new SWTWidgetLocator(Canvas.class)),87,129), "Resume At Li&ne");
        ui.wait(new ShellShowingCondition("Warning"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Warning"));
        String pcValue2 = getValueFromRegisterView("pc");
        Assert.assertTrue("PC value changed (prev=" +pcValue1 + ", new=" + pcValue2+")",!pcValue1.equals(pcValue2));
        
        String debugPC1 = EclipseUtil.getDebugViewPC();
        Assert.assertTrue("PC value (=0x" + pcValue2+") matches Debug view (=" + debugPC1+")",pcValue2.equalsIgnoreCase(debugPC1.substring(2)));
        
        this.showSeeCodeView("Source");
        ui.click(new NamedWidgetLocator("source.combo.source"));
        ui.enterText("main.c");
        ui.keyClick(WT.CR);
        IWidgetReference srcRef = EclipseUtil.findView(SEECODE_VIEW_ID,"source");
        ILocator srcRefSelect = EclipseUtil.findSquareCanvasLocator(ui, srcRef, 6);
        ui.contextClick(srcRefSelect, "Set PC");
        
        String pcValue3 = getValueFromRegisterView("pc");
        String debugPC2 = EclipseUtil.getDebugViewPC();
        Assert.assertTrue("PC changed in debug view",!debugPC2.equals(debugPC1));
        Assert.assertTrue("New PC value jives",pcValue3.equals(debugPC2.substring(2)));
      
        this.terminateDebugger();

        
    }

}