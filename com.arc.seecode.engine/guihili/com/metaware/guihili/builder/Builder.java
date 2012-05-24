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
package com.metaware.guihili.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Attribute;
import org.xml.sax.SAXException;

import com.arc.xml.AbstractBuilder;
import com.arc.xml.Element;
import com.arc.xml.IAttributeDef;
import com.arc.xml.IBinding;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.MalformedExpressionException;
/**
 * The base class of all Builder classes.
 * It has the common behavior of remembering the associated 
 * GUI object, and for adding and setting generic attributes.
 * <P>
 * This class would ordinarily be package-private, but is made public
 * because its methods are accessed by means of reflection and because
 * it is subclassed in a sub-package.
 * <P>
 * @author J. David Pickens
 * @version 7/9/99
 */
public abstract class Builder extends AbstractBuilder {
    public Builder(Gui gui) {
        _gui = gui;
    }

    /**
     * The "legacy=1" attribute is implicitly inserted by the
     * guihili parser if it sees a component following the old
     * Guihili syntax. The old Guihili syntax had
     * the form (component=button ...).
     * For such cases, we treat all unknown attributes as symbols.
     */
    public void setLegacy(boolean b) {
        treatUnknownAttributesAsSymbols(b);
    }

    protected void treatUnknownAttributesAsSymbols(boolean f) {
        mTomStyle = f;
    }

    /**
     * We override this method to permit undeclared attribute definitions
     * if we're in "tom" mode. The old guihili treats all attributes
     * as environment assignments.
     */
    @Override
    protected void unknownAttribute(Element e, IBinding b, Attribute a)
        throws SAXException {
        // legacy=1 is inserted implicitly by guihili parser.
        if (a.getName().equals("legacy") && a.getValue().equals("1")) {
            setLegacy(true);
            return;
        }

        if (mTomStyle) {
            newEnvironment();
            _gui.getEnvironment().putSymbolValue(
                a.getName(),
                a.getValue());
        }
        else
            super.unknownAttribute(e, b, a);
    }

    /**
     * Called to establish a new lookup environment.
     * The {@link ContainerBuilder} subclass may call it if it detects
     * "set" operation.
     */
    protected void newEnvironment() {
        if (mSavedEnv == null) {
            mSavedEnv = _gui.getEnvironment();
            _gui.setEnvironment(Environment.create(mSavedEnv));
        }
    }

    /**
     * Restore previous lookup environment if one was created.
     */
    @Override
    protected void cleanup() {
        if (mSavedEnv != null) {
            _gui.setEnvironment(mSavedEnv);
            mSavedEnv = null;
        }
        super.cleanup();
    }
    /**
     * Evaluate an expression that is the value of an attribute.
     */
    @Override
    protected Object valueOf(String value, IAttributeDef adef)
        throws SAXException {
        IEvaluator eval = _gui.getEvaluator();
        IEnvironment env = _gui.getEnvironment();
        // If no evaluator, we assume it has nothing to evaluate
        // the framework will convert it
        if (eval == null)
            return value;
        try {
            switch (adef.getType()) {
                case IAttributeDef.STRING :
                    if (adef.delayEvaluation())
                        return value;
                    return eval.evaluateStringExpression(value, env);
                case IAttributeDef.INT :
                    return new Integer(eval.evaluateInteger(value, env));
                case IAttributeDef.BOOLEAN :
                    return new Boolean(eval.evaluateBoolean(value, env));
                case IAttributeDef.LIST :
                    {
                        Object o = eval.parseList(value);
                        // System.out.println("LIST: " + value + "-->" + o);
                        if (o instanceof String) {
                            List<Object> l = eval.evaluateList((String) o, env);
                            // System.out.println("    LIST: " + o + "-->" + l);
                            if (l != null && l.size() == 1)
                                o = l.get(0);
                            else
                                o = l;
                        }
                        // If a string, then assume separated by spaces.
                        if (o instanceof String) {
                            ArrayList<String> l = new ArrayList<String>();
                            StringTokenizer each =
                                new StringTokenizer((String) o);
                            while (each.hasMoreTokens())
                                l.add(each.nextToken());
                            o = l;
                        }
                        // System.out.println("    LIST: returns " + o );
                        return o;
                    }
                case IAttributeDef.ACTION :
                    return eval.parseAction(value);
                default :
                    return eval.evaluateExpression(value, env);
            }
        }
        catch (MalformedExpressionException x) {
            throw new SAXException(value, x);
        }
    }

    protected Gui _gui;
    private boolean mTomStyle; // Treat attributes as environment symbols
    private IEnvironment mSavedEnv; // to be restored
}
