package com.arc.cdt.tests.B;

import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_CR90731 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Check that it doesn't take an unreasonable amount of time to terminate a debug session.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
	/**
	 * Main test method.
	 */
	public void testT_CR90731() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		buildProject("Test1");
		invokeDebuggerFor("Test1",true);
		final boolean failure[] = new boolean[1];
		final TimerTask t_too_long = new TimerTask() {
			@Override
            public void run() {
				failure[0] = true;			
			}
		};
		// Create a time and schedule it
		final Timer timer = new Timer();
		// allow 5 seconds, always fails for 100 ms
		timer.schedule(t_too_long, 5000);
		// stop debugger
		terminateDebugger();
		Assert.assertFalse("Termination took too long",failure[0]);
	    t_too_long.cancel();
	    switchToCPerspective();
		// if debugger stops soon enough it avoids assert error
	}

}