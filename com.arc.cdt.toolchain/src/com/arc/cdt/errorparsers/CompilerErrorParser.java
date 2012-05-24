/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.cdt.errorparsers;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;

/**
 * This is a custom error parser used for the MetaWare High C/C++
 * Compiler.
 * 
 * N.B. It doesn't handle parsing multi-line errors with the base-
 * line CDT implementation.  We're waiting for them to upgrade CDT
 * to enable this functionality.
 */
public class CompilerErrorParser extends AbstractARCErrorParser 
{
    
    private static final Pattern PATTERN = Pattern.compile("^[wE] \"([^\"]*)\"\\s*,?\\s*L(\\d+)(/C(\\d*))?(\\(#?\\d+\\))?:\\s*(.*)$");
	private  String mFile;
	private  int mLineNumber;
//	private static int mColumnNumber;
	private  String mErrorDescription;
	private int mSeverity;
	private  boolean mInError = false;
	private ErrorParserManager mErrorParserManager;

	private void resetErrorInfo() {
		mFile = null;
		mLineNumber = 0;
//		mColumnNumber = 0;
		mErrorDescription = null;
		mSeverity = 0;
	}

	private void setMarker() {
		if (mFile != null) {
			generateMarker(mErrorParserManager,mFile,mLineNumber,mErrorDescription,mSeverity,null);
		}
		resetErrorInfo();
	}

	/**
	 * The constructor.
	 */
	public CompilerErrorParser() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IErrorParser#processLine(java.lang.String, org.eclipse.cdt.core.ErrorParserManager)
	 */
	public boolean processLine(String line, ErrorParserManager eoParser) {
		mErrorParserManager = eoParser;
		//SOP(s);
		// queens.c (L27/C5) : error 360: You can't return a value from void
		// function `Try'.
		//		 msdev: filname(linenumber) : error/warning error_desc
		if (mInError && line.indexOf('|') == 0) {
			// it's a continuation of the previous line
		    // cr95531: the Problems view does not support multi-line entries (though
		    // its tooltips do).
		    // It either shows the newline as a square, or doesn't show anything.
		    // In case the latter applies, we'll append a space to  each line.
		    //cr96298: get rid of newline. It doesn't serve in purpose since the tooltip in the editor
		    // folds long messages anyway.
			mErrorDescription += " " + line.substring(1).trim();
			// Clients don't call "flush()", so we must detect if this is possibly the end
			// of the message. We assume so if it ends in ".". (CR91179)
			if (mErrorDescription.endsWith("."))
				setMarker();
			return true;
		} else if (mInError && line.startsWith("REPAIR:")) {
			// it's a continuation of the previous line
			mErrorDescription += " " + line.trim();
			return true;
		} else {
			setMarker();
		}

        mInError = false;
        Matcher matcher = PATTERN.matcher(line);
        if (matcher.matches()){
            mFile = matcher.group(1);
            String lineString = matcher.group(2);
            String number = matcher.group(5);
            String msg = matcher.group(6);
            mLineNumber = Integer.parseInt(lineString);        
            mErrorDescription = msg.trim();
            
            mSeverity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
            if (line.startsWith("w")) { //$NON-NLS-1$
                // Include ID for warnings so user knows what to turn off.
                if (number != null && number.length() > 0){
                    mErrorDescription = number + " " + mErrorDescription;
                }
                mSeverity = IMarkerGenerator.SEVERITY_WARNING;           
            }
            // If warning ends in a period, assume there is no continuation.
            // We don't have a "flush" method, and a warning may be the only
            // thing emitted in stderr. Errors are always terminated by
            // the gmake error, so things get flushed.
            // CORRECTION: check for errors also. We want the first line to have the background color.
            if (mErrorDescription.endsWith(".")) {
                setMarker();
                return true;
            }
            mInError = true;

        }		
		return mInError;
	}

	public void flush() {
		setMarker();
	}
}
