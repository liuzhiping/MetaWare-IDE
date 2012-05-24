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


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


/**
 * This class represents a "toggle", a named boolean switch that maintains a bound boolean state that is "on" (true) or
 * "off" (false).
 * <P>
 * A Toggle object is not directly instantiated, but is created by either calling
 * <code>Toggle.{@link #define(String,boolean) define(String name, boolean v)}</code> or
 * <code>Toggle.{@link #set(String name, boolean value)}</code>.
 * <p>
 * A toggle also has a bound boolean "defined" property.
 * <P>
 * To make a defined toggle, <code>Foo</code>, with an initial state of <code>off</code>:
 * 
 * <Pre>
 * 
 * Toggle t = Toggle. {@link #define(String,boolean) define}("FOO",false);
 * 
 * </pre>
 * 
 * <P>
 * A toggle may be set to an initial value prior to being defined:
 * 
 * <Pre>
 * 
 * Toggle t = Toggle. {@link #set(String name, boolean value) set("FOO",true)};
 * 
 * </pre>
 * 
 * This will lookup a toggle, "FOO", creating it as necesssary, and set its state to <code>on</code>, regardless of
 * whether or not it is previously defined.
 * <P>
 * If a toggle state is set prior to being defined, then the state argument to the subsequent
 * {@link #define(String,boolean) define()}call will be ignored.
 * <P>
 * To query the state of a toggle, <code>t</code>, invoke <code>t.{@link #on() on()}</code> or
 * <code>t.{@link #off() off()}</code>.
 * <P>
 * To change the state of a toggle, <code>t</code>, invoke <code>t.{@link #set(boolean) set(v)}</code>.
 * <P>
 * A toggle may be restored to its previous state by invoking {@link #pop() pop()}.
 * <P>
 * Toggle names are case-insensitive. They are, by convention, stored in upper case.
 * <P>
 * Applications can listen for new toggles to be added by invoking
 * {@link #addNewToggleListener(NewToggleListener) addNewToggleListener}.
 */
public final class Toggle {

    /**
     * Toggle state property (on or off).
     */
    public static final String STATE = "state";

    /**
     * Toggle defined property (fired when toggle is defined).
     */
    public static final String DEFINED = "defined";

    /**
     * Lookup a toggle by name. If not found and "enterIt" is true, then create one with an "undefined" state and enter
     * it.
     * @param name name of the toggle.
     * @param enterIt if true, toggle will be created it not yet in table.
     * @return the toggle, or null if toggle isn't found and "enterIt" is false.
     */
    public static Toggle lookup (String name, boolean enterIt) {
        name = name.toUpperCase();
        Toggle t = sHash.get(name);
        if (t == null && enterIt) {
            t = new Toggle(name);
            fireNewToggleEvent(t);
            sHash.put(name, t);
        }
        return t;
    }

    /**
     * Add a listener for a new toggle.
     */
    public static void addNewToggleListener (NewToggleListener listener) {
        synchronized (sListeners) {
            sListeners.add(listener);
        }
    }

    /**
     * Remove a listener for a new toggle.
     */
    public static void removeNewToggleListener (NewToggleListener listener) {
        synchronized (sListeners) {
            sListeners.remove(listener);
        }
    }

    private static void fireNewToggleEvent (Toggle t) {
        NewToggleListener l[];
        synchronized (sListeners) {
            if (sListeners.size() == 0)
                return;
            l = sListeners.toArray(new NewToggleListener[sListeners.size()]);
        }
        for (int i = 0; i < l.length; i++)
            l[i].newToggle(t);
    }

    /**
     * Lookup up a toggle by name. If not defined, returns null.
     * @param name name of the toggle.
     * @return Toggle instance associated with name, or null.
     */
    public static Toggle lookup (String name) {
        return lookup(name, false);
    }

    /**
     * Return the (primary) name of the toggle.
     * @return the (primary) name of the toggle.
     */
    public String getName () {
        return mName;
    }

    /**
     * Return a description of this toggle, if it was defined with one. Otherwise, returns <code>null</code.>
     * @return brief description of this toggle, or null.
     */
    public String getDescription () {
        return mDescription;
    }

    /**
     * Associate a Toggle instance with a name, if one is not already associated. Set its state to "defined". Set its
     * value "value" unless it has already been set to something else (by a previous call to
     * <code>{@link #set(String name, boolean value)}</code>).
     * @param name name of toggle.
     * @param value the initial value to be assigned the toggle unless it was already set. (Toggles can be set prior to
     * being "defined".)
     * @param description a description to be used on, say, a checkbox label that is tied to this toggle.
     * @exception ToggleException toggle is already defined.
     */
    public static Toggle define (String name, boolean value, String description) {
        Toggle t = lookup(name, true);
        if (t.isDefined())
            throw new ToggleException("Toggle " + name + " defined more than once");
        t.mDescription = description;
        t.mDefined = true;
        t.mPropertySupport.firePropertyChange(DEFINED, false, true);
        if (!t.mIsSet)
            t.set(value);
        return t;
    }

    /**
     * Define a toggle with no description. See {@link #define(String,boolean,String)}.
     * @param name name of toggle.
     * @param value the initial value to be assigned the toggle unless it was already set. (Toggles can be set prior to
     * being "defined".)
     * @exception ToggleException toggle is already defined.
     */
    public static Toggle define (String name, boolean value) {
        return define(name, value, null);
    }

    /**
     * Define an "alias" for an existing toggle. The alias name will reference the toggle. If there is already a Toggle
     * instance associated with the alias name, then it must not be defined, nor must it have been set to a value other
     * than that of the original toggle.
     * @param t the Toggle instance that we're assigning a new name for.
     * @param name the alias that will refer to toggle t.
     * @exception ToggleException if "name" is already defined as a toggle. Or if "name" already associated with Toggle
     * instance that was preset.
     */
    public static void defineAlias (Toggle t, String name) {
        name = name.toUpperCase();
        // If the name was already referenced as a toggle,
        // complain if its value is different from that which
        // it is being aliased to.
        Toggle tog = sHash.get(name);
        if (tog != null && tog != t) {
            if (tog.isDefined())
                throw new ToggleException("Aliasing already defined toggle: " + name);
            if (tog.mIsSet) {
                if (t.mIsSet && t.on() != tog.on())
                    throw new ToggleException("Aliasing preset toggle " + name + " with " + t.getName());
                t.set(tog.on());
            }
        }
        sHash.put(name, t);
    }

    /**
     * Lookup a toggle and set it to a particular value without necessarily "defining" it.
     * @param name the name of the toggle.
     * @param value the value to set the toggle to.
     * @return the Toggle instance.
     */
    public static Toggle set (String name, boolean value) {
        Toggle t = lookup(name, true);
        t.set(value);
        return t;
    }

    /**
     * Lookup a toggle and "pop" is value. That is, restore its value to its previous state.
     * @param name the name of the toggle.
     * @return the Toggle instance.
     */
    public static Toggle pop (String name) {
        Toggle t = lookup(name, true);
        t.pop();
        return t;
    }

    /**
     * Add a property change listener.
     */
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        mPropertySupport.addPropertyChangeListener(listener);
    }

    /**
     * Add a proprety change listener for either {@link #STATE STATE}or {@link #DEFINED DEFINED}.
     */
    public void addPropertyChangeListener (String property, PropertyChangeListener listener) {
        mPropertySupport.addPropertyChangeListener(property, listener);
    }

    /**
     * remove a property change listener.
     */
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        mPropertySupport.removePropertyChangeListener(listener);
    }

    /**
     * remove a specific property change listener.
     */
    public void removePropertyChangeListener (String property, PropertyChangeListener listener) {
        mPropertySupport.removePropertyChangeListener(property, listener);
    }

    /**
     * Set this toggle to the given value. Toggles are assigned values in a stack-like fashion. Previous values can be
     * restored by calling {@link #pop() pop()}.
     * @param value the value being assigned to the toggle.
     * @see #pop()
     */
    public final void set (boolean value) {
        boolean oldValue = on();
        mIsSet = true;
        mValue <<= 1;
        mValue |= value ? 1 : 0;
        if (value != oldValue)
            mPropertySupport.firePropertyChange(STATE, oldValue, value);
    }

    /**
     * Return true if toggle's state is on.
     * @return value of toggle.
     */
    public final boolean on () {
        return (mValue & 1) != 0;
    }

    /**
     * Return true if toggle's state is off.
     * @return true if toggle is not set.
     */
    public final boolean off () {
        return !on();
    }

    /**
     * Restore previous value of toggle.
     */
    public final void pop () {
        boolean oldValue = on();
        mValue >>= 1;
        if (oldValue != on())
            mPropertySupport.firePropertyChange(STATE, oldValue, !oldValue);
    }

    /**
     * {@inheritDoc}Implementation of overridden (or abstract) method.
     * @return the String representation of the toggle, which is the Toggle's name.
     */
    @Override
    public String toString () {
        return mName;
    }

    /**
     * Returns true if toggle has been defined.
     * @return true if this toggle has been officially "defined"
     */
    public boolean isDefined () {
        return mDefined;
    }

    /**
     * Return the collection of all toggles that has been created.
     * @return the collection of all toggles.
     */
    public static Collection<Toggle> allToggles () {
        return sHash.values();
    }

    /**
     * Return all toggles as an array.
     * @return an array of all toggles.
     */
    public static Toggle[] getToggles () {
        Collection<Toggle> c = allToggles();
        return c.toArray(new Toggle[c.size()]);
    }

    /**
     * Private constructor
     */
    private Toggle(String name) {
        mDefined = false;
        mIsSet = false;
        mValue = 0;
        mName = name;
        mPropertySupport = new PropertyChangeSupport(this);
    }

    private static HashMap<String, Toggle> sHash = new HashMap<String, Toggle>();

    private boolean mDefined;

    private boolean mIsSet;

    private int mValue;

    private String mName;

    private String mDescription;

    private PropertyChangeSupport mPropertySupport;

    private static ArrayList<NewToggleListener> sListeners = new ArrayList<NewToggleListener>();
    /*
     * public static void main(String args[]){ Toggle t1 = Toggle.define("toggle_1",true); Toggle t2 =
     * Toggle.lookup("toggle_2"); t2.set(true); Toggle.defineAlias(t1,"toggle_3"); System.out.println("t1=" + t1.on());
     * System.out.println("t2=" + t2.on()); t2.pop(); System.out.println("t2 popped to" + t2.on());
     * System.out.println("t3=" + Toggle.lookup("toggle_3").on()); System.out.println("t3 is aliased to " +
     * Toggle.lookup("toggle_3").getName()); Toggle.set("toggle_3",false); System.out.println("t1 after setting to false =" +
     * t1.on()); }
     */
}
