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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.arc.mw.util.Toggle;
import com.arc.widgets.IButton;
import com.arc.widgets.IChoice;
import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILabel;
import com.arc.widgets.ITextField;
import com.metaware.guihili.Coerce;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.MalformedExpressionException;

/**
 * Given a component, make sure it is enabled or disabled appropriately. Also
 * controls visibility. The "enabled" property of component is controlled by
 * three things:
 * <P>
 * <ol>
 * <li>If the parent container is disabled, then the component is disabled.
 * <li>If an "enable_if" disabled this component then it is disabled.
 * <li>If a bound boolean property is false, then this component is disabled.
 * </ol>
 * Otherwise, the component is enabled.
 *  
 */
public class EnableMonitor {

    private Object mEnableIf;

    private Object mVisibleIf;

    private Gui mGui;

    private IEnvironment mEnv;

    private IComponent mComponent;

    private String mProperty; // boolean property that controls

    private boolean mPropertyValue;
    
    private List<IEnableObserver> _observers = null;
    

    // value of property that enables this component
    private boolean mListensForPropertyChanges;

    private static Toggle sTrace = Toggle.define("TRACE_ENABLE", false);

    interface IEnableObserver{
        void enablePropertyChanged(IComponent c);
    }
    /**
     * @param c
     *            the component whose enabled property is being controlled.
     * @param gui
     *            the associated manager.
     */
    public EnableMonitor(IComponent c, Gui gui) {
        mGui = gui;
        mComponent = c;
        mEnv = gui.getEnvironment();
    }
    
    /**
     * Add an observer when a component is enabled or disabled.
     * @param observer
     */
    public synchronized void addObserver(IEnableObserver observer){
        if (_observers == null) _observers = new ArrayList<IEnableObserver>();
        _observers.add(observer);        
    }
    
    public synchronized void removeObserver(IEnableObserver observer){
        if (_observers != null){
            _observers.remove(observer);
        }
    }

    /**
     * Set "enable_if" expression that controls this component
     */
    public void setEnableIf(Object eif) {
        mEnableIf = eif;
        if (tracing()) trace("enable_if for " + getName() + " set to " + eif);
        if (mEnableIf != null) {
            assureListenerIsWired();
        }
    }

    private void assureListenerIsWired() {
        if (!mListensForPropertyChanges) {
            // We must re-evaluate "enable_if" each time a property changes
            mGui.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    update();
                }
            });
            mListensForPropertyChanges = true;
        }
    }

    /**
     * Set expression that determines whether or not the component is visible.
     */
    public void setVisibleIf(Object eif) {
        mVisibleIf = eif;
        if (eif != null) {
            assureListenerIsWired();
        }
        update();
    }

    /**
     * Return "enable_if" expression that controls this component
     */
    public Object getEnableIf() {
        return mEnableIf;
    }

    /**
     * return property that controls the enabled property of this component.
     */
    public String getProperty() {
        return mProperty;
    }

    /**
     * Return whether or not the "enabled" property is negated from the value of
     * the controlling property.
     */
    public boolean isNegated() {
        return !mPropertyValue;
    }

    /**
     * Set property string that controls this component.
     * 
     * <pre>
     * 
     *  prop -&gt; name
     *       -&gt; !name
     *  
     * </pre>
     */
    public void setProperty(String string) {
        if (string == null || string.length() == 0)
                throw new IllegalArgumentException("invalid property string");
        boolean flag = true;
        if (string.charAt(0) == '!') {
            string = string.substring(1);
            flag = false;
        }
        wireProperty(string, flag);
    }

    private void wireProperty(final String prop, final boolean onValue) {
        if (!mListensForPropertyChanges)
                mGui.addPropertyChangeListener(prop,
                        new PropertyChangeListener() {

                            @Override
                            public void propertyChange(PropertyChangeEvent event) {
                                if (tracing())
                                        trace("Property " + prop
                                                + " causes update for "
                                                + getName());
                                update();
                            }
                        });
        mProperty = prop;
        mPropertyValue = onValue;
        update();
    }

    /**
     * Return name of component of error tracing
     */
    private String getName() {
        return getName(mComponent);
    }

    private static String getName(IComponent c) {
        if (c.getName() != null) return c.getName();
        if (c instanceof IButton) return "button " + ((IButton) c).getText();
        if (c instanceof ITextField)
                return "textfield " + ((ITextField) c).getText();
        if (c instanceof IChoice) return "combobox " + ((IChoice) c).getText();
        if (c instanceof ILabel) return "label " + ((ILabel) c).getText();
        return c.getComponent().getClass().getName();
    }

    private static int sIndent;

    /**
     * Enable or disable the associated component appropriately.
     */
    public void update() {
        sIndent++;
        try {
            if (sIndent > 50){
                throw new MalformedExpressionException("Loop in 'enabled' attribute; property=\""+
                        mProperty);
            }
            // If invisible then we're done.
            if (mVisibleIf != null && mGui.isInitialized()) {
                boolean v = Coerce.toBoolean(mGui.getEvaluator().evaluate(
                        mVisibleIf, mEnv));
                if (tracing())
                        trace("VisibleIf for " + getName() + " evaluates to "
                                + v);
                mComponent.setVisible(v);
            }
            //
            // If parent is disabled, then so are we.
            //
            IContainer parent = mComponent.getParent();
            if (parent != null && !parent.isEnabled()) {
                if (tracing())
                        trace(getName() + " disabled because parent was.");
                setEnabled(false);
                return;
            }
            //
            // Check if "enable_if" disables us.
            //

            // enable_if expressions can have forward refs; not valid until
            // GUI fully initialized.
            if (mEnableIf != null && mGui.isInitialized()) {
                boolean v = Coerce.toBoolean(mGui.getEvaluator().evaluate(
                        mEnableIf, mEnv));
                if (tracing())
                        trace("EnableIf for " + getName() + " evaluates to "
                                + v);
                if (!v) {
                    setEnabled(false);
                    return;
                }
            }

            //
            // If we're controlled by a property, see if it disables us.
            //
            if (mProperty != null) {
                Object v = mGui.getProperty(mProperty);
                boolean value;
                if (v instanceof String) {
                    String sv = (String) v;
                    value = sv.length() > 0 && !sv.equals("0")
                            && !sv.equalsIgnoreCase("false");
                } else if (v instanceof Boolean)
                    value = ((Boolean) v).booleanValue();
                else if (v instanceof Integer)
                    value = ((Integer) v).intValue() != 0;
                else
                    value = v != null;
                if (tracing())
                        trace(getName() + " controlled by property named "
                                + mProperty + "=" + value);
                if (value != mPropertyValue) {
                    if (tracing()) trace("Property disables " + getName());
                    setEnabled(false);
                    return;
                }
            }
            setEnabled(true);
        } catch (MalformedExpressionException e) {
            mGui.handleException(e);
        } finally {
            sIndent--;
        }
    }
    
    private void notifyObservers(IComponent c){
        IEnableObserver[] observers;
        synchronized(this){
            if (_observers == null || _observers.size() == 0) return;
            observers = _observers.toArray(new IEnableObserver[_observers.size()]);
        }
        for (int i = 0; i < observers.length; i++){
            observers[i].enablePropertyChanged(c);
        }
    }

    /**
     * Set the "enabled" property for the component associated with this
     * instance. If it is a container, then recursively process the children.
     */
    private void setEnabled(boolean b) {
        setEnabled(mComponent, b);
    }

    /**
     * Set the "enabled" property for the given component. If it is a container,
     * then recursively process the children. If the child has an EnableMonitor,
     * then call its "update()" method to set things.
     */
    private void setEnabled(IComponent c, boolean b) {
        if (c.isEnabled() != b) {
            if (tracing())
                    trace(getName(c) + ".enabled=" + b + "  (instanceof "
                            + c.getClass().getName() + ")");
            c.setEnabled(b);
            if (c instanceof IContainer) {
                if (tracing())
                        trace(getName(c)
                                + " is a panel; will propagate enable=" + b);
                IContainer panel = (IContainer) c;
                IComponent[] children = panel.getChildren();
                for (int i = 0; i < children.length; i++) {
                    IComponent child = children[i];
                    EnableMonitor em = mGui.getEnableMonitor(child);
                    if (tracing())
                            trace("    child of " + getName(c) + " is "
                                    + getName(child));
                    if (em != null)
                        em.update();
                    else
                        setEnabled(child, b);
                }
            }
            notifyObservers(c);
        } else if (tracing()) {
            if (tracing())
                    trace(getName(c) + " already has enabled=" + b
                            + "  (instanceof " + c.getClass().getName() + ")");
        }
    }

    private boolean tracing() {
        return sTrace.on();
    }

    private void trace(String msg) {
        if (tracing()) {
            System.out.print("[ENABLE]");
            for (int i = 0; i < sIndent; i++)
                System.out.print("..");
            System.out.println(msg);
        }
    }
}
