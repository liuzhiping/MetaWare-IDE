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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

/**
 * Parsers the output of the MetaWare linker. Specifically, it just looks for
 * undefined references:
 * 
 * <pre>
 * 
 *  
 *   Unresolved Symbol:    Referenced from:
 *   --------------------------------------
 *   symbol                referencing file
 *   ...                   ...
 *   
 *  
 * </pre>
 */
public class LinkerErrorParser extends AbstractARCErrorParser  {

    private boolean mPendingUnresolves = false;

    private static final Pattern PATTERN = Pattern
            .compile("^\\[ld[^\\]]*\\]\\s*(\"([^\"]*)\"(,\\s*line\\s(\\d*):)?)?(.*)$");

    private boolean mUnderscoresSeen = false;

    private String mPendingSymbol = null;
    
    private boolean mInError = false;
    
    private String mErrorDescription = null;
    
    private  String mFile;
    private  int mLineNumber;
//  private static int mColumnNumber;
    private int mSeverity;

    private ErrorParserManager mErrorParserManager;

    public boolean processLine(String line, ErrorParserManager eoParser) {
        if (!mPendingUnresolves) {
            if (line.indexOf("Unresolved Symbol:") >= 0) {
                if (mInError) setMarker();
                mPendingUnresolves = true;
                return true;
            } else {
                return tryGenericLinkerMessage(line, eoParser);
            }
        } else if (line.indexOf("-----") >= 0) {
            // Do nothing; its the underline
            mUnderscoresSeen = true;
            return true;
        } else if (mUnderscoresSeen) {
            String symbolAndFile[] = parseSymbolAndFile(line, mPendingSymbol);
            if (symbolAndFile != null && symbolAndFile.length > 0) {
                if (symbolAndFile.length == 1) {
                    mPendingSymbol = symbolAndFile[0];
                } else {
                    IFile sourceFile = getSourceFileFor(symbolAndFile[1],
                            eoParser);
                    eoParser.generateMarker(sourceFile, 0,
                            "Unresolved reference: " + symbolAndFile[0],
                            IMarkerGenerator.SEVERITY_ERROR_RESOURCE, null);
                    mPendingSymbol = null;
                }
                return true;
            }
        }
        mPendingUnresolves = false;
        mUnderscoresSeen = false;
        mPendingSymbol = null;
        return false;
    }

    private static final Pattern SYM_FILE_PATTERN = Pattern
    .compile("((static )?(\\w+(<[^>]*>)*::)*~?(\\w+)(\\(.*\\))?)(\\s+.*)?\\z");

    private static String[] parseSymbolAndFile(String line,
            String symbolFromPrevLine) {
        if (symbolFromPrevLine != null) {
            if (line.startsWith("                  ")) {
                String file = line.trim();
                if (file.endsWith(".o") || file.endsWith(".obj")) {
                    return new String[] { symbolFromPrevLine, file };
                }
            }
            return null;
        }
        // If line has "<...>" they may be arbitrarily nested.
        // Find nested ones and replace '<' and '>' with benign chars and
        // then convert them back after the match.
        int angleStart = line.indexOf('<');
        int angleEnd = -1;
        String angleStuff = null;
        if (angleStart > 0){
            int nested = 1;
            for (int j = angleStart+1; nested > 0 && j < line.length(); j++){
                if (line.charAt(j) == '<') nested++;
                else if (line.charAt(j) == '>') {
                    nested--;   
                    angleEnd = j;
                    angleStuff = line.substring(angleStart+1,j);
                    line = line.substring(0,angleStart+1) +
                                angleStuff.replace('<','%').replace('>','#') +
                                line.substring(angleEnd);
                }
            }
        }
        Matcher m = SYM_FILE_PATTERN.matcher(line);
        if (m.matches()) {
            // If nested <...> then restore...
            String sym = m.group(1).replace('%','<').replace('#','>');
            String file = m.group(7);
            if (file != null && file.trim().length() == 0) {
                file = null;
            }
            if (file != null)
                return new String[] { sym, file.trim() };
            return new String[] { sym };
        }
        return null;
    }

    private boolean tryGenericLinkerMessage (String line, ErrorParserManager eoParser) {
        mErrorParserManager = eoParser;
        if (mInError) {
            if (line.indexOf('|') == 0) {
                // it's a continuation of the previous line
                mErrorDescription += "\n" + line.substring(1).trim();
                // Clients don't call "flush()", so we must detect if this is possibly the end
                // of the message. We assume so if it ends in ".". (CR91179)
                if (mErrorDescription.endsWith("."))
                    setMarker();
                return true;
            }
            else
                setMarker();
        }
        Matcher matcher = PATTERN.matcher(line);
        mInError = false;
        if (matcher.matches()) {
            // Assembler errors look like this:
            // [ldac] "xxx" is multiply define in foo.o and bar.o
            //
            // Warnings look like this:
            // [ldac] (Warning) a warning message.
            // [ldac] "cmdfile", line xxx: (Warning) ...
            // [ldac] (Warning#nnn) ...
            //

            mSeverity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
            String msg = matcher.group(5);
            mFile = matcher.group(2);
            String lineString = matcher.group(4);
            if (lineString != null && lineString.length() > 0){
                try {
                    mLineNumber = Integer.parseInt(lineString);
                }
                catch (NumberFormatException e) {
                    mLineNumber = 0;
                }
            }
            else mLineNumber = 0;
            mErrorDescription = msg.trim();
            if (msg.toLowerCase().startsWith("(warning)")) {
                mSeverity = IMarkerGenerator.SEVERITY_WARNING;
                msg = msg.substring("(warning)".length());
            }
            else if (msg.toLowerCase().indexOf("(warning") >= 0) {
                mSeverity = IMarkerGenerator.SEVERITY_WARNING;
            }
            // cr95765: linker message "No section mapped to GROUP" doesn't have terminating period!
            if (msg.endsWith(".") || msg.endsWith("No section mapped to GROUP")) {
                setMarker();
                return true;
            }
            mInError = true;
        }
        return mInError;
    }
    
    private void resetErrorInfo() {
        mFile = null;
        mLineNumber = 0;
//      mColumnNumber = 0;
        mErrorDescription = null;
        mSeverity = 0;
    }
    
    private void setMarker() {
        if (mErrorDescription != null)
            generateMarker(mErrorParserManager,mFile,mLineNumber,mErrorDescription,mSeverity,null);
        resetErrorInfo();
    }

    private static IFile getSourceFileFor(String objectFile,
            ErrorParserManager eoParser) {
        if (objectFile.endsWith(".o") || objectFile.endsWith(".obj")) {
            String[] suffixes = { ".c", ".cpp", ".cc", ".s" };
            String baseFile = objectFile.substring(0, objectFile.length() - 2);
            if (!new File(baseFile).isAbsolute()) {
                baseFile = ".." + File.separator + baseFile;
            }
            for (String suffix : suffixes) {
            	String fn = baseFile + suffix;
                IFile f = eoParser.findFileName(fn);
                if (f != null)
                    return f;
            }
        }
        else {
            return eoParser.findFileName(objectFile); // Assume a command file.
        }
        return null;
    }

}
