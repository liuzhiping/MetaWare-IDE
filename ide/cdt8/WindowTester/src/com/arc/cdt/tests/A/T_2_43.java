

package com.arc.cdt.tests.A;


import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_2_43 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test contents of debugger launch configuration dialog.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    /**
     * Main test method.
     */
    public void testT_2_43 () throws Exception {
        // Test if Launch configuration dialog isn't pathalogically large by being able to set
        // every option.
        bringUpDebugLaunchDialog(new IUIRunnable() {

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {

                ui.click(computeTreeItemLocator("C\\/C\\+\\+ .*Application/Test1.elf"));
                ui.click(new XYLocator(new CTabItemLocator("Debugger"), 40, 11));
                EclipseUtil.setActiveShellSize(ui, 1024, 900);

                ui.click(computeTreeItemLocator("Program Options"));
                //ui.click(new NamedWidgetLocator("Source_path"));
                //ui.enterText("C:/tmp");
                //ui.click(new NamedWidgetLocator("Directory_translation"));
                //ui.enterText("C:/tmp;D:/tmp");
                ui.click(new NamedWidgetLocator("Local_symbols"));
                ui.click(new NamedWidgetLocator("Dont_download"));
                ui.click(new NamedWidgetLocator("verify_download"));
                // WARNING: unsupported widget selection ignored - Widget Selection event: null
                // WARNING: unsupported widget selection ignored - Widget Selection event: null
                ui.click(new NamedWidgetLocator("prefer_sw_bp"));
                ui.click(new NamedWidgetLocator("prefer_sw_bp"));
                // EclipseUtil.dumpControl((Shell)ui.getActiveWindow());
                ui.click(new ComboItemLocator("MQX", new NamedWidgetLocator("RTOS")));
                ui.click(computeTreeItemLocator("Command-Line Options"));
                ui.click(new NamedWidgetLocator("Program_toggles_on"));
                ui.enterText("A,B,C");
                ui.click(new NamedWidgetLocator("Program_toggles_off"));
                ui.enterText("One,Two,Three");
                ui.click(new NamedWidgetLocator("cmd_line_option"));
                ui.enterText("commandline");
                ui.click(new NamedWidgetLocator("command_logging"));
                ui.click(new NamedWidgetLocator("command_logging"));
                ui.click(new NamedWidgetLocator("profiling_window"));
                ui.click(computeTreeItemLocator("Semantic Inspection"));
                ui.click(new NamedWidgetLocator("SIDLL1"));
                ui.enterText("DLL");
                ui.click(computeTreeItemLocator("Target Selection"));
                ui.click(new NamedWidgetLocator("A6_mpu"));
                ui.click(new NamedWidgetLocator("A6_mpu"));
                ui.click(new NamedWidgetLocator("ARC_instr_cnt"));
                ui.click(new NamedWidgetLocator("ARC_killed_cnt"));
                ui.click(new ButtonLocator("Enable Instruction History Tracing"));
                ui.click(computeTreeItemLocator("Simulator Extensions"));
                ui.click(computeTreeItemLocator("Simulator Extensions/DSP Instructions"));
                ui.click(new NamedWidgetLocator("ARC_xmac_24"));
                ui.click(new NamedWidgetLocator("ARC_xmac_d16"));
                ui.click(new NamedWidgetLocator("ARC_ea"));
                ui.click(new NamedWidgetLocator("ARC_dvbf"));
                ui.click(new NamedWidgetLocator("ARC_crc"));
                ui.click(new NamedWidgetLocator("ARC_mul32x16"));
                ui.click(computeTreeItemLocator("Simulator Extensions/DSP Memory"));
                ui.click(new ButtonLocator("Scratch RAM extension"));
                ui.click(new ButtonLocator("XY memory"));
                ui.click(computeTreeItemLocator("Simulator Extensions/Memory Options"));
                ui.click(computeTreeItemLocator("Simulator Extensions/User Extensions"));
                ui.click(new NamedWidgetLocator("ARC_ExtDLL1"));
                ui.enterText("simext");
                ui.click(computeTreeItemLocator("Simulator Extensions/Cache simulation"));
                ui.click(new NamedWidgetLocator("AC_icache"));
                ui.click(new ComboItemLocator("2048", new NamedWidgetLocator("AC_icache_size")));
                ui.click(new ComboItemLocator("32", new NamedWidgetLocator("AC_icache_line_size")));
                ui.click(new ComboItemLocator("2", new NamedWidgetLocator("AC_icache_ways")));
                ui.click(new ComboItemLocator("Random", new NamedWidgetLocator("AC_icache_repalg")));
                ui.click(new NamedWidgetLocator("AC_dcache"));
                ui.click(new ComboItemLocator("2048", new NamedWidgetLocator("AC_dcache_size")));
                ui.click(new ComboItemLocator("128", new NamedWidgetLocator("AC_dcache_line_size")));
                ui.click(new ComboItemLocator("4", new NamedWidgetLocator("AC_dcache_ways")));
                ui.click(new ComboItemLocator("Random", new NamedWidgetLocator("AC_dcache_repalg")));
                ui.click(new NamedWidgetLocator("ARC_cache_rams"));
                ui.click(computeTreeItemLocator("Simulator Extensions/Floating Point"));
                ui.click(computeTreeItemLocator("Simulator Extensions/MX \\& VRaptor extensions"));
                ui.click(computeTreeItemLocator("Simulator Extensions/Interrupts"));
                //ui.click(new NamedWidgetLocator("ARC_int_extension"));
                ui.click(new NamedWidgetLocator("ARC_bad_instr_intr"));
                ui.click(new NamedWidgetLocator("ARC_mem_exc_intr"));
                ui.click(computeTreeItemLocator("Simulator Extensions/Cycle Counting"));
                ui.click(new ButtonLocator("Enable estimated instruction cycle counting"));
                ui.click(computeTreeItemLocator("Simulator Extensions/Terminal\\/COMM Simulator"));
                ui.click(new ButtonLocator("Terminal Simulator   "));
                ui.click(new ButtonLocator("COMM Port Simulator   "));
                ui.click(computeTreeItemLocator("Simulator Extensions/Timers"));
                ui.click(new NamedWidgetLocator("ARC_timer0"));
                ui.click(new NamedWidgetLocator("ARC_timer1"));
                ui.click(computeTreeItemLocator("Initialization"));
                ui.click(new NamedWidgetLocator("AC_chipinit"));
                ui.click(new NamedWidgetLocator("AC_Chipinit_filename"));
                ui.enterText("CHIPINIT");
                
                // WindowTester causes bogus "Widget is Disposed" exception in the following
                // action. Thus, we are forced to skip it.
//                if (false)
//                    ui.click(new ComboItemLocator("0x10_0000", new NamedWidgetLocator("memsize")));
//                else 
                	ui.find(new NamedWidgetLocator("memsize"));
                
                ui.click(computeTreeItemLocator("Breakpoints\\/Stepping"));
                ui.click(new ButtonLocator("Prefer software breakpoints"));
                ui.click(computeTreeItemLocator("Peripheral displays"));
                ui.click(new NamedWidgetLocator("ARC_uart_0"));
                ui.click(new NamedWidgetLocator("ARC_uart_1"));
                ui.click(new NamedWidgetLocator("ARC_uart_2"));
                ui.click(new NamedWidgetLocator("ARC_vmac_2"));
                ui.click(new NamedWidgetLocator("ARC_vmac_1"));
                ui.click(new NamedWidgetLocator("ARC_vmac_0"));
                ui.click(computeTreeItemLocator("AUX Registers"));
                ui.click(new NamedWidgetLocator("ARC_auxreg"));
                ui.click(new NamedWidgetLocator("ARC_auxreg"));
                ui.click(computeTreeItemLocator("Side-effect registers"));
                ui.click(computeTreeItemLocator("Target reset"));
                ui.click(new ButtonLocator("Re&vert"));
                ButtonLocator apply = new ButtonLocator("Apply");
                if (new IsEnabledCondition(apply,true).testUI(ui)){
                    ui.click(new ButtonLocator("Close"));              
                    ui.wait(new ShellShowingCondition("Save [cC]hanges?"));
                    ui.click(new ButtonLocator("No"));
                    ui.wait(new ShellDisposedCondition("Save [cC]hanges?"));
                }
                else {
                	ui.click(new ButtonLocator("Close"));
                }

            }
        });
    }

}