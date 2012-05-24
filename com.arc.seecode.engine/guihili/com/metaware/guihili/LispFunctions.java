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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.arc.mw.util.IPropertyMap;
import com.arc.widgets.IButton;
import com.arc.widgets.IChoice;
import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.ITextField;
import com.metaware.guihili.builder.Environment;

/**
 * A collection of Lisp function implementations for an expression evaluator.
 * <P>
 * Methods in this class are accessed by reflection by the Lisp interpreter.
 * <P>
 * Given a Lisp expression:
 * 
 * <pre>
 * 
 *     ( &lt;i&gt;func operand1 operand2 ... &lt;/i&gt; )
 *  
 * </pre>
 * 
 * the Lisp interpreter will look for a method <code>do_<i>func</i></code>
 * with the following signature:
 * 
 * <pre>
 * 
 *     Object do_&lt;i&gt;func&lt;/i&gt;( List<Object> list, IEvaluator eval, IEnvironment env )
 *  
 * </pre>
 * 
 * When found, it will invoke the method to compute expression. The list that is
 * passed to the method includes the function itself as the first element (a
 * string).
 * <P>
 * Because reflection is used to access the methods of this class, they must all
 * be public.
 * <P>
 * 
 * @author David Pickens
 */
public class LispFunctions {
    /**
     * 
     * @param mapper
     *            Retrieves component from name.
     * @param props
     *            retrieves property values from property names.
     * @param ehandler
     *            callback in case of exception.
     * @param msgDialog
     *            callback in case we need to display an error dialog.
     */
    public LispFunctions(IComponentMap mapper, IPropertyMap props,
            IExceptionHandler ehandler, IMessageDialog msgDialog) {
        mComponents = mapper;
        mProperties = props;
        mExceptionHandler = ehandler;
        mMessageDialog = msgDialog;
    }

    /**
     * Handle "exists" function.
     * 
     * <pre>
     * &quot;(exists var_name)&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_exists(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() != 2)
            badCount(list);
        Object o = eval.evaluate(list.get(1), env);
        if (!(o instanceof String))
            error("Operand of 'exists' must be variable name");
        return new Boolean(env.getSymbolValue((String) o) != null);
    }

    /**
     * Handle "if" function.
     * 
     * <pre>
     * &quot;(if &lt;expression&gt; &lt;then&gt; [&lt;else&gt;])&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_if(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() < 3 || list.size() > 4)
            badCount(list);
        boolean b;
        try {
            b = evaluateBoolean(list.get(1), eval, env);
        } catch (MalformedExpressionException x) {
            //System.err.println("Bad boolean: env is " + env);
            //System.out.flush();
            throw new MalformedExpressionException(
                    "\"if\" conditional is not a boolean: " + x.getMessage());
        }
        if (b)
            return eval.evaluate(list.get(2), env);
        if (list.size() == 4)
            return eval.evaluate(list.get(3), env);
        return null;
    }

    /**
     * Handle "equals" function.
     * 
     * <pre>
     * &quot;(equals a b)&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_equals(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        // System.out.println("do_equals: " + list);
        if (list.size() != 3)
            badCount(list);
        Object a = eval.evaluate(list.get(1), env);
        Object b = eval.evaluate(list.get(2), env);
        boolean result = a == null ? (b == null ? true : false) : a.equals(b);
        // System.out.println(" do_equals " + a + " " + b + " returns " +
        // result);
        return result ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Handle "not" function.
     * 
     * <pre>
     * &quot;(not a)&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_not(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() != 2)
            badCount(list);
        boolean a = evaluateBoolean(list.get(1), eval, env);
        return new Boolean(!a);
    }

    /**
     * Handle 'select' function.
     * 
     * <pre>
     * &quot;(select &lt;i&gt;component-name&lt;/i&gt; &lt;i&gt;property&lt;/i&gt;)&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_select(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() != 3)
            badCount(list);
        String compName = evaluateString(list.get(1), eval, env);
        String propName = evaluateString(list.get(2), eval, env);
        IComponent c = mComponents.getComponent(compName);
        if (c instanceof IContainer && !(c instanceof ISelectable)){
            // Probably a compound element like a labeled combobox...
            IComponent kids[] =  ((IContainer)c).getChildren();
            if (kids.length > 0){
                c = kids[kids.length-1];
            }
        }
        if (c == null) {
            error("Component \"" + compName + "\" doesn't exist");
            return null;
        }
        if (c instanceof ISelectable) {
            return ((ISelectable) c).getSelection(propName);
        }
        // There are sufficiently few properties to check for, that we itemize
        // them
        // here.
        if (propName.equals("text")) {
            if (c instanceof IChoice)
                return ((IChoice) c).getText();
            if (c instanceof ITextField)
                return ((ITextField) c).getText();
            error("Component \"" + compName + "\" does not have a 'text' property");
        } else if (propName.equals("value")) {
            if (c instanceof IButton) {
                return ((IButton) c).isSelected() ? "1" : "0";
            }
            error("Component \"" + compName + "\" does not have a 'value' property");
        } else if (propName.equals("texts")) {
            if (c instanceof IChoice) {
                IChoice cc = (IChoice) c;
                String array[] = new String[cc.getItemCount()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = cc.getItemAt(i).toString();
                }
                return array;
            }
            error("Component \"" + compName + "\" does not have a 'texts' property");
        } else if (propName.equals("size")) {
            if (c instanceof IChoice) {
                IChoice cc = (IChoice) c;
                return new Integer(cc.getItemCount());
            }
            error("Component \"" + compName + "\" does not have a 'size' property");
        }
        else 
            return "<Can't select \"" + propName + "\" from instance of " + c.getClass().getName() + ">";
        return null;
    }
    
    /**
     * Handle 'setfield' function.
     * 
     * <pre>
     * &quot;(setfield <i>component-name</i>; <i>property</i> <i>value</i>)&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_setfield(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() != 4)
            badCount(list);
        String compName = evaluateString(list.get(1), eval, env);
        String propName = evaluateString(list.get(2), eval, env);
        String value = evaluateString(list.get(3), eval, env);
        IComponent c = mComponents.getComponent(compName);
        if (c instanceof IContainer && !(c instanceof ISelectable)){
            // Probably a compound element like a labeled combobox...
            IComponent kids[] =  ((IContainer)c).getChildren();
            if (kids.length > 0){
                c = kids[kids.length-1];
            }
        }
        if (c == null) {
            error("Component \"" + compName + "\" doesn't exist");
            return null;
        }
        if (c instanceof ISelectable) {
            ((ISelectable) c).putSelection(propName,value);
        }
        else
        // There are sufficiently few properties to check for, that we itemize
        // them
        // here.
        if (propName.equals("text")) {
            if (c instanceof IChoice)
                ((IChoice) c).setSelection(value);
            else
            if (c instanceof ITextField)
                ((ITextField) c).setText(value);
            else
               error("Component \"" + compName + "\" does not have a 'text' property");
        } else if (propName.equals("value")) {
            if (c instanceof IButton) {
                ((IButton) c).setSelected(value != null && 
                        (value.equals("1") || value.equalsIgnoreCase("true")));
            }
            else
                error("Component \"" + compName + "\" does not have a 'value' property");
        } 
        else 
            error("Component \"" + compName + "\" does not have a '" + propName + "' property");
        return null;
    }

    /**
     * Handle 'getenv' function.
     * 
     * <pre>
     * &quot;(getenv &lt;i&gt;symbol-name&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_getenv(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() != 2)
            badCount(list);
        String name = evaluateString(list.get(1), eval, env);
        return env.getenv(name);
    }

    /**
     * Handle "property" function.
     * 
     * <pre>
     * &quot;(property &lt;i&gt;name&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_property(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() != 2)
            badCount(list);
        String name = evaluateString(list.get(1), eval, env);
        Object result = mProperties.getProperty(name);
        // System.out.println("(property " + name + ") returns " + result);
        return result;
    }

    /**
     * Handle "enabled" function.
     * 
     * <pre>
     * &quot;(enabled &lt;i&gt;component-name&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_enabled(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() != 2)
            badCount(list);
        String name = evaluateString(list.get(1), eval, env);
        IComponent c = mComponents.getComponent(name);
        if (c == null) {
        	// If no component by that name, then allow a boolean property
        	Object v = mProperties.getProperty(name);
        	if (v != null)
        		return evaluateBoolean(v,eval,env);
            error("Not a valid component name: " + name);
            return null;
        } else if (c instanceof IButton) {
            IButton b = (IButton) c;
            return new Boolean(b.isEnabled() && b.isSelected());
        } else {
            return new Boolean(c.isEnabled());
        }
    }

    /**
     * Handle "or" function.
     * 
     * <pre>
     * &quot;(or &lt;i&gt;operands...&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_or(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        int cnt = list.size();
        if (cnt <= 2)
            badCount(list);
        for (int i = 1; i < cnt; i++) {
            if (evaluateBoolean(list.get(i), eval, env))
                return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Handle "and" function.
     * 
     * <pre>
     * &quot;(and &lt;i&gt;operands...&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_and(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        int cnt = list.size();
        if (cnt <= 2)
            badCount(list);
        for (int i = 1; i < cnt; i++) {
            if (!evaluateBoolean(list.get(i), eval, env))
                return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * Handle null function.
     * 
     * <pre>
     * &quot;(null &lt;i&gt;operand&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_null(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        int cnt = list.size();
        if (cnt != 2)
            badCount(list);
        Object o = eval.evaluate(list.get(1), env);
        if (o == null || o instanceof String && ((String) o).length() == 0)
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    /**
     * Handle value_matches function.
     * 
     * <pre>
     * &quot;(value_matches &lt;i&gt;operand&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter. NOTE: this is misnamed! Should
     * be "value_contains_substring"
     */
    public Object do_value_matches(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        int cnt = list.size();
        if (cnt != 2)
            badCount(list);
        Object o = eval.evaluate(list.get(1), env);
        Object value = eval.expandString("$VALUE", env);
        if (value == null)
            return new Boolean(o != null);
        if (value instanceof String && o instanceof String) {
            return ((String) value).indexOf((String) o) >= 0 ? Boolean.TRUE
                    : Boolean.FALSE;
        }

        return new Boolean(value.equals(o));
    }

    /**
     * Handle set function.
     * 
     * <pre>
     * &quot;(set &lt;i&gt;name&lt;/i&gt; &lt;i&gt;value&lt;i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_set(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        int cnt = list.size();
        if (cnt != 3)
            badCount(list);
        String name = evaluateString(list.get(1), eval, env);
        Object value = eval.evaluate(list.get(2), env);
        env.putSymbolValue(name, value);
        return null;
    }

    /**
     * Handle list function.
     * 
     * <pre>
     * &quot;(list &lt;i&gt;operands...&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_list(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        int cnt = list.size();
        ArrayList<Object> result = new ArrayList<Object>(cnt);
        for (int i = 1; i < cnt; i++) {
            Object element = eval.evaluate(list.get(i),env);
            if (element != null)
                result.add(element);
        }
        return result;
    }

    /**
     * Handle print function.
     * 
     * <pre>
     * &quot;(print &lt;i&gt;msg...&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_print(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        String msg = evaluateString(list.get(1), eval, env);
        System.out.println(msg);
        return null;
    }

    /**
     * Handle oops function.
     * 
     * <pre>
     * &quot;(oops &lt;i&gt;msg...&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_oops(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() < 2)
            badCount(list);
        String msg = evaluateString(list.get(1), eval, env);
        mMessageDialog.showErrorDialog(msg);
        return null;
    }
    
    /**
     * Handle info function.
     * 
     * <pre>
     * &quot;(info &lt;i&gt;msg...&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_info(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() < 2)
            badCount(list);
        String msg = evaluateString(list.get(1), eval, env);
        mMessageDialog.showMessageDialog(msg);
        return null;
    }

    /**
     * Handle file_exists function.
     * 
     * <pre>
     * &quot;(file_exists &lt;i&gt;filename&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_file_exists(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        int cnt = list.size();
        if (cnt != 2)
            badCount(list);
        String filename = evaluateString(list.get(1), eval, env);
        File f = new File(filename);
        if (!f.isAbsolute()){
            f = new File(env.getWorkingDirectory(),filename);
        }
        if (f.exists())
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    /**
     * Handle cat function.
     * 
     * <pre>
     * &quot;(cat &lt;i&gt;args...&lt;/i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter. Evaluates string opernands and
     * concatenates them into single string.
     */
    public Object do_cat(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        StringBuffer buf = new StringBuffer(200);
        int cnt = list.size();
        for (int i = 1; i < cnt; i++) {
            String s = evaluateString(list.get(i), eval, env);
            if (s != null)
                buf.append(s);
        }
        return buf.toString();
    }

    /**
     * Handle copy_file function.
     * 
     * <pre>
     * &quot;(copy_file &lt;i&gt;from&lt;/i&gt; &lt;i&gt;to&lt;i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_copy_file(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        if (list.size() != 3)
            badCount(list);
        String from = evaluateString(list.get(1), eval, env);
        String to = evaluateString(list.get(2), eval, env);
        if (!new File(from).exists()) {
            JOptionPane.showMessageDialog(null, "File \"" + from
                    + "\" does not exist.", "File missing",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        try {
            // System.out.println("copy_file " + from + " to " + to);
            FileInputStream in = new FileInputStream(from);
            FileOutputStream out = new FileOutputStream(to);
            byte buffer[] = new byte[4096];
            while (true) {
                int cnt = in.read(buffer);
                if (cnt < 0)
                    break;
                out.write(buffer, 0, cnt);
            }
        } catch (Exception x) {
            exception(x);
        }
        return null;
    }

    /**
     * Handle "call" function.
     * 
     * <pre>
     * &quot;(call &lt;i&gt;name&lt;/i&gt; &lt;i&gt;args...&lt;i&gt; )&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter.
     */
    public Object do_call(List<Object> list, IEvaluator eval, IEnvironment env)
            throws MalformedExpressionException {
        int cnt = list.size();
        if (cnt < 2)
            badCount(list);
        String procName = evaluateString(list.get(1), eval, env);
        ProcBody body = env.getProcedure(procName);
        if (body == null) {
            error("Not a recognizable procedure name: " + procName);
            return null;
        }
        IEnvironment newEnv = Environment.create(env);
        newEnv = Environment.create(newEnv, body.getEnvironment());
        //System.out.println("value of VALUE in body of " + procName + " is " +
        // body.getEnvironment().getSymbolValue("VALUE"));
        // System.out.println("(call " + procName );
        //
        // Enter each argement as "ARG<i>", starting at 1.
        //
        for (int i = 2; i < cnt; i++) {
            // System.out.println(" ARG" + (i-1) + "=" + list.get(i));
            newEnv.putSymbolValue("ARG" + (i - 1), list.get(i));
        }
        // System.out.println(")");
        // System.out.println("VALUE in environment is " +
        // newEnv.getSymbolValue("VALUE"));
        Object o = eval.evaluate(body.getBody(), newEnv);
        // System.out.println("Call to " + procName + " evaluates to " + o);
        return o;
    }

    /**
     * Handle "tokenize" function.
     * 
     * <pre>
     * &quot;(tokenize var_name)&quot;
     * </pre>
     * 
     * <p>
     * Invoked by reflection by Lisp interpreter. It takes a string and
     * tokenizes it into a list of strings such that white spaces separates
     * tokens. Tokens containing spaces are assumed quoted. Usual escape
     * sequences are supported (e.g., \").
     */
    public Object do_tokenize (List<Object> list, IEvaluator eval, IEnvironment env) throws MalformedExpressionException {
        if (list.size() != 2)
            badCount(list);
        String string = evaluateString(list.get(1), eval, env);
        ArrayList<String> newList = new ArrayList<String>();
        int i = 0;
        int slen = string.length();
        StringBuffer buf = new StringBuffer(slen);
        final int BETWEEN_TOKENS = 0;
        final int NOT_QUOTED = 1;
        final int SINGLE_QUOTED = 2;
        final int DOUBLE_QUOTED = 3;
        int state = BETWEEN_TOKENS;
        while (i < slen) {
            char c = string.charAt(i);
            switch (c) {
            case '\\': {
                if (state == BETWEEN_TOKENS)
                    state = NOT_QUOTED;
                if (state != SINGLE_QUOTED && state != DOUBLE_QUOTED) {
                    buf.append(c);
                }
                else {
                    if (i + 1 < slen) {
                        switch (string.charAt(i + 1)) {
                        case '"':
                        case '\'':
                        case '\\':
                            buf.append(string.charAt( ++i));
                            break;
                        case 't':
                            buf.append('\t');
                            i++;
                            break;
                        case 'r':
                        case 'n':
                            if (state == SINGLE_QUOTED || state == DOUBLE_QUOTED) {
                                buf.append(string.charAt( ++i) == 'r' ? '\r' : '\n');
                            }
                            else {
                                buf.append('\\');
                            }
                            break;
                        }
                    }
                    else
                        buf.append('\\');
                }
            }
                break;
            case '"':
                if (state == BETWEEN_TOKENS || state == NOT_QUOTED) {
                    state = DOUBLE_QUOTED;
                }
                else if (state == DOUBLE_QUOTED) {
                    state = NOT_QUOTED;
                }
                else
                    buf.append('"');
                break;
            case '\'':
                if (state == BETWEEN_TOKENS || state == NOT_QUOTED) {
                    state = SINGLE_QUOTED;
                }
                else if (state == SINGLE_QUOTED) {
                    state = NOT_QUOTED;
                }
                else
                    buf.append('\'');
                break;
            case ' ':
            case '\t':
                switch (state) {
                case NOT_QUOTED:
                    newList.add(buf.toString());
                    buf.setLength(0);
                    state = BETWEEN_TOKENS;
                    break;
                case BETWEEN_TOKENS:
                    break;
                default:
                    buf.append(c);
                    break;
                }
                break;
            default:
                if (state == BETWEEN_TOKENS)
                    state = NOT_QUOTED;
                buf.append(c);
                break;
            }
            i++;
        }
        if (state != BETWEEN_TOKENS) {
            newList.add(buf.toString());
        }
        return newList;
    }

    public static String evaluateString(Object o, IEvaluator eval,
            IEnvironment env) throws MalformedExpressionException {
        Object r = eval.evaluate(o, env);
        if (r == null)
            return "";
        if (r instanceof String)
            return (String) r;
        throw new MalformedExpressionException(
                "Not a valid string expression: " + r);
    }

    public static boolean evaluateBoolean(Object o, IEvaluator eval,
            IEnvironment env) throws MalformedExpressionException {
        Object r = o!=null?eval.evaluate(o, env):null;
        if (r == null)
            return false;
        if (r instanceof Boolean)
            return ((Boolean) r).booleanValue();
        if (r instanceof Integer)
            return ((Integer) r).intValue() != 0;
        if (r instanceof String) {
            String s = (String) r;
            if (s.equalsIgnoreCase("true"))
                return true;
            if (s.equalsIgnoreCase("false"))
                return false;
            if (s.equals("1"))
                return true;
            if (s.equals("0"))
                return false;
            if (s.length() > 0 && Character.isDigit(s.charAt(0))){
            	try {
					int v = Integer.parseInt(s);
					return v != 0;
				} catch (NumberFormatException e) {
					//Not a valid integer...
				}
            }
        }
        throw new MalformedExpressionException(
                "Not a valid boolean expression: " + r);
    }

    @SuppressWarnings("unchecked")
    public static String encode(Object o) {
        if (o instanceof List) {
            StringBuffer buf = new StringBuffer(50);
            List<Object> l = (List<Object>)o;
            buf.append("(");
            for (int i = 0; i < l.size(); i++) {
                buf.append(encode(l.get(i)));
                buf.append(' ');
            }
            buf.append(")");
            return buf.toString();
        } else if (o == null)
            return "<null>";
        else
            return '"' + o.toString() + '"';
    }

    public static void badCount(List<Object> list) throws MalformedExpressionException {
        String s = encode(list);
        throw new MalformedExpressionException("Bad operand count for " + s);
    }

    public static void error(String msg) throws MalformedExpressionException {
        throw new MalformedExpressionException(msg);
    }

    private void exception(Exception x) throws MalformedExpressionException {
        if (mExceptionHandler != null)
            mExceptionHandler.handleException(x);
        else
            throw new MalformedExpressionException(x.getMessage(), x);
    }

    private IComponentMap mComponents;

    private IPropertyMap mProperties;

    private IExceptionHandler mExceptionHandler;

    private IMessageDialog mMessageDialog;
}
