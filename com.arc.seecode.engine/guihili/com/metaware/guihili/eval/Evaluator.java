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
package com.metaware.guihili.eval;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.MalformedExpressionException;


/**
 * Evaluates expressions.
 * @author David Pickens, May 09,2002
 */

public class Evaluator implements IEvaluator {

    /**
     * Create an evaluator that uses the given environment
     */
    public Evaluator(ILisp lisp) {
        mLisp = lisp;
    }

    public Evaluator() {
        this(Lisp.create());
    }

    public ILisp getLisp () {
        return mLisp;
    }

    // See comments in interface
    @Override
    public boolean evaluateBoolean (String expression, IEnvironment env) throws MalformedExpressionException {
        Object expand = evaluate(expression, env);
        if (expand instanceof String) {
            String s = (String) expand;
            if (s.equalsIgnoreCase("true"))
                return true;
            if (s.equalsIgnoreCase("false"))
                return false;
            if (s.equals("0"))
                return false;
            if (s.equals("1"))
                return true;
            if (s.length() > 0 && Character.isDigit(s.charAt(0))){
            	try {
					return Integer.parseInt(s) != 0;
				} catch (NumberFormatException e) {
					//invalid integer
				}
            }
        }
        else if (expand instanceof Boolean)
            return ((Boolean) expand).booleanValue();
        else if (expand instanceof Integer) {
            return ((Integer) expand).intValue() != 0;
        }
        else if (expand == null) {
        	return false;
        }
        throw new MalformedExpressionException("Not a valid boolean expression: "
                + expression
                + "(expands to "
                + expand
                + ")");
    }

    // See comments in interface
    @Override
    public int evaluateInteger (String expression, IEnvironment env) throws MalformedExpressionException {
        Object expand = evaluate(expression, env);
        if (expand instanceof String) {
            try {
                return Integer.decode((String) expand).intValue();
            }
            catch (NumberFormatException x) {
            } // caught below
        }
        else if (expand instanceof Integer) {
            return ((Integer) expand).intValue();
        }
        throw new MalformedExpressionException("Not a valid integer expression: " + expression);
    }

    @Override
    public Object evaluateExpression (String expression, IEnvironment env) throws MalformedExpressionException {
        return evaluate(expression, env);
    }

    // See comments in interface
    @Override
    public String evaluateStringExpression (String expression, IEnvironment env) throws MalformedExpressionException {
        Object expand = evaluate(expression, env);
        if (expand instanceof String) {
            return (String) expand;
        }
        throw new MalformedExpressionException("Not a valid string expression: "
                + expression
                + "; returned value is "
                + (expand != null ? "instance of " + expand.getClass().getName() : "<null>"));
    }

    /**
     * Parse and evaluate an expression
     */
    private Object evaluate (String expression, IEnvironment env) throws MalformedExpressionException {
        Object o = parse(expression, false);
        return evaluate(o, env);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object evaluate (Object o, IEnvironment env) throws MalformedExpressionException {
        if (o instanceof String) {
            String s = (String)o;
            Object so = expandString(s, env);
            if (so instanceof List)
                o = so;
            else
                return so;
        }
        if (o instanceof List) {
            o = mLisp.lispEval((List<Object>) o, this, env);
            //
            // We can have symbols that expand into lisp expression.
            // E.g.
            // FOO="$(...)"
            // BAR=$( if 1 $FOO )
            // Check for this.
            //
            if (o instanceof String) {
                String s = (String) o;
                if (s.length() > 2 && s.charAt(0) == '$' && s.charAt(1) == '(')
                    o = evaluate(s, env);
            }
            return o;
        }
        if (o instanceof Boolean){
            return o;
        }
        throw new MalformedExpressionException("Assertion failure: not a string or list");
    }
    
    private int recurseLevel = 0;

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> evaluateList (String string, IEnvironment env) throws MalformedExpressionException {
    	 Object so = null;
         string = string.trim();
         if (string.startsWith("$(")){
             so = parseList(string);
         }
         else if (string.startsWith("$")){
             int i = 1;
             while (i < string.length() && Character.isJavaIdentifierPart(string.charAt(i))){
                 i++;
             }
             if (i == string.length()){
                 so = env.getSymbolValue(string.substring(1));
                 if (so instanceof String && recurseLevel < 50){
                     try{
                         recurseLevel++;
                         so = this.evaluateList((String)so,env);
                     }
                     finally {
                         recurseLevel--;
                     }
                 }
             }
         }
         if (so == null)
             so = expandString(string, env);
        if (so instanceof List)
            return (List<Object>) so;
        return Collections.singletonList(so);
    }

    private transient int mDepth; // Tracks recursion in expandString();

    /**
     * Expand the contents of a string.
     * <P>
     * Substitution:
     * <dl>
     * <dt>$arg
     * <dd>subjected to the substitution Environment.
     * <dt>$arg$)foo
     * <dt>$arg$>foo
     * <dd>Expands <code>arg</code> and then appends <code>foo</code>.
     * <dt>prefix%abc%suffix
     * <dd>same as "prefix$abc$)suffix", I think.
     * </dl>
     * <p>
     * NOTE: much of the method was taken directly from Tom's t2.java version.
     * @param s the string to be expanded.
     * @return the expanded string, or, for the case of "$foo", the object denoted by "foo".
     */
    @Override
    public Object expandString (String s, IEnvironment env) throws MalformedExpressionException {
        // System.out.println("expandString(" + s + ")");
        // A lookup environment is supposed to transitively lookup
        // a variable.
        // If the string is just one variable that results in an object,
        // return the object, even if not a string.
        if (s == null) {
            return s;
        }
        mDepth++;
        if (mDepth >= 50) {
            throw new MalformedExpressionException("String has recursion: " + s);
        }
        // Keep replacing %X% until there are no more of them.
        // Scan the string for %XXX% where XXX is in the environment.
        int max = s.length();
        List<Object> resultList = null; // Non-null if we're returning a list to be evaluated.
        for (int i = 0; i < max; i++) {
            int pct = s.indexOf('%', i);
            if (pct == -1) {
                break;
            }
            int pct2 = s.indexOf('%', pct + 1);
            if (pct2 != -1) {
                String lookup = s.substring(pct + 1, pct2);
                String val = (String) env.expand(lookup, this);
                if (val != null) {
                    // val = val.trim();
                    s = s.substring(0, pct) + val + s.substring(pct2 + 1);
                }
                else
                    // throw new MalformedExpressionException
                    System.err.println("Symbol \"" + lookup + "\" not defined in \"" + s + "\"");
            }
            // I think this isn't quite right: should account for length
            // of val.
            i = pct + 1;
        }
        // Now look for $name with optional *s after $ for further indirection.
        // To get a $ you must escape it: \$.
        // System.out.println("expandString is about to scan " + s);
        max = s.length();
        int first = 0;
        for (int i = 0; i < max;) {
            // System.out.println("expandString continues with " + s.substring(i));
            int dlr = s.indexOf('$', i);
            if (dlr == -1) {
                // append rest of string
                if (resultList != null && first < max)
                    resultList.add(s.substring(first, max));
                break;
            }
            // System.out.println("found $ in " + s.substring(i));
            if (dlr == 0 && max > 1 && s.charAt(1) == '(') {
                i = 2;
                continue; // Lisp function
            } 
            // $$foo --> $foo
            if (dlr+1 < max && s.charAt(dlr+1) == '$'){
                s = s.substring(0,dlr) + s.substring(dlr+1);
                i = dlr+1;
                continue;
            }
            int end = dlr + 1;
            int len = s.length();
            int indirect = 1;
            while (end < len && s.charAt(end) == '*') {
                // Scan leading *s.
                indirect++;
                end++;
            }
            int start_id = end;
            String lookup = null;
            while (end < len) {
                // Scan identifier.
                char C = s.charAt(end);
                if (Character.isLetterOrDigit(C) || C == '_') {
                    end++;
                }
                else {
                    break;
                }
            }
            lookup = s.substring(start_id, end);
            Object ans = null;
            for (int j = 1; j <= indirect; j++) {
                ans = env.expand(lookup, this);
                // System.out.println("lookup of \"" + lookup + "\" returns " + ans );
                // if (ans !=null) System.out.println("instance of " + ans.getClass().getName());
                if (ans instanceof String) {
                    String ss = (String) ans;
                    if (ans.equals(lookup) && indirect > 1) {
                        // We have recursion.
                        lookup = null;
                        break;
                    }
                    else if (ss.length() > 2 && ss.charAt(0) == '$' && ss.charAt(1) == '(') {
                        ans = parse(ss, false); // an expression
                        lookup = null;
                    }
                    else {
                        lookup = ss;
                        ans = null;
                    }
                    // .trim();
                }
                else {
                    if (ans instanceof Integer || ans instanceof Boolean) {
                        lookup = ans.toString();
                        ans = null;
                    }
                    else
                        lookup = null;
                    break;
                }
            }
            // If $) or $> follows the symbol, it's a break; discard it only
            // when you successfully substitute the former.
            // E.g. $X$)abc => $X plus abc; but if you don't substitute
            // $X this round, leave the $) there.
            boolean skip_2_more = end + 2 < len
                    && s.charAt(end) == '$'
                    && (s.charAt(end + 1) == ')' || s.charAt(end + 1) == '>');
            if (skip_2_more) {
                end += 2;
            }
            if (ans != null) {
                // System.out.println("expandString() return " + ans);
                if (resultList == null) {
                    resultList = new ArrayList<Object>();
                    resultList.add("cat");
                }
                if (dlr > first) {
                    resultList.add(s.substring(first, dlr));
                }
                first = end;
                resultList.add(ans);
            }
            else if (lookup != null) {
                if (resultList != null) {
                    if (dlr > first)
                        resultList.add(s.substring(first, dlr));
                    first = end;
                    resultList.add(lookup);
                }
                else {
                    s = s.substring(0, dlr) + lookup + s.substring(end);
                    max = s.length();
                    end = dlr + lookup.length();
                }
            }
            i = end;
        }
        mDepth--;
        if (resultList != null) {
            if (resultList.size() == 2) // ( cat x ) --> x
                return resultList.get(1);
            // System.out.println("expandString() returns " + resultList);
            return resultList;
        }
        return s;
    }

    // see comments in interface
    @Override
    public Object parseAction (String expression) throws MalformedExpressionException {
        return parse(expression, false);
    }

    // see comments in interface
    @Override
    public Object parseList (String expression) throws MalformedExpressionException {
        return parse(expression, true);
    }

    /**
     * Evaluate a quoted string by removing quotes and expanding escape sequences.
     * @param quoted quoted string to scan, starting at "index"
     * @param buf the buffer into which to evaluate the string.
     * @param index the starting position in "quoted".
     * @return the index of the character following the terminating quote.
     */
    private int parseString (String quoted, StringBuffer buf, int index) throws MalformedExpressionException {
        int i = index;
        int len = quoted.length();
        char c = '\0';
        while ( ++i < len) {
            c = quoted.charAt(i);
            if (c == '"') {
                ++i;
                break;
            }
            if (c == '\\' && i + 1 < len) {
                c = quoted.charAt( ++i);
            }
            buf.append(c);
        }
        if (c != '"')
            throw new MalformedExpressionException("Not valid quoted string: " + quoted.substring(index));
        return i;
    }

    /**
     * Parse an expression.
     * <P>
     * If a simple identifier, return the identifier.
     * <P>
     * If a lisp expression, then create a List object. Example of a List object:
     * 
     * <pre>
     * 
     *    $( if ( eval x ) &quot;abc&quot; &quot;def&quot; )
     *  &lt;/pre.
     * 
     */
    private Object parse (String expression, boolean isList) throws MalformedExpressionException {
        // if we have no list or strings, then just return it
        String s = expression.trim();
        if (s.startsWith("$("))
            return parseLisp(s.substring(1), isList);
        if (s.startsWith("\"")) {
            StringBuffer buf = new StringBuffer(s.length());
            int i = parseString(s, buf, 0);
            if (i < s.length())
                throw new MalformedExpressionException("Junk after quoted string: " + s);
            return buf.toString();
        }
        return expression;
    }

    private Object parseLisp (String expression, boolean isList) throws MalformedExpressionException {
        ArrayList<Object> list = new ArrayList<Object>();
        expression = expression.trim();
        int i = parseLisp(expression, 0, list, isList);
        if (i < expression.length())
            throw new MalformedExpressionException("Garbage after Lisp expression: \""
                    + expression.substring(i)
                    + "\" in \""
                    + expression
                    + "\"");
        return list;
    }

    /**
     * Parse a lisp expression where the first character is known to be '('.
     * @param expression that expression
     * @param index the index of the first character of what we're scanning.
     * @param list the list to append to.
     * @param isList will be treated as list; not a function.
     * @return index of the character beyond the expression.
     */
    private int parseLisp (String expression, int index, List<Object> list, boolean isList) throws MalformedExpressionException {
        if (expression.charAt(index) != '(')
            throw new Error("Assertion failure");
        int len = expression.length();
        int i = index + 1;
        boolean first = true;
        while (i < len) {
            char c = expression.charAt(i);
            while (Character.isWhitespace(c) && ++i < len)
                c = expression.charAt(i);
            if (c == ')') {
                return i + 1;
            }
            else if (c == '(') {
                ArrayList<Object> childList = new ArrayList<Object>();
                i = parseLisp(expression, i, childList, false);
                list.add(childList);
            }
            else if (c == '"') {
                // As a consession to old Guihili,
                // interpret ( "abc" ...) as ( list "abc" ... )
                if (first && !isList) {
                    list.add("list");
                }
                StringBuffer buf = new StringBuffer();
                i = parseString(expression, buf, i);
                list.add(buf.toString());
            }
            else if (Character.isJavaIdentifierPart(c) || c == '-' || c == '$') {
                StringBuffer buf = new StringBuffer();
                while ( ++i < len) {
                    buf.append(c);
                    c = expression.charAt(i);
                    if (!Character.isJavaIdentifierPart(c) && c != '$')
                        break;
                    // Take into account " ( $foo$)bar ... )" .
                    if (c == '$' && i + 1 < len && expression.charAt(i + 1) == ')')
                        buf.append(expression.charAt( ++i));
                }
                list.add(buf.toString());
            }
            else
                throw new MalformedExpressionException("Garbage in Lisp expression: " + expression.substring(i));
            first = false;
        }
        throw new MalformedExpressionException("Lisp expression not terminated: " + expression.substring(index));
    }

    private ILisp mLisp;
}
