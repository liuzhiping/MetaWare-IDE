package com.arc.cdt.tests.A;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_2_06 extends UIArcTestCaseSWT {
    
    public static final String CATEGORY = BUILD_MANAGEMENT;
    public static final String DESCRIPTION = "Confirm that preprocessor settings are passed to the compiler.";

	private static final String PROJECT_NAME = "Queens_AC";

    /**
	 * Main test method.
	 */
	public void testT_2_06() throws Exception {
		//registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		// add preprocessor define: FOO
	    this.setDefaultBuildProperties(PROJECT_NAME);
		this.enterPreprocessorValue(PROJECT_NAME,"FOO");
		buildProject(PROJECT_NAME);
		getUI().wait(milliseconds(1000)); // give it time to materialize
		String console = getBuildConsoleContent();
		Pattern p = Pattern.compile("\\nmcc.*-DFOO(=|\\s)");
        Matcher m = p.matcher(console);
        if (!m.find()) {
		    writeStringToFile("T_2_6.txt",console);
	        Assert.assertTrue(false);
		}

        this.removePreprocessorValue(PROJECT_NAME);
        buildProject(PROJECT_NAME);
        
        console = getConsoleContent();
        
        p = Pattern.compile("\\nhcac.*-DFOO(=|\\s)");
        m = p.matcher(console);
        // Complain if "-DFOO" still exists, or if "gmake: Nothing to make" appears.
        if (m.find() || console.indexOf("Nothing") >= 0) { 
            writeStringToFile("T_2_6.txt",console);
            Assert.assertTrue(false);
        }
	}
}