/*******************************************************************************
 * Copyright (c) 2004, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Synopsys
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;

public class ManagedCommandLineGenerator implements
		IManagedCommandLineGenerator {

	public final String AT = "@";	//$NON-NLS-1$
	public final String COLON = ":";	//$NON-NLS-1$
	public final String DOT = ".";	//$NON-NLS-1$
	public final String ECHO = "echo";	//$NON-NLS-1$
	public final String IN_MACRO = "$<";	//$NON-NLS-1$
	public final String LINEBREAK = "\\\n";	//$NON-NLS-1$
	public final String NEWLINE = System.getProperty("line.separator");	//$NON-NLS-1$
	public final String OUT_MACRO = "$@";	//$NON-NLS-1$
	public final String SEPARATOR = "/";	//$NON-NLS-1$
	public final String SINGLE_QUOTE = "'";	//$NON-NLS-1$
	public final String DOUBLE_QUOTE = "\""; //$NON-NLS-1$
	public final String TAB = "\t";	//$NON-NLS-1$
	public final String WHITESPACE = " ";	//$NON-NLS-1$
	public final String WILDCARD = "%";	//$NON-NLS-1$
	public final String UNDERLINE = "_"; //$NON-NLS-1$
	public final String EMPTY = ""; //$NON-NLS-1$
	
	public final String VAR_FIRST_CHAR = "$"; //$NON-NLS-1$
	public final char VAR_SECOND_CHAR = '{'; //$NON-NLS-1$
	public final String VAR_FINAL_CHAR = "}"; //$NON-NLS-1$
	public final String CLASS_PROPERTY_PREFIX = "get"; //$NON-NLS-1$
	
	public final String CMD_LINE_PRM_NAME = "COMMAND"; //$NON-NLS-1$
	public final String FLAGS_PRM_NAME = "FLAGS"; //$NON-NLS-1$
	public final String OUTPUT_FLAG_PRM_NAME = "OUTPUT_FLAG"; //$NON-NLS-1$
	public final String OUTPUT_PREFIX_PRM_NAME = "OUTPUT_PREFIX"; //$NON-NLS-1$
	public final String OUTPUT_PRM_NAME = "OUTPUT"; //$NON-NLS-1$
	public final String INPUTS_PRM_NAME = "INPUTS"; //$NON-NLS-1$
	
	private static ManagedCommandLineGenerator cmdLineGen;
	
	protected ManagedCommandLineGenerator() {		
		cmdLineGen = null;
	}
	
	public static ManagedCommandLineGenerator getCommandLineGenerator() {
		if( cmdLineGen == null ) cmdLineGen = new ManagedCommandLineGenerator();
		return cmdLineGen;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator#getCommandLineInfo(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
	 */
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool,
			String commandName, String[] flags, String outputFlag,
			String outputPrefix, String outputName, 
			String[] inputResources, String commandLinePattern) 
	{
		StringBuffer sb = new StringBuffer();
		if( commandLinePattern == null || commandLinePattern.length() <= 0 )
		    commandLinePattern = Tool.DEFAULT_PATTERN;
/*		    
			sb.append( commandName + WHITESPACE + stringArrayToString( flags ) + WHITESPACE + outputFlag + WHITESPACE + outputPrefix + 
				outputName + WHITESPACE + stringArrayToString( inputResources ) );
		else {
*/		    
			int start =  0;
			int stop = 0;
			while( (start = commandLinePattern.indexOf( VAR_FIRST_CHAR, start )) >= 0 ) {
				if( commandLinePattern.charAt( start + 1 ) != VAR_SECOND_CHAR  ) {
					sb.append(VAR_FIRST_CHAR);
					start++;
					continue;
				}
				if( start > stop ) {
					sb.append( commandLinePattern.substring(stop, start) );
				}
				stop = commandLinePattern.indexOf( VAR_FINAL_CHAR, start + 1 );
				if( stop > 0 && stop <= commandLinePattern.length() ) try {
					String varName = commandLinePattern.substring( start+2, stop ).trim();
					if( varName.compareToIgnoreCase( CMD_LINE_PRM_NAME ) == 0 ) sb.append( commandName.trim() );
					else if( varName.compareToIgnoreCase( FLAGS_PRM_NAME ) == 0 ) sb.append( stringArrayToString( flags ) );
					else if( varName.compareToIgnoreCase( OUTPUT_FLAG_PRM_NAME ) == 0 ) sb.append( outputFlag.trim() );
					else if( varName.compareToIgnoreCase( OUTPUT_PREFIX_PRM_NAME ) == 0 ) sb.append( outputPrefix.trim() );
					else if( varName.compareToIgnoreCase( OUTPUT_PRM_NAME ) == 0 )
						{
							StringBuffer tempBuffer = new StringBuffer(EMPTY);
						
							if(!outputName.equals(EMPTY))
							{
//								 if the output name isn't a variable then quote it
                                //CUSTOMIZATION: don't quote '$(blah)' but do quote '$(blah)/foo' in case
                                // $(blah) has spaces.
								if(outputName.indexOf("$(") != 0 || !outputName.endsWith(")")) //$NON-NLS-1$
									tempBuffer.append( DOUBLE_QUOTE + outputName + DOUBLE_QUOTE);
								else
									tempBuffer.append(outputName);
							}
							
							sb.append(tempBuffer.toString().trim());
						}
					else if( varName.compareToIgnoreCase( INPUTS_PRM_NAME ) == 0 && 
							(inputResources != null)){
						StringBuffer tempBuffer = new StringBuffer(EMPTY);
						for(int k = 0; k < inputResources.length; k++)
						{
							if(!inputResources[k].equals(EMPTY))
							{
								// if the input resource isn't a variable then quote it
							    // CUSTOMIZATION: don't quote '$(blah)' but do quote '$(blah)/foo' in case
                                // $(blah) has spaces.
                                String s = inputResources[k];
                                s = s.replaceAll("\\\\ "," "); // Get rid of excaped spaces; we're going to quote
                                if(inputResources[k].indexOf("$(") != 0 || !inputResources[k].endsWith(")") || s.indexOf(' ') >= 0) //$NON-NLS-1$
                                    tempBuffer.append(DOUBLE_QUOTE + s + DOUBLE_QUOTE + WHITESPACE); //$NON-NLS-1$ //$NON-NLS-2$
                                else
                                    tempBuffer.append(s + WHITESPACE);
							}
						}
						
						sb.append(tempBuffer.toString().trim());
						
					}
					else sb.append( VAR_FIRST_CHAR + VAR_SECOND_CHAR + varName + VAR_FINAL_CHAR );
				} catch( Exception ex ) {
					// 	do nothing for a while
				}
				start = ++stop;
//			}
		}
		if (stop<commandLinePattern.length()){
			sb.append(commandLinePattern.substring(stop));
		}

		return new ManagedCommandLineInfo( sb.toString().trim(), commandLinePattern, commandName, stringArrayToString( flags ),
				outputFlag, outputPrefix, outputName, stringArrayToString( inputResources ) );
	}
	
	private String stringArrayToString( String[] array ) {
		if( array == null || array.length <= 0 ) return new String();
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < array.length; i++ )
			sb.append( array[i] + WHITESPACE );
		return sb.toString().trim();
	}

}
