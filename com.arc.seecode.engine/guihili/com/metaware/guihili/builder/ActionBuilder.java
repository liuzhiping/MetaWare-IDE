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

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import org.xml.sax.SAXException;

import com.arc.widgets.IImage;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

// import metaware.js.JavaScript;

/**
 * This class is the builder for a Guihili "action" node.
 * <P>
 * This class would ordinarily be package-private, but is made public because it
 * is accessed by means of reflection.
 * <P>
 * Likewise, all the "setter" methods are made public because so that they can
 * be accessed by reflection. Ordinarily, they would be package-private.
 * <P>
 * This class is accessed by reflection from the <code>guihili.xml</code>
 * file.
 * <P>
 * 
 * @author David Pickens
 */
public class ActionBuilder extends Builder {
    public ActionBuilder(Gui gui) {
        super(gui);
    }

    /**
     * Handle the <code>name</code> attribute. Set the name of the action.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Handle the <code>label</code> attribute. The label that is to appear on
     * any button that references this action.
     */
    public void setLabel(String label) {
        _label = label;
    }

    /**
     * Handle the <code>icon</code> attribute. The icon that is to appear on
     * any button that references this action.
     */
    public void setIcon(String icon) {
        _icon = icon;
    }

    public void setTooltip(String tooltip) {
        _tooltip = tooltip;
    }

    public void setEnabled(String enabled) {
        _enabled = enabled;
    }

    /**
     * Add a JavaScript script to invoke when action is performed.
     */
    // public void addChild(Object object, Element child)throws SAXException {
    // if (object instanceof Script){
    // if (_scripts == null) _scripts = new ArrayList(2);
    // _scripts.add(object);
    // }
    // else
    // throw new SAXException("Bad child of \"action\" node");
    // }
    /**
     * Called after XML has been processed.
     */
    @Override
    public Object returnObject() throws SAXException {
        Action action = null;
        /*
         * If javascripts to be executed...
         */
        /***********************************************************************
         * if (_scripts != null){ action = new AbstractAction(){ int pending =
         * 0; public void actionPerformed(ActionEvent event){ if (pending > 0)
         * return; // Prevent infinite recursion. pending++; JavaScript js =
         * _gui.getJavaScript(); int size = _script.size(); for (int i = 0; i <
         * size; i++){ Script s = (Script)_scripts.get(i); js.doScript(s); }
         * pending--; } }; }
         **********************************************************************/
       /* if (action != null)
            _gui.addAction(_name, action);
        else*/ {
            /*
             * If there is a current actionlistener defined, and it is an
             * instance of Action, then apply the attributes.
             */
            final ActionListener a = _gui.getAction(_name);
            if (a != null) {
                if (a instanceof Action) {
                    action = (Action) a;
                } else {
                    CompositeAction ca = new CompositeAction(_name);
                    action = ca;
                    ca.add(a);
                    _gui.removeAction(_name, a);
                    _gui.addAction(_name, action);
                }
            } else {
                action = new CompositeAction(_name);
                _gui.addAction(_name, action);
            }
        }
        if (_label == null)
            _label = _name;
        final Action action_ = action;
        _gui.processTextOrProperty(_label, new ITextWrapper() {
            @Override
            public void setText(String s) {
                action_.putValue(Action.NAME, s);
            }

            @Override
            public String getText() {
                return (String) action_.getValue(Action.NAME);
            }
        });
        if (_icon != null)
            _gui.processIcon(_icon, new IIconWrapper() {
                @Override
                public void setIcon(IImage icon) {
                    action_.putValue(Action.SMALL_ICON, icon);
                }

                @Override
                public IImage getIcon() {
                    return (IImage) action_.getValue(Action.SMALL_ICON);
                }
            });
        if (_tooltip != null)
            _gui.processTextOrProperty(_tooltip, new ITextWrapper() {
                @Override
                public void setText(String s) {
                    action_.putValue(Action.SHORT_DESCRIPTION, s);
                }

                @Override
                public String getText() {
                    return (String) action_.getValue(Action.SHORT_DESCRIPTION);
                }
            });

        if (_enabled != null) {
            _gui.addPropertyChangeListener(_enabled,
                    new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            Object newValue = event.getNewValue();
                            action_.setEnabled(isTrueValue(newValue));
                        }
                    });
            action_.setEnabled(isTrueValue(_gui.getProperty(_enabled)));
        }
        return null;
    }

    private static boolean isTrueValue(Object b) {
        return b != null && (b == Boolean.TRUE || b.equals("true")
                || b instanceof Integer && ((Integer) b).intValue() != 0);
    }

    private String _name;

    private String _label;

    private String _icon;

    private String _tooltip;

    private String _enabled;
    // private List _scripts; // list of JavaScripts to execute.
}
