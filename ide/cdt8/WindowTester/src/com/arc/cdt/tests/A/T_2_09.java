package com.arc.cdt.tests.A;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;


public class T_2_09 extends UIArcTestCaseSWT {
    
   

    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    
    public static final String DESCRIPTION = "Confirm that debugger displays stay in sync with selected stackframe";

	private static final String PROJECT_NAME = "Queens_AC";

    /**
	 * NOTE: this test looks at Disassembly and Register views as different stackframes are selected.
	 * As the compiler is upgraded, the baseline for this test will likely need to be updated as well.
	 */
	public void testT_2_09() throws Exception {
		this.switchToCPerspective();  // in case left in bogus state
	    setCanonicalSize(); // Debugger view state is size dependent    
		IUIContext ui = getUI();
		//this.registerPerspectiveConfirmationHandler();
		this.setDefaultBuildProperties(PROJECT_NAME);
		cleanProject(PROJECT_NAME);
		buildProject(PROJECT_NAME);
		this.launchDebugger(PROJECT_NAME +".elf", true);
		this.setAnimationItems(false);
		this.showView(DISASM_VIEW_ID);
        this.showView(REGISTER_VIEW_ID);
		this.showSeeCodeView("Command-line input");
		this.enterDebugCommandLine("go Try");
		this.enterDebugCommandLine("go Try");
		this.enterDebugCommandLine("ssi");
		this.enterDebugCommandLine("ssi");
		this.enterDebugCommandLine("ssi");
		
		ui.wait(milliseconds(1000));
		// Now test that the disassembly and register displays stay in
		// sync with the launch view selection.
		compareSeecodeDisplay("T_2_09.disasm.1",DISASM);
		compareView("T_2_09.reg.1",REGISTER_VIEW_ID);
		this.moveLaunchViewSelection(-1);
		compareSeecodeDisplay("T_2_09.disasm.2",DISASM);
		compareView("T_2_09.reg.2",REGISTER_VIEW_ID);
		this.moveLaunchViewSelection(-1);
		compareSeecodeDisplay("T_2_09.disasm.3",DISASM);
		compareView("T_2_09.reg.3",REGISTER_VIEW_ID);
		this.moveLaunchViewSelection(+1);
		compareSeecodeDisplay("T_2_09.disasm.4",DISASM);
		compareView("T_2_09.reg.4",REGISTER_VIEW_ID);
		this.moveLaunchViewSelection(+1);
        compareSeecodeDisplay("T_2_09.disasm.5",DISASM);
        compareView("T_2_09.reg.5",REGISTER_VIEW_ID);
		
		this.terminateDebugger();
		this.switchToCPerspective();
	}

}