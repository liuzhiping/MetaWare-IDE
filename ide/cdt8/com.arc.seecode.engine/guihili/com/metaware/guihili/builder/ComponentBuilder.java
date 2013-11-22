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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.arc.mw.util.Toggle;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IFont;
import com.arc.xml.Element;
import com.metaware.guihili.Coerce;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IHelpHandler;
import com.metaware.guihili.ITextWrapper;

/**
 * Base class for all builders that produce Components.
 * If accessed directly, then the "name" attribute must reference
 * a component that was programmatically set by calling {@link Gui#setComponent(String,IComponent)}.
 *<P>
 * This class would ordinarily be package-private, but is made public
 * because it is accessed by means of reflection and is subclassed by
 * sub-packages.
 * <P>
 * Likewise, all the "setter" methods are made public because so that
 * they can be accessed by reflection. Ordinarily, they would be package-private.
 * <P>
 * This class is accessed by reflection from the <code>guihili.xml</code> file.
 * <P>
 * @author David Pickens
 */
public class ComponentBuilder extends Builder {

    public static final String ARG_ACTION = Gui.ARG_ACTION;
    static Toggle sTrace = Toggle.define("TRACE_GUI_COMPONENT", false);
    static public String SET_OPTS = ":setOpts:";
    
    //private float _alignmentX;
    //private float _alignmentY;


    public ComponentBuilder(Gui gui) {
        super(gui);
    }

    /**
     * Prepare to start a new object to be built.
     * After this call, the "set<em>Property</em>(value)" 
     * method will be called to set each attribute that is flagged
     * as a "property"
     * Others are set by calling "setAttribute()"
     */
    @Override
    protected void startNewInstance(Element element) throws SAXException {
        String name = element.getAttribute("name");
        if (name.length() > 0) {
            mComponent = _gui.getComponent(name);
            if (mComponent == null)
                throw new SAXException(
                    element.getTagName()
                        + " tag references non-existent component "
                        + name);
        }
        else
            error(
                element,
                "Anonymous component "
                    + element.getTagName()
                    + " requires a name attribute.");
    }
    
    public void setVisible(Object v){
        mVisibleIf = v;
    }

    public void setExpandable(boolean v) {
        mExpandable = v;
        if (v)
            mHorizontalAlignment = IComponent.FILL;
    }

    protected boolean isExpandable() {
        return mExpandable;
    }
    public void setValign(String v) {
        if (v.equalsIgnoreCase("BEGINNING")
            || v.equalsIgnoreCase("TOP")
            || v.equalsIgnoreCase("NORTH")) {
            mVerticalAlignment = IComponent.BEGINNING;
        }
        else if (v.equalsIgnoreCase("CENTER")) {
            mVerticalAlignment = IComponent.CENTER;
        }
        else if (
            v.equalsIgnoreCase("END")
                || v.equalsIgnoreCase("BOTTOM")
                || v.equalsIgnoreCase("SOUTH")) {
            mVerticalAlignment = IComponent.END;
        }
        else if (v.equalsIgnoreCase("FILL"))
            mVerticalAlignment = IComponent.FILL;
        else
            throw new IllegalArgumentException(
                "Invalid vertical alignment: " + v);
    }

    public void setHalign(String v) {
        if (v.equalsIgnoreCase("BEGINNING")
            || v.equalsIgnoreCase("LEFT")
            || v.equalsIgnoreCase("WEST")) {
            mHorizontalAlignment = IComponent.BEGINNING;
        }
        else if (v.equalsIgnoreCase("CENTER")) {
            mHorizontalAlignment = IComponent.CENTER;
        }
        else if (
            v.equalsIgnoreCase("END")
                || v.equalsIgnoreCase("RIGHT")
                || v.equalsIgnoreCase("EAST")) {
            mHorizontalAlignment = IComponent.END;
        }
        else if (v.equalsIgnoreCase("FILL")) {
            mHorizontalAlignment = IComponent.FILL;
            this.setExpandable(true); // so that mx.opt looks good
        }
        else
            throw new IllegalArgumentException(
                "Invalid horizontal alignment: " + v);
    }
    
    /**
     * The "alignmentX" attribute
     */
    public void setAlignmentX(float f) {
        //_alignmentX = f;
    }

    public void setAlignmentY(float f) {
       // _alignmentY = f;
    }

    /**
     * Records current element so that exceptions in action functions can
     * diagnose the location
     */
    @Override
    protected void beginChildren(Element e) throws SAXException {
        mThisElement = e;
        if (mComponent != null && !mFinished)
            finishComponent(mComponent);
    }

    /**
     * Add an unnamed child object.
     * @param object the object constructed for the child.
     * @param child the XML element from which the object was constructed.
     */
    @Override
    protected void addChild(Object object, Element child) throws SAXException {
        error(child, "Can't define children for anonymous component");
    }

    public void setDoc(String doc) {
        mDoc = doc;
    }
    public void setDoc_title(String doc) {
       // mDocTitle = doc; // not yet used
    }

    /**
     * Return the object that was created.
     */
    @Override
    protected Object returnObject() throws SAXException {
        if (mDocTitle != null) {
            try {
                _gui.setProperty("docTitle", mDocTitle);
            }
            catch (PropertyVetoException e) {
                System.out.println(
                    "Unable to set online documentation" + e.getMessage());
            }
        }
        if (mComponent != null) {

            if (mDoc != null) {
                IHelpHandler helpHandler = _gui.getHelpHandler();
                if (helpHandler != null)
                    helpHandler.associateHelpMessage(mComponent, mDoc);
            }

            handleEnablement(mComponent);
        }

        return mComponent;
    }

    /**
     * 
     */
    protected void handleEnablement(IComponent component) {
        // We do this late instead of in
        // "finishComponent" so that it applies
        // to children also.
        if (_enabled != null)
            setEnablement(component, _enabled);
        if (mEnableIf != null || mVisibleIf != null) {
            setEnableIf(component, mEnableIf, mVisibleIf);
        }
    }

    /**
     * Apply decorations to component.
     * We factor this out because some subclasses, like DialogBuilder actually
     * want its content pane decorated.
     */
    protected void finishComponent(final IComponent component) {
        mFinished = true;
        /*
         * If border title specified, but no border, then define one
         */
        if (_borderTitle != null) {
            _gui.processTextOrProperty(_borderTitle, new ITextWrapper() {
                @Override
                public void setText(String text) {
                    component.setBorderTitle(text);
                }
                @Override
                public String getText() {
                    return component.getBorderTitle();
                }
            });
        }
        if (_tooltip != null) {
            _gui.processTextOrProperty(_tooltip, new ITextWrapper() {
                @Override
                public void setText(String text) {
                    component.setToolTipText(text);
                }
                @Override
                public String getText() {
                    return component.getToolTipText();
                }
            });
        }
        if (mVerticalAlignment >= 0)
            component.setVerticalAlignment(mVerticalAlignment);
        if (mHorizontalAlignment >= 0)
            component.setHorizontalAlignment(mHorizontalAlignment);
        if (mExpandable)
            component.setHorizontalWeight(1.0);
        if (_border != IComponent.NO_BORDER)
            component.setBorder(_border);
        if (_font != null)
            component.setFont(_font);
        if (_foreground != null)
            component.setForeground(_foreground);
        if (_background != null)
            component.setBackground(_background);
        if (_width != 0 || _height != 0) {
            int w = _width;
            int h = _height;
            if (w == 0) {
                w = component.computeSize(0, h).width;
            }
            if (h == 0) {
                h = component.computeSize(w, 0).height;
            }
            component.setPreferredSize(w, h);
        }

        if (_name != null) {
            setComponentName(component);
            _gui.setComponent(_name, component);
        }

        // Make sure component is built...
        if (mComponent != null) {
            mComponent.getComponent();
            // do "arg_action=..."
            prepareActionProcIfRequired(getActionValueWrapper(mComponent));
        }
    }

    /**
     * Set the name of the component. May overriddent by subclasses if the
     * active component is actually within the generated component.
     * @param component component to be named.
     */
    protected void setComponentName (final IComponent component) {
        component.setName(_name);
    }

    public void setName(String name) {
        _name = name;
    }
    public String getName() {
        return _name;
    }
    /**
     * "id" is what guihili uses
     */
    public void setId(String name) {
        _name = name;
    }

    protected void setComponent(IComponent c) {
        mComponent = c;
        // Must do this right away before instantiation or else SWT implementation doesn't work.
        if (_border != 0)
            mComponent.setBorder(_border);
        if (_borderTitle != null)
            mComponent.setBorderTitle(_borderTitle);
    }
    protected IComponent getComponent() {
        return mComponent;
    }

    /**
     * The "width" attribute.
     */
    public void setPreferredWidth(int width) {
        _width = width;
    }
    /**
     * The "height" attribute.
     */
    public void setPreferredHeight(int height) {
        _height = height;
    }

    /**
     * Process tooltip attribute
     */

    public void setTooltip(String tooltip) {
        _tooltip = tooltip;
    }
    public String getTooltip() {
        return _tooltip;
    }

    public void setEnabled(String value) {
        //
        // HACK: old Guihili has "enable_property_name" to mean "enabled".
        // But it also has "enabled=0" or "enabled=1" to presumably set an
        // initial value? Seems superfluous. We ignore the latter.
        if (!value.equals("0") && !value.equals("1"))
            _enabled = value;
    }

    public String getEnabled() {
        return _enabled;
    }

    /**
     * Arrange to reset enable property of component by evaluating
     * "enable_if" expression each time a property changes.
     */
    private void setEnableIf(final IComponent c, final Object enableIf, Object visibleIf) {
        // If any property changes, then re-evaluate the enable_if expression.
        EnableMonitor em = _gui.makeEnableMonitor(c);
        if (enableIf != null)
            em.setEnableIf(enableIf);
        if (visibleIf != null)
            em.setVisibleIf(visibleIf);
    }

    /**
     * Set the "enabled" attribute.
     * Its value is "true" "false" or a dynamic property that is
     * set to "true" or "false".
     * Actually, anything other than "", "false"(any case), or "0" is 
     * considered  true.
     *
     * New addition: allow multiple properties separated by whitespace. They
     * must all be true for the component to be enabled.
     * E.g.    enabled="alpha beta"
     */
    public void setEnablement(IComponent c, String enabled) {
        if (enabled.charAt(0) == '*')
            enabled = enabled.substring(1);
        EnableMonitor em = _gui.makeEnableMonitor(c);
        em.setProperty(enabled);
    }

    /**
     * The "border" attribute
     */
    public void setBorder(String borderName) throws SAXException {
        if (borderName.equalsIgnoreCase("etched"))
            _border = IComponent.ETCHED_BORDER;
        else if (
            borderName.equalsIgnoreCase("bevel")
                || borderName.equalsIgnoreCase("bevelin"))
            _border = IComponent.BEVEL_IN_BORDER;
        else if (borderName.equalsIgnoreCase("bevelout"))
            _border = IComponent.BEVEL_OUT_BORDER;
        else
            throw new SAXException(
                "Border \"" + borderName + "\" is not recognized");
        if (mComponent != null)
            mComponent.setBorder(_border);
    }

    public void setBorderTitle(String title) {
        _borderTitle = title;
        if (mComponent != null)
            mComponent.setBorderTitle(title);
    }

    public void setEnable_if(Object o) {
        //System.out.println("SET_ENABLE_IF: " + c.getName() + " " + enableIf);
        mEnableIf = o;
    }

    public void setForeground(String colorName) {
        _foreground = _gui.getComponentFactory().makeColor(colorName);
    }

    public void setBackground(String colorName) {
        _background = _gui.getComponentFactory().makeColor(colorName);
    }

    public void setFont(String font) {
        // fontfamily-style-pointsize
        int dash = font.indexOf('-');
        String family = null;
        String style = null;
        String fontsize = null;
        if (dash < 0)
            dash = font.indexOf(' ');
        if (dash > 0) {
            family = font.substring(0, dash);
            int dash2 = font.indexOf('-', dash + 1);
            if (dash2 < 0)
                dash2 = font.indexOf(' ', dash + 1);
            if (dash2 > 0) {
                style = font.substring(dash + 1, dash2);
                fontsize = font.substring(dash2 + 1);
            }
        }
        if (family != null && style != null && fontsize != null) {
            boolean bold = false;
            boolean italic = false;
            if (style.equalsIgnoreCase("BOLD"))
                bold = true;
            else if (style.equalsIgnoreCase("BOLDITALIC")) {
                bold = true;
                italic = true;
            }
            if (style.equalsIgnoreCase("ITALIC")) {
                italic = true;
            }
            int size = 0;
            try {
                size = Integer.parseInt(fontsize);
            }
            catch (NumberFormatException x) {
                throw new IllegalArgumentException(
                    "Bad size in font specification: " + font);
            }
            _font =
                _gui.getComponentFactory().makeFont(family, bold, italic, size);
        }
        else
            throw new IllegalArgumentException(
                "Bad font specification: " + font);
    }
    /**
     * Handle "arg_action" attribute.
     *  "-arg_action=..."
     * This is an action procedure that evaluates to a string.
     * It is appended to the "ARG_ACTION" property list
     */
    public void setArg_action(Object o) {
        mArgAction = o;
    }
    /**
     * This is an action procedure that evaluates to a string.
     * It is appended to the "ACTION" property list
     */
    public void setActionProc(Object o) {
        mActionProc = o;
    }
    /**
     *Arrange to set "VALUE" variable to the text associated with this
     * component when the "action" or "arg_action" argument is evaluated.
     * Then arrange to fire the "action" or "arg_action" procedure when
     * the OK action is fired.
     */
    private void prepareActionProcIfRequired(ITextWrapper getter) {
        if (mArgAction != null)
            doAction(mArgAction, ARG_ACTION, Gui.GEN_ARG_ACTION, getter);
        if (mActionProc != null)
            doAction(mActionProc, "ACTION", Gui.GEN_ARG_ACTION, getter);
    }
    
    protected ITextWrapper getActionValueWrapper(final IComponent c){
        return new ITextWrapper(){

            @Override
            public void setText (String text) {
                
            }

            @Override
            public String getText () {
                return c.isEnabled()?"??VALUE of " + c.getClass().getName() + "??":null;
            }};
    }
    /**
     * Return true if list is empty in regard to constructing "arg_action" stuff.
     */
    @SuppressWarnings("unchecked")
    private static boolean isEmptyList(List<Object> s) {
        if (s.size() > 1)
            return false;
        if (s.size() == 1) {
            Object o = s.get(0);
            if (o == null)
                return true;
            if (o instanceof List)
                return isEmptyList((List<Object>)o);
            if ("".equals(o))
                return true;
            return false;
        }
        return true;
    }

    /**
    * Arrange to execute "action" or "arg_action" associated with button.
    * @param proc the "action" or "arg_action" procedure that evaluates to a string.
    * @param propertyName name of property that is a list of strings to append to.
    * @param actionName the action name to fire the evaluation (usually "OK");
    * @param getter callback to get value to be assigned "VALUE" before
    * evaluating procedure.
    * 
    */
    private void doAction(
        final Object proc,
        final String propertyName,
        final String actionName,
        final ITextWrapper getter) {
        if (getter == null) throw new IllegalArgumentException("getter is null");
        final IEnvironment env = _gui.getEnvironment();
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                IEnvironment save = _gui.getEnvironment();
                _gui.setEnvironment(env);
                Object oldValue = env.getSymbolValue("VALUE");
                try {
                    String value = getter.getText();
                    // Will be null if component isn't enabled
                    if (value != null) {
                        env.putSymbolValue("VALUE", value);
                        if (value.length() > 1 && value.charAt(0) == '"') {
                            value =
                                "\\\""
                                    + value.substring(1, value.length() - 1)
                                    + "\\\"";
                        }
                        else if (value.indexOf(' ') >= 0) {
                            value = "\\\"" + value + "\\\"";
                        }
                        env.putSymbolValue("QUOTED_VALUE", value);
                        appendArgAction(propertyName,proc);
                    }
                }
                catch (Exception x) {
                    if (mThisElement == null)
                        _gui.handleException(x);
                    else {
                        Locator loc = mThisElement.getLocator();
                        _gui.handleException(
                            loc.getSystemId() + ", line=" + loc.getLineNumber(),
                            x);
                    }
                }
                finally {
                    _gui.setEnvironment(save);
                    env.putSymbolValue("VALUE", oldValue);
                }
            }
        };
        _gui.addAction(actionName, a);
    }
    

    /*
     * This function will process property from the string :setOpts
     * which should be added on arg_action as below
     * example:  arg_action = { if {value_matches 1} "-Xqmpyd:setOpts:ARC_macd=1;ARC_mac=1;ARC_mpy=1;ARC_mpy16=1"
     *
     * Note: if we make the SET_OPTS string next to -Xqmpyd with no space
     * then there won't be quote around Xqmpyd when display this string
     * on GUI argument list panel
     */
    protected List<Object> process_SETOPTS(List<Object> s) throws Exception{
   	 List <Object> newList = new ArrayList();
   	 int setOptslength =  SET_OPTS.length();
   	 for ( int i = 0; i < s.size(); i ++){
       	 String opts = (String)s.get(i);
       	 int index = opts.indexOf(SET_OPTS);
       	 
       	 if( index>=0){
       		 String newStr = opts.substring(0,index);
       		 String setoptStr = opts.substring(index);
       		
       		 //System.out.println("newStr" + newStr + " setOptStr " + setoptStr + " len" + setoptStr.length() );

       		 if (setoptStr.length() > setOptslength) { // there are attr
       			 String attrs = setoptStr.substring(setOptslength);
       			 StringTokenizer token = new StringTokenizer(attrs,";");
       			 while(token.hasMoreTokens()){
       				 String pairStr = (String)token.nextToken();
       				 int nameIdx = pairStr.indexOf("=");
       				 if(nameIdx> 0) {
       					 String attrName = pairStr.substring(0,nameIdx);
       					 // has at least one value after=
       					 if(pairStr.length() > nameIdx +1) {
       						 String attrVal =  pairStr.substring(nameIdx +1);
       						 _gui.setProperty(attrName, attrVal);
								
       						// System.out.println("Set Propery " + attrName + " Val:" + attrVal);
       					 }
       				 }

       			 }
       		 }

                
       		 newList.add(newStr);

       	 }
       	 else
       		 newList.add(opts);
        }
   	 return newList;
    }
    /**
     * Given an arg action ("ARG_ACTION") property that references a list,
     * and an expression that evaluates to a list, evaluate the expression
     * and append it to the list.

     * @param propertyName "ARG_ACTION"
     * @param proc the expression that evaluates to a list
     */
    @SuppressWarnings("unchecked")
    protected void appendArgAction(String propertyName, Object proc) {
        try {
            List<Object> s = Coerce.toList(proc, _gui.getEvaluator(), _gui
                    .getEnvironment());
            // Don't append empty strings
            if (!isEmptyList(s)) {
                Object o = _gui.getProperty(propertyName);
                List<Object> list;
                if (o instanceof String) {
                    list = new ArrayList<Object>();
                    list.add(o);
                    _gui.setProperty(propertyName, list);
                } else {
                    list = (List<Object>)o;
                    if (list == null) {
                        list = new ArrayList<Object>();
                        _gui.setProperty(propertyName, list);
                    }
                }
				
                list.addAll(process_SETOPTS(s));
            }
        } catch (Exception x) {
            if (mThisElement == null)
                _gui.handleException(x);
            else {
                Locator loc = mThisElement.getLocator();
                _gui.handleException(loc.getSystemId() + ", line="
                        + loc.getLineNumber(), x);
            }
        }
    }

    static boolean booleanValue(Object value) {
        return !(
            value == null
                || value.equals("false")
                || value.equals("0")
                || value == Boolean.FALSE);
    }
    private String _name;
    private String _enabled;
    private String _tooltip;
    private int _width;
    private int _height;
    private IComponent mComponent;
    private int _border;
    private String _borderTitle;
    private String mDoc = null;
    private String mDocTitle;
    private Object mEnableIf; // "enable_if" expression
    private Element mThisElement;
    //private IEnvironment mEnv;
    // Environment in place when built; needed for actions
    private Object mArgAction; // guihili "arg_action=..."
    private Object mActionProc; // guihili "action=..."
    //private boolean mEnabledState = true;
    // enable property value ignoring "enable_if" expression.

    protected boolean tracing() {
        return sTrace.on();
    }

    protected void trace(String s) {
        if (sTrace.on())
            System.out.println("[GUI COMPONENT] " + s);
    }
    protected IColor _foreground; // Color
    protected IColor _background; // Color
    protected IFont _font; // Font
    private int mVerticalAlignment = -1;
    private int mHorizontalAlignment = -1;
    private boolean mExpandable;
    protected boolean mFinished;  // True when finishComponent called.
    private Object mVisibleIf = null; // "visible" expression
}
