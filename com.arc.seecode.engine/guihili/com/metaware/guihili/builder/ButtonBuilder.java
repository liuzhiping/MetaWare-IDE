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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.xml.sax.SAXException;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.widgets.IImage;
import com.arc.widgets.IWindow;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.ITextWrapper;

/**
 * This class is the builder for a Guihili "button" node.
 * <P>
 * This class would ordinarily be package-private, but is made public
 * because it is accessed by means of reflection.
 * <P>
 * Likewise, all the "setter" methods are made public because so that
 * they can be accessed by reflection. Ordinarily, they would be package-private.
 *
 * This class is accessed by reflection from the <code>guihili.xml</code> file.
 * <P>
 * Attributes are:
 * <ul>
 * <li>text
 * <li>icon
 * <li>action
 * <li>property
 * <li>value
 * </ul>
 * @author David Pickens
 */
public class ButtonBuilder extends ComponentBuilder {
    public ButtonBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) {
        _button = createButton(element);
        setComponent(_button);
    }

    protected IButton createButton(Element element) {
        return _gui.getComponentFactory().makeButton(_gui.getParent());
    }

    /**
     * <dl>
     * <dt><code>on_push=<i>name</i></code>
     * <dd>show window named "name".
     * <dt><code>on_push=$(<i>lisp-expression</i>)</code>
     * <dd>Execute expression.
     * </dl>
     */
    public void setOn_push(Object a) {
        mOnPush = a;
    }

    void setButtonText(IButton button, String text) {
        if (text != null) {
            int i = text.indexOf('$');
            if (i >= 0 && i + 1 < text.length()) {
                _button.setMnemonic(Character.toLowerCase(text.charAt(i + 1)));
                text = text.substring(0, i) + text.substring(i + 1);
            }
        }
        _button.setText(text);
    }

    public void setText(String text) {
        _label = text;
        if (_button.getImage() == null && text.indexOf(' ') < 0) {
            IImage icon = _gui.getAssociatedIcon(text);
            // If name is associated with an icon through the icon mapper, then
            // use the icon.
            if (icon != null) {
                _button.setImage(icon);
                return;
            }
        }
        ITextWrapper wrapper = new ITextWrapper() {
            @Override
            public void setText(String txt) {
                setButtonText(_button, txt);
            }
            @Override
            public String getText() {
                return _button.getText();
            }
        };
        _gui.processTextOrProperty(text, wrapper);
    }

    /**
     * set the ICON attribute.
     * NOTE: an icon attribute of empty-string means that we do not want
     * an ICON. This provides away to suppress icons on menu-items that
     * are tied to actions.
     */
    public void setIcon(String text) {
        _icon = text;
        if (_icon.length() > 0) {
            IIconWrapper wrapper = new IIconWrapper() {
                @Override
                public void setIcon(IImage icon) {
                    _button.setImage(icon);
                }
                @Override
                public IImage getIcon() {
                    return _button.getImage();
                }
            };
            _gui.processIcon(text, wrapper);
        }
    }

    public void setAction(final String actionName) {
        _action = _gui.getAction(actionName);
        /*
         * If action not yet defined, it may be defined later in XML.
         * Arrange to define the action at the end of XML processing.
         */
        if (_action == null) {
            PropertyChangeListener listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent x) {
                    if (x.getNewValue().equals("false")) {
                        _gui.removePropertyChangeListener(actionName, this);
                        _action = _gui.getAction(actionName);
                        if (_action != null) {
                            _button.addActionListener(_action);
                            if (_action instanceof Action)
                                applyActionProperties((Action) _action);
                        }
                        else {
                            System.err.println(
                                "Action " + actionName + " isn't defined!");
                            if (getEnabled() == null)
                                _button.setEnabled(false);
                        }
                    }
                }
            };
            _gui.addPropertyChangeListener(Gui.READING_XML, listener);
        }
    }

    /*
     * Given an associated Action object, extract properties from it.
     */
    private void applyActionProperties(Action action) {
        if (_icon == null) {
            IImage icon = (IImage)action.getValue(Action.SMALL_ICON);
            if (icon != null)
                _button.setImage(icon);
        }
        if (getTooltip() == null) {
            String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
            if (tooltip != null)
                _button.setToolTipText(tooltip);
        }
        if (getEnabled() == null)
            _button.setEnabled(action.isEnabled());
        if (_label == null)
            setButtonText(_button, (String) action.getValue(Action.NAME));
        action.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                String name = event.getPropertyName();
                if (name.equals("enabled") && getEnabled() == null)
                    _button.setEnabled(
                        ((Boolean) event.getNewValue()).booleanValue());
                else if (
                    getTooltip() == null
                        && name.equals(Action.SHORT_DESCRIPTION)) {
                    _button.setToolTipText(event.getNewValue().toString());
                }
                else if (_icon == null && name.equals(Action.SMALL_ICON)) {
                    _button.setImage((IImage)event.getNewValue());
                }
                else if (_label == null && name.equals(Action.NAME))
                    _button.setText((String) event.getNewValue());
            }
        });
    }

    /**
     * return true if string appears to denote "true"
     */
    static boolean isTrue(Object v) {
        if (v == null)
            return false;
        if (v instanceof String) {
            String s = (String) v;
            return s.equals("1") || s.equalsIgnoreCase("true");
        }
        if (v instanceof Integer) {
            return ((Integer) v).intValue() != 0;
        }
        if (v instanceof Boolean)
            return ((Boolean) v).booleanValue();
        return false;
    }
    
    @Override
    public Object returnObject() throws SAXException {
        if (_action != null) {
            _button.addActionListener(_action);
            if (_action instanceof Action)
                applyActionProperties((Action) _action);
        }
        // If "toggle" property set but "property" was not, then 
        // make the toggle property be the property name, and its value is true
        if (_property == null && _toggle != null) {
            _property = _toggle;
            _value = null; // makes it boolean
            _toggle = null;
        }
        if (_property != null) {
            if (_property.equals(_toggle))
                throw new SAXException(
                    "button toggle has same name as property: " + _property);
            /*
             * Arrange to have the button selected if the property is ever
             * set to whatever "value" is set to.
             */
            _gui
                .addPropertyChangeListener(
                    _property,
                    new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    String newValue = event.getNewValue() != null? event.getNewValue().toString() : null;
                    boolean select =
                        _value != null
                            ? _value.equals(newValue)
                            : isTrue(newValue);
                    if (select != _button.isSelected()) {
                        _button.setSelected(select);
                        if (_toggle != null)
                            try {
                                _gui.setProperty(
                                    _toggle,
                                    select ? "true" : "false");
                            }
                            catch (PropertyVetoException x) {
                            }
                    }
                }
            });
            if (_value != null)
                _button.setSelected(_value.equals(_gui.getProperty(_property)));
            else
                _button.setSelected(isTrue(_gui.getProperty(_property)));
        }
        
        if (_toggle != null)
            try {
                _gui.setProperty(
                    _toggle,
                    _button.isSelected() ? "true" : "false");
            }
            catch (PropertyVetoException x) {
            }
        if (_toggle != null || _property != null){
            /*
             * If property specified, set it to "value" or "true" when
             * button is selected.
             */
            ActionListener listener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    try {
                        if (_property != null) {
                            if (_value == null)
                                _gui.setProperty(_property, Boolean.valueOf(_button
                                        .isSelected()));
                            else if (_button.isSelected()) {
                                _gui.setProperty(_property, _value);
                                // If button sets a property, then don't let
                                // it turn off until the property turns it off.
                            }
                        }
                        // "setSelected" does not fire a property change
                        // event. Check for toggle here.
                        if (_toggle != null)
                            _gui.setProperty(_toggle,
                                    _button.isSelected()?"true":"false");
                    } catch (PropertyVetoException x) {
                    }
                }
            };
            _button.addActionListener(listener);
        }
        
        /*
         * if "on_push=name" specified, then make action listener
         * to show window or internal frame named "name"
         */
        if (mOnPush != null) {
            if (mOnPush instanceof String)
                _button.addActionListener(
                    makeShowWindowActionListener(
                        _gui.expandString((String) mOnPush),
                        _gui,
                        _button));
            else
                _button.addActionListener(
                    makeEvalActionListener(castToList(mOnPush), _gui));
        }
  
        return super.returnObject();
    }

	@SuppressWarnings("unchecked")
	private static List<Object> castToList(Object o) {
		return (List<Object>) o;
	}


    static ActionListener makeShowWindowActionListener(
        final String windowName,
        final Gui gui,
        final IButton button) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // A "window" node registers its name as an action to invoke it
                ActionListener a = gui.getAction(windowName);
                if (a != null)
                    a.actionPerformed(event);
                else {
                    IComponent w = gui.getComponent(windowName);
                    if (w instanceof IWindow)
                         ((IWindow) w).open();
                    else if (w != null)
                        gui.getComponentFactory().showErrorDialog(
                            button.getComponent(),
                            "Window is of class " + w.getClass().getName());
                    else
                        gui.getComponentFactory().showErrorDialog(
                            button.getComponent(),
                            "Can't find window " + windowName);
                }
            }
        };
    }

    /**
     * Arrange to invoke action "proc" when action event is fired.
     */
    static ActionListener makeEvalActionListener(
        final List<Object> proc,
        final Gui gui) {
        final IEvaluator eval = gui.getEvaluator();
        final IEnvironment env = gui.getEnvironment();
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // We must run the action after other events because
                // a unexplained bug. If the evaluate causes a popup
                // (e.g., JOptionPane), and we're already a modal dialog,
                // the gui hangs! This fixes it.
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            eval.evaluate(proc, env);
                        }
                        catch (Exception x) {
                            gui.handleException("on_push procedure failed", x);
                        }
                    }
                });
            }
        };
    }

    public void setProperty(String p) {
        _property = p;
        if (getName() == null) setName(p);
    }
    public String getProperty() {
        return _property;
    }

    public void setValue(String value) {
        _value = value;
    }
    
    protected String getValue() { return _value; }

    /**
     * A boolean property that will be set to the state of the button.
     */
    public void setToggle(String property) {
        _toggle = property;
    }

    private IButton _button;
    private String _property;
    private String _toggle;
    private String _value;
    private String _icon;
    private String _label;
    private ActionListener _action;
    private Object mOnPush;
}
