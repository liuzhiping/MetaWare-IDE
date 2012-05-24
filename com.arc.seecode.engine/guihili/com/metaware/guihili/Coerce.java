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
package com.metaware.guihili;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.arc.mw.util.Cast;


/**
 * Coerced a value into a boolean, int, or string. Or else throws an exception.
 * @author David Pickens
 * @version May 13, 2002
 */
public class Coerce {

    /**
     * Coerce an object to a boolean
     * @param o a value to be coerced to boolean
     */
    public static boolean toBoolean (Object o) throws MalformedExpressionException {
        if (o instanceof Boolean)
            return ((Boolean) o).booleanValue();
        if (o instanceof Integer)
            return ((Integer) o).intValue() != 0;
        if (o instanceof String) {
            String s = (String) o;
            if (s.equalsIgnoreCase("true"))
                return true;
            if (s.equals("0"))
                return false;
            if (s.equalsIgnoreCase("false"))
                return false;
            if (s.equals("1"))
                return true;
        }
        throw new MalformedExpressionException("Not valid boolean: " + o);
    }

    /**
     * Coerce an object to a String
     * @param o a value to be coerced to String
     */
    public static String toString (Object o) {
        if (o == null)
            return "";
        return o.toString();
    }

    /**
     * Coerce an object to a list. We can have procs within lists.
     * @param o an unevaluated value to be coerced to list
     * @param eval the evaluator
     * @param env the symbol-lookup context
     */
    @SuppressWarnings("unchecked")
    public static List<Object> toList (Object o, IEvaluator eval, IEnvironment env) throws MalformedExpressionException {
        if (o instanceof List) {
            List<Object> l = (List<Object>) o;
            if (l.size() == 0)
                return l;
            if (l.get(0) instanceof List) {
                ArrayList<Object> ourList = new ArrayList<Object>();
                for (int i = 0; i < l.size(); i++) {
                    ourList.addAll(toList(l.get(i), eval, env));
                }
                return ourList;
            }
            //
            // KLOODGE: legacy junk permits '( "string" )' to return back as a string.
            // if the the name has white space or "-" in it, then assume that
            if (l.size() == 1 && l.get(0) instanceof String) {
                String s = (String) l.get(0);
                if (s.indexOf(' ') > 0 || s.indexOf('-') > 0 || s.indexOf('$') > 0)
                    o = s;
            }
            o = eval.evaluate(o, env);
            if (o instanceof List)
                return Cast.toType((List<?>) o);
        }
        else
            o = eval.expandString((String) o, env);
        if (o == null)
            return new ArrayList<Object>(0);
        return Collections.singletonList((Object)toString(o));
    }
}
