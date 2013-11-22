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

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.arc.mw.util.IPropertyManager;
import com.arc.mw.util.Toggle;
import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImage;
import com.arc.widgets.IScrollPane;
import com.arc.widgets.WidgetsFactory;
import com.arc.xml.BuilderInstantiator;
import com.arc.xml.Element;
import com.arc.xml.XmlBasedObject;
import com.metaware.guihili.builder.CompositeAction;
import com.metaware.guihili.builder.EnableMonitor;
import com.metaware.guihili.builder.Environment;
import com.metaware.guihili.builder.FileResolver;
import com.metaware.guihili.builder.IIconWrapper;
import com.metaware.guihili.eval.Evaluator;
import com.metaware.guihili.eval.ILisp;
import com.metaware.guihili.eval.Lisp;
import com.metaware.guihili.parser.XMLReaderFactory;

/**
 * This class is used to construct a GUI component, or heirarchy of components
 * from a Guihili file.
 * <p>
 * GUI events are monitored by registering property listeners or action
 * listeners.
 * <P>
 * The guihili file is read by calling {@link #readXML(String) readXML}.
 * Afterwards, the components can be retrieved by name by calling
 * {@link #getComponent(String)}. Typically, the primary container panel is
 * called <code>"main"</code>.
 */
public class Gui extends XmlBasedObject implements IComponentMap, IActionMap, IPropertyManager, IExceptionHandler, IMessageDialog, VetoableChangeListener {
    
    public enum PropertyType {
        ANY,  // No restrictions
        INT,  // signed integer
        HEX,  // an integer preferably in hex
        UINT, // an unsigned integer
    }

    public static final String GUI_VERSION = "1021";
    
    /**
     * The name of the property that gets assigned the
     * list that is constructed from "arg_action" actions.
     */
    public static String ARG_ACTION = "ARG_ACTION";

    /**
     * The name of the action that causes the
     * "arg_action" list to be generated.
     */
    public static String GEN_ARG_ACTION = "GEN_ARG_ACTION";
    /**
     * A property that will be set to "true" when reading XML and set to "false"
     * when XML reading is complete. Builder classes can register listeners to
     * do things after all XML has been read.
     */
    public static final String READING_XML = "ReadingXML";

    private static final String[] sDefaultSuffices = new String[] { ".opt",
            ".xml"};

    private static Toggle sTrace = Toggle.define("TRACE_GUI", false);

    private static int sInstanceCount;

    private IContainer mRootParent;

    /**
     * Construct from a user-supplied environment, expression evaluator, and
     * file resolver. Any argument can be <code>null</code> will will cause an
     * appropriate default to be used.
     * 
     * @param env
     *            symbol environment that used by expression evaluator; serves
     *            as "global" environment; if <code>null</code>, a default
     *            will be used.
     * @param eval
     *            the expression evaluator; if <code>null</code> a default
     *            will be used.
     * @param resolver
     *            file resolver; if <code>null</code>, a default will be
     *            used.
     */
    public Gui(IEnvironment env, IEvaluator eval, IFileResolver resolver) {
        if (env != null)
            mEnvironment = env;
        else
            mEnvironment = Environment.create();
        if (mEnvironment.getSymbolValue("GUI_VERSION") == null){
            mEnvironment.putSymbolValue("GUI_VERSION",GUI_VERSION);
        }
        mGlobalEnvironment = mEnvironment;
        if (eval != null)
            mEvaluator = eval;
        else {
            ILisp lisp = Lisp.create();
            Lisp.addFunctions(lisp, new LispFunctions(this, this, this, this));
            mEvaluator = new Evaluator(lisp);
        }
        mResolver = resolver;
        if (resolver == null) {
            mResolver = new FileResolver(new String[] { "."}, sDefaultSuffices);
        }
        mInstance = ++sInstanceCount;
        _components = new HashMap<String,IComponent>();
        _properties = new HashMap<String,Object>();
        _actions = new HashMap<String,ActionListener>();
        //	_buttonGroups = new HashMap();
        _propertyChangeSupport = new PropertyChangeSupport(this);
        _vetoableChangeSupport = new VetoableChangeSupport(this);
        _moreToCome = 0; // When 0, no more XML to be read after current one.
        mInitialized = false;

        try {
            setProperty(READING_XML, "false");
        } catch (PropertyVetoException x) {
        }
        init(Gui.class.getResource("guihili.xml"), new OurBuilderInstantiator(
                this), "com.metaware.guihili.builder");
    }

    /**
     * Construct with a default environment, expression evaluator, and file
     * resolver.
     */
    public Gui() {
        this((IEnvironment) null, (IEvaluator) null, (IFileResolver) null);
    }

    /**
     * Construct with a user-supplied lookup environment and file resolver, but
     * with a default expression evaulator.
     * 
     * @param env
     *            symbol environment that used by expression evaluator; serves
     *            as "global" environment; if <code>null</code>, a default
     *            will be used.
     * @param resolver
     *            file resolver; if <code>null</code>, a default will be
     *            used.
     */
    public Gui(IEnvironment env, IFileResolver resolver) {
        this(env, null, resolver);
    }

    /**
     * Construct with a user-supplied lookup environment and file resolver, but
     * with a default expression evaulator.
     * 
     * @param env
     *            symbol environment that used by expression evaluator; serves
     *            as "global" environment; if <code>null</code>, a default
     *            will be used.
     * @param dirs
     *            list of directories in which "include" directives will be
     *            resolved.
     */
    public Gui(IEnvironment env, String dirs[]) {
        this(env, new FileResolver(dirs, sDefaultSuffices));
    }

    /**
     * Construct with a user-supplied lookup environment and file resolver, and
     * a user-suppplied Lisp processor.
     * 
     * @param env
     *            symbol environment that used by expression evaluator; serves
     *            as "global" environment; if <code>null</code>, a default
     *            will be used.
     * @param lisp
     *            a lisp processor for evaluating expressions.
     * @param dirs
     *            list of directories in which "include" directives will be
     *            resolved.
     */
    public Gui(IEnvironment env, ILisp lisp, String dirs[]) {
        this(env, lisp != null ? new Evaluator(lisp) : null,
                dirs != null ? new FileResolver(dirs, sDefaultSuffices) : null);
    }

    /**
     * Construct with directory paths for resolving "includes".
     * 
     * @param dirs
     *            list of directories in which "include" directives will be
     *            resolved.
     */
    public Gui(String dirs[]) {
        this(null, dirs);
    }

    /**
     * Called from base class immediately before reading the Guihili file.
     */
    @Override
    protected void startReading() {
        mRootParent = mParentContainer;
        try {
            setProperty(READING_XML, "true");
        } catch (PropertyVetoException x) {
        }
    }

    /**
     * Read Guihili from URL. If "last" is false, then we expect to read another
     * URL afterwards. This prevents us from being too quick to diagnose
     * undefined actions.
     */
    public void readXML(URL url, boolean last) throws IOException,
            SAXException, SAXParseException {
        if (!last) {
            _moreToCome++;
            readXML(url);
            _moreToCome--;
        } else
            readXML(url);
    }

    private IContainer mParentContainer;

    /**
     * Set the container that the generated widget will be placed into. Also set
     * the contraint that the generated widget will be associated within the
     * container.
     * 
     * @param container
     *            the container that the generated widget will be placed into.
     */
    public void setParent(IContainer container) {
        mParentContainer = container;
    }

    public IContainer getParent() {
        return mParentContainer;
    }

    public IComponentFactory getComponentFactory() {
        if (mComponentFactory == null) {
            mComponentFactory = WidgetsFactory.createSwing();
        }
        return mComponentFactory;
    }

    public void setComponentFactory(IComponentFactory f) {
        mComponentFactory = f;
    }

    public void dispose() {
        if (mComponentFactory != null) {
            mComponentFactory.dispose();
            mComponentFactory = null;
        }
    }

    /**
     * Given the current symbol-lookup environment and evaulator, evaluate the
     * string.
     * <P>
     * NOTE: this is called internally as a parsed Guihili document is
     * processed. It is made public so that sub-packages can access it. It is
     * not intended to be called otherwise.
     * 
     * @param s
     *            the string to evaluate.
     * @return the evaluated string.
     */
    public String expandString(String s) {
        try {
            return (String) getEvaluator().evaluate(s, getEnvironment());
        } catch (Exception x) {
            handleException("While evaluating: " + s, x);
            return null;
        }
    }

    /**
     * Return the object that manages the enabling and disabling of the given
     * component if there is one. Otherwise, return null.
     * <P>
     * <b>NOTE </b>: this is called internally as a parsed Guihili document is
     * processed. It is made public so that sub-packages can access it. It is
     * not intended to be called otherwise.
     * 
     * @param c
     *            the component whose "enabled" property is being controlled.
     */
    public EnableMonitor getEnableMonitor(IComponent c) {
        if (mEnableMonitorMap == null) return null;
        return  mEnableMonitorMap.get(c);
    }

    /**
     * Return the object that manages the enabling and disabling of the given
     * component if there is one. Make one if there isn't one already
     * <P>
     * <b>NOTE </b>: this is called internally as a parsed Guihili document is
     * processed. It is made public so that sub-packages can access it. It is
     * not intended to be called otherwise.
     * 
     * @param c
     *            the component whose "enabled" property is being controlled.
     */
    public EnableMonitor makeEnableMonitor(IComponent c) {
        if (mEnableMonitorMap == null) mEnableMonitorMap = new HashMap<IComponent,EnableMonitor>(50);
        EnableMonitor em =  mEnableMonitorMap.get(c);
        if (em == null) {
            em = new EnableMonitor(c, this);
            mEnableMonitorMap.put(c, em);
        }
        return em;
    }

    /**
     * Given the current environment, evaluate a string or expression.
     * 
     * @param a
     *            a string or expression (list) to evaluate
     * @return the evaluated object.
     */
    public Object evaluate(Object a) {
        try {
            return getEvaluator().evaluate(a, getEnvironment());
        } catch (Exception x) {
            handleException("While evaluating: " + a, x);
            return null;
        }
    }

    /**
     * Read Guihili or XML from a file with a given name. If the name ends in
     * ".opt", we assume it to be Guihili, otherwise XML.
     * 
     * @param filename
     *            the name of the Guihili or XML file to be read.
     */
    @Override
    public void readXML(String filename) throws IOException, SAXException,
            SAXParseException {
        InputSource input = mResolver.openFile(filename);
        if (input != null)
            readXML(input);
        else
            throw new FileNotFoundException("Can't open \"" + filename + "\"");
    }

    /**
     * Read Guihili or XML from an input source. If the name ends in ".opt", we
     * assume it to be Guihili, otherwise XML.
     * 
     * @param input
     *            the source of the Guihili or XML file to be read.
     */
    @Override
    public void readXML(InputSource input) throws SAXException,
            SAXParseException {
        // If guihili file, we insert a Guihili parser that conforms to the
        // XMLReader interface.
        if (input.getSystemId().endsWith(".opt")) {
            setXMLReader(XMLReaderFactory.makeReader(mResolver, mEvaluator,
                    mEnvironment));
        }
        super.readXML(input);
    }

    /**
     * Called from base class when reading of Guihili file is completed.
     */
    @Override
    protected void endReading() {
        if (_moreToCome == 0) {
            mInitialized = true;
            try {
                setProperty(READING_XML, "false");
            } catch (PropertyVetoException x) {
            }
            // Invoke delayed function...
            if (_toBeInvokedEarly != null) {
                invokeRunList(_toBeInvokedEarly);
                _toBeInvokedEarly = null; // free up
            }
            if (_toBeInvokedLate != null) {
                invokeRunList(_toBeInvokedLate);
                _toBeInvokedLate = null; // free up; there's entry for every
                // component!
            }
            // A bug (feature?) of SWT renders empty
            // containers with a fixed length and height.
            // Go through and eliminate empty containers.
            if (mRootParent != null) {
                removeEmptyContainers(mRootParent);
            }
        }
    }

        /** To get around a SWT bug (feature?), get rid of
         * all empty containers that are an artifact of
         * old Guihili.
         * @param c
         */
    public void removeEmptyContainers(IContainer c) {
        IComponent kids[] = c.getChildren();
        if (kids.length > 0) {
            for (int i = 0; i < kids.length; i++) {
                //cr98607: allow empty scroll pane; we have one in a CardLayout
                if (kids[i] instanceof IContainer && !(kids[i] instanceof IScrollPane)) {
                    removeEmptyContainers((IContainer) kids[i]);
                }
            }
            kids = c.getChildren();
        }
        if (kids.length == 0) c.dispose();
    }

    private void invokeRunList(List<Runnable> runList) {
        int cnt = runList.size();
        for (int i = 0; i < cnt; i++) {
            runList.get(i).run();
        }
        runList.clear();
    }

    /**
     * Returns true if we are fully initialized. If not, we're still reading
     * XML.
     * 
     * @return false if we're still reading Guihili/XML file.
     */
    public boolean isInitialized() {
        return mInitialized;
    }

    /**
     * Add a Swing action that the GUI may fire.
     * 
     * @param action
     *            the action to be added. Its <code>NAME</code> attribute is
     *            the name of the action.
     * @return a composite action that will invoke all registered action
     *         listeners with the same name.
     */
    public Action addAction(Action action) {
        return addAction((String) action.getValue(Action.NAME), action);
    }

    /**
     * Add an action listener that the GUI may fire by name. Because several
     * actions can have the same name, we make a composite.
     * 
     * @param name
     *            the name of the action.
     * @param action
     *            the action listener to be registered to that name.
     * @return a composite action that will invoke all registered action
     *         listeners with the same name.
     */
    @Override
    public Action addAction(String name, ActionListener action) {
        Action result = null;
        Object a = _actions.get(name);
        /*
         * Multiple actions by same name!
         */
        if (a == null && action instanceof CompositeAction) {
            _actions.put(name, action);
            result = (Action) action;
        } else if (a instanceof CompositeAction) {
            CompositeAction ca = (CompositeAction) a;
            if (action instanceof Action) {
                copyProperties((Action) action, ca);
            }
            ca.add(action);
        } else {
            CompositeAction ca = new CompositeAction(name);
            result = ca;
            if (a instanceof Action)
                copyProperties((Action) a, ca);
            else if (action instanceof Action)
                    copyProperties((Action) action, ca);
            _actions.put(name, ca);
            if (a != null) ca.add((ActionListener) a);
            ca.add(action);
        }
        return result;
    }

    static private void copyActionAttr(Action from, Action to, String attribute) {
        Object o = from.getValue(attribute);
        if (o != null) to.putValue(attribute, o);
    }

    static private void copyProperties(Action from, Action to) {
        copyActionAttr(from, to, Action.NAME);
        copyActionAttr(from, to, Action.SHORT_DESCRIPTION);
        copyActionAttr(from, to, Action.LONG_DESCRIPTION);
        copyActionAttr(from, to, Action.SMALL_ICON);
    }

    /**
     * Remove a previously-registered action listener.
     * 
     * @param name
     *            the name by which the action listener was registered.
     * @param action
     *            the action listener itself to be removed.
     */
    @Override
    public void removeAction(String name, ActionListener action) {
        Object a = _actions.get(name);
        if (a != null) if (a == action)
            _actions.remove(name);
        else if (a instanceof CompositeAction) {
            CompositeAction ca = (CompositeAction) a;
            ca.remove(action);
        }
    }

    /**
     * Return the action that corresponds to name. Returns null if not found.
     * 
     * @param name
     *            name of the composite action listener to be retrieved.
     * @return the associated composite action listener, or <code>null</code>.
     */
    @Override
    public Action getAction(String name) {
        return (Action) _actions.get(name);
    }

    /**
     * Add a listener for when <i>any </i> property changes. We provide this to
     * handle the "enable_if" attribute that must be reevaluated everytime
     * something changes.
     * <P>
     * 
     * @param listener
     *            the listener to be invoked when any property changes.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        _propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Set an exception handler to be invoked when any exception occurs during
     * Guihili processing, or during component accessing.
     * 
     * @param handler
     *            the exception handler
     */
    public void setExceptionHandler(IExceptionHandler handler) {
        mExceptionHandler = handler;
    }

    /**
     * Handle an exception. <b>Note: </b> this is public so that it can be
     * accessed by sub-packages. It is not intended to be called elsewhere.
     * 
     * @param x
     *            the exception to be handled.
     */

    @Override
    public void handleException(Throwable x) {
        handleException(null, x);
    }

    /**
     * Handle an exception. <b>Note: </b> this is public so that it can be
     * accessed by sub-packages. It is not intended to be called elsewhere.
     * 
     * @param msg
     *            a message to be displayed if not null.
     * @param x
     *            the exception to be handled.
     */
    @Override
    public void handleException(String msg, Throwable x) {
        if (mExceptionHandler != null) {
            if (msg != null)
                mExceptionHandler.handleException(msg, x);
            else
                mExceptionHandler.handleException(x);
        }
        else {
            if (msg != null) System.err.println(msg);
            x.printStackTrace(System.err);
        }
    }

    /**
     * Remove generic property change listener.
     * 
     * @param listener
     *            property change listener to be removed.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        _propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Add a property change listener bound to a particular property. When the
     * property value is changed, the listener will be invoked.
     * 
     * @param property
     *            name of the property to listen for.
     * @param listener
     *            to be invoked when the property value changes.
     */
    @Override
    public void addPropertyChangeListener(String property,
            PropertyChangeListener listener) {
        _propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    /**
     * Remove a property change listener.
     * 
     * @param property
     *            the name of the property.
     * @param listener
     *            the listener to be removed.
     */
    @Override
    public void removePropertyChangeListener(String property,
            PropertyChangeListener listener) {
        _propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    /**
     * Add a vetoable change listener for a constrained property.
     * 
     * @param property
     *            name of the property to listen for.
     * @param listener
     *            to be invoked when the property value changes.
     */
    public void addVetoableChangeListener(String property,
            VetoableChangeListener listener) {
        _vetoableChangeSupport.addVetoableChangeListener(property, listener);
    }

    /**
     * Remove a vetoable change listener.
     * 
     * @param property
     *            the name of the property.
     * @param listener
     *            the listener to be removed.
     */
    public void removeVetoableChangeListener(String property,
            VetoableChangeListener listener) {
        _vetoableChangeSupport.removeVetoableChangeListener(property, listener);
    }

    //    /**
    //     * Any XML border description that has a "name" attribute is
    //     * entered into a hash table.
    //     * Return the border with the given name.
    //     * Returns null if not found.
    //     * <P>
    //     * <b>NOTE:</b> this is made public so that it can be accessed
    //     * from sub-packages. It shouldn't be accessed otherwise.
    //     * @param name the name of a border.
    //     */
    //    public Border getBorder(String name){
    //	Border b = (Border)_borders.get(name);
    //	// Check for predefined...
    //	if (b == null){
    //	    if (name.equals("etched")){
    //		b = new EtchedBorder();
    //		_borders.put(name,b);
    //		}
    //	    else
    //	    if (name.equals("bevel") || name.equals("beveled") ||
    //		name.equals("bevelled"))
    //		{
    //		b = new BevelBorder(BevelBorder.LOWERED);
    //		_borders.put(name,b);
    //		}
    //	    }
    //	return b;
    //	}

    //    /**
    //     * Register a border by name.
    //     * <b>NOTE:</b> this is made public so that it can be accessed
    //     * from sub-packages. It shouldn't be accessed otherwise.
    //     * @param name the name of a border.
    //     * @param border the border to be associated with the name.
    //     */
    //    public void setBorder(String name, Border border){
    //	_borders.put(name,border);
    //	}
    //

    /**
     * Any guihili component description that has a "name" attribute is entered
     * into a hash table. Return the component with the given name. Returns null
     * if not found.
     * 
     * @param name
     *            name of the component.
     * @return the component with the given name, or <code>null</code>.
     */
    @Override
    public IComponent getComponent(String name) {
        return  _components.get(name);
    }

    /**
     * Return an array of all named components.
     * 
     * @return an array of all named components generated from guihili.
     */
    public IComponent[] getComponents() {
        Collection<IComponent> c = _components.values();
        return c.toArray(new IComponent[c.size()]);
    }

    /**
     * Record a "gui_proc". It is a component description that works like a
     * macro. It is instantiated at a "call" site.
     * <P>
     * <b>Note: </b> this method was made public so as to be accessible from
     * sub-packages. It should not be accessed elsewhere.
     * 
     * @param name
     *            the name of the gui proc.
     * @param body
     *            the body that contains the definition of a component.
     */
    public void addGuiProc(String name, Element body) {
        if (mGuiProcs == null) mGuiProcs = new HashMap<String,Element>();
        mGuiProcs.put(name, body);
    }

    /**
     * return a gui_proc body named "name". If not found, returns null.
     * <P>
     * <b>Note: </b> this method was made public so as to be accessible from
     * sub-packages. It should not be accessed elsewhere.
     * 
     * @param name
     *            the name of the gui_proc that we're looking up.
     * @return the corresponding body or null.
     */
    public Element getGuiProc(String name) {
        if (mGuiProcs != null) return mGuiProcs.get(name);
        return null;
    }

    /**
     * Add a named component to the GUI. Typically, this method is called by the
     * Guihili processor, but may be called by the client to "predefine" a
     * component that is referenced by name from within Guihili file.
     * 
     * @param name
     *            name of the component.
     * @param c
     *            the component to be associated with that name.
     */
    public void setComponent(String name, IComponent c) {
        if (sTrace.on())
                trace("SET COMPONENT " + name + " " + c.getClass().getName());
        _components.put(name, c);
    }

    /**
     * Compare values and take into account them being arrays.
     * 
     * @param oldValue
     * @param newValue
     * @return true if match.
     */
    static boolean compareEqual(Object oldValue, Object newValue) {
        if (oldValue == null)
            return newValue == null;
        else if (oldValue.equals(newValue)) return true;
        if (newValue == null) return false;
        if (oldValue instanceof Object[] && newValue instanceof Object[]) {
            Object[] o1 = (Object[]) oldValue;
            Object[] o2 = (Object[]) newValue;
            if (o1.length != o2.length) return false;
            for (int i = 0; i < o1.length; i++) {
                if (!compareEqual(o1[i], o2[i])) return false;
            }
            return true;
        }
        Object v1 = oldValue;
        Object v2 = newValue;
        if (v2 instanceof Boolean || v2 instanceof Integer){
            Object tmp = v1;
            v1 = v2;
            v2 = tmp;
        }
        if (v1 instanceof Boolean){
            Boolean b = (Boolean)v1;
            if (v2.equals("true") || v2.equals("1"))
                return b.booleanValue();
            if (v2.equals("false") || v2.equals("0"))
                return !b.booleanValue();
            if (v2 instanceof Integer){
                int i = ((Integer)newValue).intValue();
                if (i == 1) return b.booleanValue();
                if (i == 0) return !b.booleanValue();
            }
        }
        if (v1 instanceof Integer){
            int i = ((Integer)v1).intValue();
            if (v2 instanceof String){
                try {
                    if (Integer.parseInt((String)v2) == i)
                        return true;
                }
                catch (NumberFormatException e) {
                   // ignore
                }
            }
            else if (v2 instanceof Boolean && (i == 0 || i==1)){
                boolean b = ((Boolean)v2).booleanValue();
                if (i==0) return !b;
                return b;           
            }
        }
        return false;
    }

    private int recurseCount = 0;
    protected void firePropertyChange(String property, Object oldValue,
            Object newValue) {
        if (!compareEqual(oldValue, newValue)) {
            if (recurseCount > 50){
                throw new IllegalStateException("Property change is looping: prop=" + 
                        property + ", prev value=" + oldValue + ", new value=" + newValue);
            }
            try {
                recurseCount++;
                _propertyChangeSupport.firePropertyChange(property, oldValue,
                        newValue);
            }
            finally{
                recurseCount--;
            }
        }
    }

    protected void fireVetoableChange(String property, Object oldValue,
            Object newValue) throws PropertyVetoException {
        _vetoableChangeSupport.fireVetoableChange(property, oldValue, newValue);
    }

    /**
     * Set a property.
     * If the property is bound, a PropertyChangeEvent will be fired.
     * If the property is contrained, a VetoableChangeEvent will be fired.
     * Any GUI component that is somehow dependent on the value of the
     * property will be alerted.
     * @param property name of the property.
     * @param newValue new value to be assigned to the property.
     */
    @Override
    public void setProperty(String property, Object newValue)
            throws PropertyVetoException {
        if (sTrace.on())
            trace("PROPERTY " + property + "=" + newValue);
        Object oldValue = getProperty(property);
        // Becuase of massive number of property change listeners that transitively cause
        // other property changes to be affected, filter out non-changes.
        if (newValue != null){
            if (newValue.equals(oldValue)) return;
        }
        else if (oldValue == null) return;
        if (_vetoableChangeSupport.hasListeners(property))
            fireVetoableChange(property, oldValue, newValue);
        if (newValue != null)
            _properties.put(property, newValue);
        else
            _properties.remove(property);
        if (_propertyChangeSupport.hasListeners(property))
            firePropertyChange(property, oldValue, newValue);
        // Don't support the modes since a change event can occur on each key stroke
//        if (getPropertyType(property) == PropertyType.HEX &&
//                (!(newValue instanceof String) || !newValue.toString().startsWith("0x")) &&
//                _properties.get(property) == newValue){ // may have changed by listenener
//            long value = toInteger(newValue);
//            setProperty(property,"0x" + Long.toHexString(value));
//        }
    }
    
    public static long toInteger(Object v) throws NumberFormatException {
        if (v instanceof Number){
            return ((Number)v).longValue();
        }
        if (v instanceof String){
            String s = v.toString();
            if (s.startsWith("0x") || s.startsWith("0X")){
                return Long.parseLong(s.substring(2),16);
            }
            return Long.parseLong(s);
        }
        throw new NumberFormatException("Not valid integer: " + v);
    }
    
    public void setPropertyType(String property, PropertyType type){
        if (type == PropertyType.ANY){
            _propertyType.remove(property);
            this.removeVetoableChangeListener(property, this);
        }
        else {
            this.addVetoableChangeListener(property,this);
            _propertyType.put(property,type);
        }
    }
    
    /**
     * Called when changing the value of a property that has a "type".
     * Throws an exception if the value doesn't correspond to the type.
     */
    @Override
    public void vetoableChange(PropertyChangeEvent evt)
            throws PropertyVetoException {
//        String name = evt.getPropertyName();
//        boolean mustBeUnsigned = false;
//        switch (getPropertyType(name)) {
//        case ANY:
//            break;
//        case UINT:
//            mustBeUnsigned = true;
//            /*FALLTHRU*/
//        case HEX:
//        case INT: {
//            try {
//                long v = toInteger(evt.getNewValue());
//                if (mustBeUnsigned && v < 0) {
//                    throw new PropertyVetoException("Must not be negative", evt);
//                }
//            } catch (NumberFormatException x) {
//                throw new PropertyVetoException("Not a valid integer", evt);
//            }
//        }
//        }
    }
    
    public PropertyType getPropertyType(String name){
        PropertyType type = _propertyType.get(name);
        if (type == null) type = PropertyType.ANY;
        return type;
    }

    /**
     * Indicate that a particular property name is not to be stored
     * persistently.
     * @param propertyName name of property to make non-persistent.
     */
    public void setNonpersistent(String propertyName){
        _nonpersistentProperties.add(propertyName);
    }

    /**
     * Return the set of defined property names.
     * @return the set of all defined properties.
     */
    @Override
    public Collection<String> getPropertyNames() {
        Collection<String> c = _properties.keySet();
        if (_nonpersistentProperties.size() > 0){
            ArrayList<String> newC = new ArrayList<String>(c);
            newC.removeAll(_nonpersistentProperties);
            c = newC;
        }
        return c;
    }

    /**
     * Return the set of defined action names.
     * 
     * @return the set of defined action names.
     */
    @Override
    public Collection<String> getActionNames() {
        return _actions.keySet();
    }

    /**
     * Set a symbol-lookup environment for expression evaluation. This is called
     * as Guihili is processed to handle nested scopes.
     * 
     * @param env
     *            the new symbol-lookup environment.
     */
    public void setEnvironment(IEnvironment env) {
        mEnvironment = env;
    }

    /**
     * Return the current symbol-lookup environment.
     * 
     * @return the current symbol-lookup environment.
     */
    public IEnvironment getEnvironment() {
        return mEnvironment;
    }

    /**
     * Return the environment that is in the "global" scope. Is the original
     * environment that was passed to the constructor, or created within the
     * constructor.
     * 
     * @return the global envirinment
     */
    public IEnvironment getGlobalEnvironment() {
        return mGlobalEnvironment;
    }

    /**
     * Return the expression evaulator. This is made public so that sub-packages
     * can access it.
     * 
     * @return the expression evaluator.
     */
    public IEvaluator getEvaluator() {
        return mEvaluator;
    }

    /**
     * Add new functions to Lisp interpreter. The object is assumed to contains
     * "do_XXX" methods for each Lisp function, "XXX" that has signature:
     * 
     * <pre>
     * 
     *  
     *     Object do_XXX(List list)
     *   
     *  
     * </pre>
     * 
     * @param o
     *            object with "do_XXX" functions.
     */
    public void addLispFunctions(Object o) {
        if (mEvaluator instanceof Evaluator) {
            Lisp.addFunctions(((Evaluator) mEvaluator).getLisp(), o);
        } else
            throw new IllegalArgumentException(
                    "Evaluator doesn't use Lisp expressions!");
    }

    /**
     * Return the value of a property or <code>null</code> if the property
     * isn't defined.
     * 
     * @param property
     *            the name of the property.
     * @return the value associated with the property.
     */
    @Override
    public Object getProperty(String property) {
        return _properties.get(property);
    }
    
    public List<Object> getListFromProperty(String property) {
        Object v = getProperty(property);
        return coerceToList(v);
    }

    /**
     * Coerce a value to a list.
     * @param v a value that may already be a list or a string representation of a list.
     * @return the corresponding list.
     */
    @SuppressWarnings("unchecked")
    public List<Object> coerceToList(Object v) {
        if (v == null) return new ArrayList<Object>(0);
        if (v instanceof String) {
            try {
                String s = v.toString().trim();
                if (s.startsWith("{") && s.endsWith("}")) {
                    //canonicalize the { a y z } --> $( a y z )
                    s = "$(" + s.substring(1,s.length()-1) + ")";
                }
                v = mEvaluator.parseList(s);
            } catch (MalformedExpressionException e) {
                 handleException(e);
                 return new ArrayList<Object>(0);
            }
        }
        if (v instanceof List) return (List<Object>) v;
        
        List<Object> result = new ArrayList<Object>(1);
        result.add(v);
        return result;
    }

    /**
     * Arrange for the text object to be updated whenever property changes.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * 
     * @param property
     *            the name of a property to be updated by a text field change.
     * @param text
     *            a callback that keeps a property value and a text field in
     *            sync.
     */
    public void processProperty(String property, ITextWrapper text) {
        Object value = getProperty(property);
        if (value != null) text.setText(value.toString());
        /*
         * Arrange to change the text whenever the property value changes.
         */
        addPropertyChangeListener(property, new TextPropertyChange(this, text));
    }

    /**
     * Given a string that is either text or a property reference (prefixed by
     * "*"), arrange to set the text widget appropriately.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * 
     * @param string
     *            a string value or a property name prefixed by "*".
     * @param text
     *            a callback that keeps a property value and a text field in
     *            sync.
     */
    public void processTextOrProperty(String string, ITextWrapper text) {
        if (string.length() > 1 && string.charAt(0) == '*') {
            String property = string.substring(1);
            processProperty(property, text);
        } else
            text.setText(string);
    }

    private HashMap<String,IImage> mImageMap = new HashMap<String,IImage>();

    /**
     * Extract an icon from a name. If it appears to be a resource name (e.g.
     * "foo.gif") then load it as such. If it begins with "*", then it is a
     * property name. The value of the property may be an Icon or a String that
     * is a resource name.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * 
     * @param name
     *            name of the icon.
     */
    public IImage extractIcon(String name) {
        if (name == null) return null;
        if (name.length() == 0) return null;
        if (name.charAt(0) == '*') {
            String property = name.substring(1);
            Object value = getProperty(property);
            if (value instanceof IImage) return (IImage) value;
            if (value instanceof String)
                name = (String) value;
            else
                return null;
        }
        IImage image = mImageMap.get(name);
        if (image != null) { return image; }
        URL url = null;
        if (new File(name).exists()) {
            try {
                url = new File(name).toURI().toURL();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            url = getClass().getResource(name);
            if (url == null)
                    throw new IllegalArgumentException("Can't locate icon "
                            + name);
        }
        image = getComponentFactory().makeImage(url);
        mImageMap.put(name, image);
        return image;
    }

    /**
     * Set an icon map from which we can replace button labels with icons.
     * {@link #getAssociatedIcon(String)}uses this.
     * <P>
     * This is provided so that clients can map button labels to icons.
     * 
     * @param map
     *            the icon map that is keyed off of a button label.
     */
    public void setIconMap(IIconMap map) {
        mIconMap = map;
    }

    /**
     * Given a button label, return any associated icon that we desire to use
     * instead. The icon map is used that is set by
     * {@link #setIconMap(IIconMap)}.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * 
     * @param label
     *            the button label that is to serve as a key for the icon map.
     */
    public IImage getAssociatedIcon(String label) {
        if (mIconMap == null) return null;
        return mIconMap.getIcon(label);
    }

    /**
     * Set a callback for making a dialog from guihili. It is called when a
     * <code>dialog</code> or <code>window</code> node is encountered.
     * <P>
     * param maker a callback for getting a sub-dialog frame created.
     */
    public void setInternalFrameMaker(IInternalFrameMaker maker) {
        mInternalFrameMaker = maker;
    }

    /**
     * Get callback for making a dialog. <b>Note: </b> this is not attended to
     * be called by clients. It is public so that sub-packages can access it.
     * 
     * @return the callback for making a sub-dialog, or <code>null</code>.
     */
    public IInternalFrameMaker getInternalFrameMaker() {
        return mInternalFrameMaker;
    }

    /**
     * Register a callback interface for setting up a help link to a component.
     * 
     * @param helpHandler
     */
    public void setHelpHandler(IHelpHandler helpHandler) {
        mHelpHandler = helpHandler;
    }

    public IHelpHandler getHelpHandler() {
        return mHelpHandler;
    }

    /**
     * An icon that is based on a property name is subject to change as the
     * property value changes. This method make it happen.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * 
     * @param name
     *            an icon name or a property name prefixed with "*".
     * @param wrapper
     *            a callback to keep an icon in sync with the property value.
     */
    public void processIcon(String name, final IIconWrapper wrapper) {
        if (name == null) return;
        if (name.length() == 0) return;
        IImage icon = extractIcon(name);
        wrapper.setIcon(icon);
        if (name.charAt(0) == '*') {
            String property = name.substring(1);
            addPropertyChangeListener(property, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    Object value = event.getNewValue();
                    if (value instanceof IImage)
                        wrapper.setIcon((IImage) value);
                    else if (value instanceof String) {
                        wrapper.setIcon(extractIcon((String) value));
                    }
                }
            });
        }
    }

    /**
     * To accomodate the old "enable_trigger" button, we register a trigger
     * button that, when not selected, causes the target component to be
     * disabled. If the trigger is set, then then other means are used to
     * determine if its enabled.
     * <P>
     * We can have more than one trigger per component, therefore we maintain a
     * map of lists. The component is enable if <i>any </i> of the triggers is
     * selected. I.e., its a "or" relationship.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * <P>
     * 
     * @param target
     *            the target component that is to be enabled based on the
     *            selection of a trigger button.
     * @param trigger
     *            the button that enables the target component.
     */
    public void registerTrigger(IComponent target, IButton trigger) {
        if (_triggerMap == null) _triggerMap = new HashMap<IComponent,List<IButton>>();
        List<IButton> list =  _triggerMap.get(target);
        if (list == null) {
            list = new ArrayList<IButton>(2);
            _triggerMap.put(target, list);
        }
        list.add(trigger);
    }

    /**
     * Return the map that maps a component to the list if trigger buttons that
     * enable it.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * <P>
     * 
     * @return the map that maps a component to the list if trigger buttons that
     *         enable it.
     */

    public Map<IComponent,List <IButton>>getTriggerMap() {
        return _triggerMap;
    }

    /**
     * Given a component, return a list of checkbox buttons, if any that
     * controls its enablement (for support of old guihili). Returns null if
     * there is none.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * <P>
     * 
     * @param c
     *            the component that is possibly enabled by trigger buttons.
     * @return the list of trigger buttons that enable this component or
     *         <code>null</code>.
     */
    public List<IButton> getEnableTriggers(IComponent c) {
        if (_triggerMap == null) return null;
        return  _triggerMap.get(c);
    }

    /**
     * Emit trace message.
     * <P>
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * <P>
     */
    public void trace(String msg) {
        if (sTrace.on()) {
            System.out.println("[GUI:" + mInstance + "] " + msg);
        }
    }

    /**
     * <b>Note: </b> this is not attended to be called by clients. It is public
     * so that sub-packages can access it.
     * 
     * @param run
     *            a function to invoke after XML is read in.
     * @param late
     *            if true, we run it after others.
     */
    public void invokeAfterReading(Runnable run, boolean late) {
        if (late) {
            if (_toBeInvokedLate == null) _toBeInvokedLate = new ArrayList<Runnable>();
            _toBeInvokedLate.add(run);
        } else {
            if (_toBeInvokedEarly == null) _toBeInvokedEarly = new ArrayList<Runnable>();
            _toBeInvokedEarly.add(run);
        }
    }

    /**
     * Set the main frame that the component are associated with.
     * 
     * @param frame
     */
    public void setFrame(Object frame) {
        mFrame = frame;
    }

    public Object getFrame() {
        return mFrame;
    }

    private Object mFrame; // JFrame or SWT Shell


    @Override
    public void showErrorDialog(String message) {
        mComponentFactory.showErrorDialog(mFrame, message);

    }
    
    @Override
    public void showMessageDialog(String message) {
        mComponentFactory.showMessageDialog(mFrame, message);

    }
    
    public Map<String,Object> getPropertyMap(){
        return new Map<String,Object>(){

            @Override
            public int size() {
                return _properties.size();
            }

            @Override
            public void clear() {
                _properties.clear();
                
            }

            @Override
            public boolean isEmpty() {
                return _properties.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return _properties.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return _properties.containsValue(value);
            }

            @Override
            public Collection<Object> values() {
                return _properties.values();
            }

            @Override
            public void putAll(Map<? extends String,? extends Object> map) {
                for (Map.Entry<? extends String,? extends Object> e: map.entrySet()){
                    try {
                        // So as to fire property change event.
                        setProperty(e.getKey(),e.getValue());
                    } catch (PropertyVetoException e1) {
                    }
                }
                
            }

            @Override
            public Set<Entry<String,Object>> entrySet() {
                return _properties.entrySet();
            }

            @Override
            public Set<String> keySet() {
                return _properties.keySet();
            }

            @Override
            public Object get(Object key) {
                return _properties.get(key);
            }

            @Override
            public Object remove(Object key) {
                return _properties.remove(key);
            }

            @Override
            public Object put(String key, Object value) {
                Object result = _properties.get(key);
                try {
                    //fires property-change event
                    setProperty(key,value);
                } catch (PropertyVetoException e) {
                }
                return result;
            }};
    }

    /***************************************************************************
     * public JavaScript getJavaScript(){ if (_js == null){ _js = new
     * JavaScript(); _js.addVar("gui", new IMap(){ public Object get(Object
     * key){ return getProperty(key.toString()); } public void put(Object key,
     * Object value){ try{ setProperty(key.toString(),value); }
     * catch(PropertyVetoException x){} } }); } return _js; }
     **************************************************************************/

    private Map<String,ActionListener>_actions;

    //   private Map/*<String,Border>*/ _borders;
    private Map<String,IComponent> _components;

    private Map<String,Object>_properties;
    
    private Map<String,PropertyType> _propertyType = new HashMap<String,PropertyType>();
    
    private Set<String> _nonpersistentProperties = new HashSet<String>();

    //    private Map/*<String,ButtonGroup>*/ _buttonGroups;
    private Map<String,Element>mGuiProcs;

    private PropertyChangeSupport _propertyChangeSupport;

    private VetoableChangeSupport _vetoableChangeSupport;

    private int _moreToCome; // When non-zero, we have more XML to read after

    // this one.

    private IEnvironment mEnvironment; // for expression evaluation.

    private IEnvironment mGlobalEnvironment; // top level environment

    private IEvaluator mEvaluator; // for expression evaluation.

    private IFileResolver mResolver;

    private IExceptionHandler mExceptionHandler;

    private IInternalFrameMaker mInternalFrameMaker;

    private IIconMap mIconMap;

    private int mInstance;

    private boolean mInitialized; //true when all XML is read.

    private Map<IComponent,List <IButton>>_triggerMap;

    private List<Runnable> _toBeInvokedLate;

    private List<Runnable> _toBeInvokedEarly;

    private Map <IComponent,EnableMonitor>mEnableMonitorMap;

    private IHelpHandler mHelpHandler;

    private IComponentFactory mComponentFactory;
    //    private JavaScript _js; // JavaScript processor

}

/**
 * our customized builder instantiator. The constructor of each builder class
 * takes the "Gui" object as an argument.
 */

class OurBuilderInstantiator extends BuilderInstantiator {

    OurBuilderInstantiator(Gui gui) {
        _args = new Object[] { gui};
    }

    @Override
    protected Object[] getArguments() {
        return _args;
    }

    private Object[] _args;
}

