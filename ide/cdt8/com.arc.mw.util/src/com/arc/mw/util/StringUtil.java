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
package com.arc.mw.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
/**
 * Miscellaneous string utililities.
 */
public class StringUtil {
	
	/**
	 * This method searches the source string for the search string and if found, replaces
	 * the FIRST occurence of it with the replace string.  If the search string is not found, 
	 * the source string is returned unchanged.
	 * 
	 * @param sourceStr
	 * @param searchStr
	 * @param replaceStr
	 * @return the source string with the first occurance of the search string replaced.
	 */
	public static String strReplace(String sourceStr, String searchStr, String replaceStr) {
		String retVal = null;
		int index = sourceStr.indexOf(searchStr);
		if(index > -1) {
			retVal = sourceStr.substring(0, index);
			retVal += replaceStr;
			retVal += sourceStr.substring(index + searchStr.length(), sourceStr.length());
		}
		if(retVal == null)
			retVal = sourceStr;
		return retVal;
	}
	
	/**
	 * Returns the ending of a string after the last ocurrence of the string 'endChar'
	 * @param s
	 * @param endChar
	 * @return String
	 */
	public static String getStringEnding(String s, String endChar) {
		int endCharLoc = s.lastIndexOf(endChar);
		if(endCharLoc > 0) {
			return s.substring(endCharLoc+1, s.length());		
		}
	    return s;
	}
    /**
     *  Convert a path "a:b:c" to the array {"a", "b", "c" }
     */
    public static String[] pathToArray(String path) {
        return pathToArray(path, File.pathSeparator);
    }

    /**
     *  Convert a path "a:b:c" to the array {"a", "b", "c" }
     *
     * @param path
     * @param separator the separator character
     */
    public static String[] pathToArray(String path, String separator) {
        StringTokenizer tokenizer = new StringTokenizer(path, separator);
        ArrayList<String> list = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        return list.toArray(new String[list.size()]);
    }

    /**
    * Scan a string  looking for a terminating character <code>endChar</code>.
    * Skip over matching parentheses (...) and brackets {...}.
    * @param s the string being scanned
    * @param position the starting position in the string
    * @param endChar the character being scanned for.
    * @return the position of the terminating character, or -1 if no
    * terminating character was found.
    */
    public static int parenMatch(String s, int position, char endChar) {
        int len = s.length();
        for (int i = position; i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '(' :
                    i = parenMatch(s, i + 1, ')');
                    break;
                case '{' :
                    i = parenMatch(s, i + 1, '}');
                    break;
                case ')' :
                case '}' :
                    return (c == endChar) ? i : -1;
            }
        }
        return -1;
    }

    /**
    * Split a string into a list using whitespace as
    * the separator. The list to be filled in is passed as the first
    * argument.
    * <p>
    * Embedded quotes that aren't excaped are assume to
    * surround a single token. Thus:
    * <p>
    * <pre><code>
    *  This "is a" string wi'th embed'ded substrings
    * </pre></code>
    * <p>
    * Will parse as:
    * <pre><code>
    *  "This"
    *  "is a"
    *  "string"
    *  "with embedded"
    *  "substrings"
    * </pre></code>
    * 
    * <p>
    * @param l the list to be filled in.
    * @param s the string to be split into a list.
    * @return the list <code>l</code>
    */
    public static List<String> stringToList(List<String> l, String s) {
        if (s != null) {
            int len = s.length();
            int i = 0;
            char c;
            StringBuffer buf = new StringBuffer(s.length());

            while (i < len) {
                buf.delete(0, buf.length());

                // Skip white space
                for (; i < len; i++) {
                    c = s.charAt(i);
                    if (!Character.isWhitespace(c))
                        break;
                }

                // Scan a token, taking into account
                // quoted substrings. We strip off the quotes
                // in a manner similar to Cshell.
                char quote = 0;
                for (; i < len; i++) {
                    c = s.charAt(i);
                    if (c == '\\' && quote != 0) {
                        if (i + 1 < len) {
                            i++;
                            buf.append(s.charAt(i));
                        }
                    }
                    else if (quote == 0) {
                        if (Character.isWhitespace(c))
                            break;
                        if (c == '"' || c == '\'') {
                            quote = c;
                        }
                        else
                            buf.append(c);
                    }
                    else if (c == quote)
                        quote = 0;
                    else
                        buf.append(c);
                }
                l.add(buf.toString());
            }
        }

        return l;
    }

    /**
     * Split a string into a list using whitespace as
     * the separator.
     * @param s the string to be split.
     * @return the list.
     */
    public static List<String> stringToList(String s) {
        return stringToList(new ArrayList<String>(s.length() / 5 + 3), s);
    }

    public static String[] stringToArray(String s) {
        List<String> list = stringToList(s);
        return list.toArray(new String[list.size()]);
    }

    /**
    * Append a list to a string buffer placing a specified
    * string between each element.
    * @param b the string buffer to be filled in.
    * @param l the list of strings to be appended.
    * @param sep the seperator to inserted between each list element.
    */
    public static void appendListToString(StringBuffer b, List<String> l, String sep) {
        if (l != null) {
            for (String element: l){
                b.append(sep);
                b.append(element);
            }
        }
    }

    /**
    * Alternate to List.toString() that formats the list
    * as a simple string using a specified string as the
    * separator between elements.
    * @param l the list of strings to  concatenated into a single string.
    * @param sep the seperator to inserted between each list element.
    */
    public static String listToString(List<String> l, String sep) {
        if (l == null)
            return "";
        StringBuffer b = new StringBuffer();
        appendListToString(b, l, sep);
        return b.toString();
    }

    public static String arrayToString(String array[], String separator) {
        return listToString(Arrays.asList(array), separator);
    }

    public static String arrayToString(String array[]) {
        return arrayToString(array, " ");
    }

    /**
     * Converts a String containing binary data to hexadecimal data.
     * @param binString
     * @return String
     */
    public static String convertBinaryStringToHex(String binString) {
        Long intValue = Long.valueOf(binString, 2);
        return Long.toString(intValue.longValue(), 16);
    }
    
    /**
     * Convert an array of tokens to an argument string; tokens
     * with white space are enclosed in double quotes.
     * Tokens with double quotes and back-slashes are also quoted
     * and escaped.
     * @param list list of strings
     * @return argument list in with blank between each token.
     */
    public static String listToArgString(List<String> list){
        StringBuffer buf = new StringBuffer(list.size()*10);
        int size = list.size();
        for (int i = 0; i < size; i++){
            String s = list.get(i);
            int slen = s.length();
            buf.append(' ');
            if (slen == 0){
                buf.append("\"\"");
            }
            else {
                int start = buf.length();
                boolean needsQuoting = false;
                for (int j = 0; j < slen; j++){
                    char c = s.charAt(j);
                    switch(c){
                        case ' ': needsQuoting = true; break;
                        case '\\':
                        case '"': 
                            buf.append('\\'); 
                            needsQuoting = true;
                            break;
                        default: break;
                    }
                    buf.append(c);
                }
                if (needsQuoting){
                    buf.insert(start,'"');
                    buf.append('"');
                }               
            }
        }
        return buf.toString().trim();
    }
    
    /**
     * Like {@link #listToArgString(List)} but uses an array
     * instead.
     * @param tokens tokens to be formed into an argument string.
     * @return possibly quoted argument string.
     */
    public static String arrayToArgString(String[] tokens){
        return listToArgString(Arrays.asList(tokens));        
    }
}
