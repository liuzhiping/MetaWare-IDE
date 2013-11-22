

package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.T_CMPD;


public class T_CR92730 extends T_CMPD {

    public static final String DESCRIPTION = "Test CMPD using the imfamous \"Graphics Pipeline\" demo.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    private static final String LAUNCH_NAME = "Graphics pipeline";
    
    @Override
    protected String getLaunchName() { return LAUNCH_NAME; }

}