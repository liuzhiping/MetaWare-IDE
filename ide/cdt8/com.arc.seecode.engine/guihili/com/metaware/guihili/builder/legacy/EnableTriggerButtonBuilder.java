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
package com.metaware.guihili.builder.legacy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;
import com.metaware.guihili.builder.ButtonBuilder;
import com.metaware.guihili.builder.EnableMonitor;

/**
 * The Guihili "enable_trigger_checkbox" or "enable_trigger_radiobutton"
 * It causes the "target" component, or container of components, to
 * be enabled or disabled.
 * <P>
 * It also is used to create a card layout. In which case, the target is a 
 * container to be brought to the top of a card layout. See {@link CardsBuilder}.
 */
public class EnableTriggerButtonBuilder extends ButtonBuilder {
    public EnableTriggerButtonBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) {
        _element = element;
        super.startNewInstance(element);
    }

    @Override
    protected IButton createButton(Element element) {
        // Old legacy guihili puts checkboxes in radio button groups.
        // we intercept that here.
        if (element.getName().indexOf("radio") >= 0 ||
            RadioButtonGroupBuilder.isRadioButtonGroup(_gui.getParent()))
            return _gui.getComponentFactory().makeRadioButton(_gui.getParent());
        return _gui.getComponentFactory().makeCheckBox(_gui.getParent());
    }

    private static int sNextTrigger;

    /** Create a name if it isn't given one explicitly so that we
     * can contrive an "enable_if" expression on the target
     */
    private void setContrivedName() {
        if (getName() == null) {
            setName("TRIGGER." + sNextTrigger++);
        }
    }

    @SuppressWarnings("unchecked")
    public void setTarget(Object target) {
        if (target instanceof List)
            mTargets = (List<String>) target;
        else {
            mTargets = new ArrayList<String>(1);
            mTargets.add((String)target);
        }
    }

    public void setDefault(boolean v) {
        mDefault = v;
    }

    /**
     * Register the fact that trigger "trigger" will determine whether or
     * not target component is enabled.
     */
    private void registerTrigger(IComponent target, IButton trigger) {
        if (tracing()) {
            trace(
                "enable_trigger "
                    + (getName() != null ? getName() : "")
                    + " registers "
                    + (target.getName() != null ? target.getName() : "")
                    + " instance of "
                    + target.getClass().getName());
        }
        _gui.registerTrigger(target, trigger);
    }

    /**
     * Called once after all triggers have been gathered
     *
     * Arrange for each component that is targeted by a trigger to respond
     * to trigger as if:
     *   enable_if=(enabled <trigger_name>)
     * specified.
     * If more than one trigger targets a component then we OR them:
     *   enable_if=(or (enabled <trigger1>) (enabled <trigger2>) ...)
     * If there is a preceeding "enable_if" then we and it like so:
     *   enable_if=(and <preceeding> (or (enabled <trigger1>) (enabled <trigger2>) ...))
     */
    private void postProcessTriggers() {
        Map <IComponent,List<IButton>>map = _gui.getTriggerMap();
        if (map == null)
            return;
        for (Map.Entry<IComponent,List<IButton>> entry: map.entrySet()){
            IComponent c = entry.getKey();
            List<IButton> triggers =  entry.getValue();
            List<Object> orList = null;
            int cnt = triggers.size();
            final EnableMonitor em = _gui.makeEnableMonitor(c);
            for (int i = 0; i < cnt; i++) {
                IButton trigger =  triggers.get(i);
                if (trigger.getName() == null)
                    throw new Error("trigger has no name!");
                List<Object> expr = new ArrayList<Object>();
                expr.add("enabled");
                expr.add(trigger.getName());
                if (orList == null)
                    orList = expr;
                else {
                    if (i == 1) {
                        List<Object> newList = new ArrayList<Object>();
                        newList.add("or");
                        newList.add(orList);
                        orList = newList;
                    }
                    orList.add(expr);
                }
                // Force re-evaluating enable state of component when trigger
                // changes.
                trigger.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        em.update();
                    }
                });
            }
            if (em.getEnableIf() != null) {
                List<Object> andList = new ArrayList<Object>();
                andList.add("and");
                andList.add(em.getEnableIf());
                andList.add(orList);
                orList = andList;
            }
            em.setEnableIf(orList);
            em.update();
        }
    }

    @Override
    public Object returnObject() throws SAXException {
        final IButton b = (IButton) super.returnObject();
        b.setSelected(mDefault);
        // Every trigger must have a name for our contrived "enable_if" to see.
        if (getName() == null)
            setContrivedName();

        _gui.invokeAfterReading(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mTargets.size(); i++) {
                    IComponent c = _gui.getComponent( mTargets.get(i));
                    if (c == null)
                        try {
                            error(
                                _element,
                                "Trigger target "
                                    +  mTargets.get(i)
                                    + " does not exist");
                        }
                        catch (Exception x) {
                            _gui.handleException(x.getMessage(), x);
                        }
                    else {
                        registerTrigger(c, b);
                    }
                }
            }
        }, false);

        //
        // We execute the following once to make every component controlled by trigger
        // properly respond to trigger.
        // For a component that has multiple triggers, it is an "OR" relationship.
        // If there is a preceedin "enable_if", then it is an "and"
        //
        if (_gui.getProperty("$enable_triggers_encountered$") == null) {
            try {
                _gui.setProperty("$enable_triggers_encountered$", "1");
            }
            catch (PropertyVetoException x) {
            }
            _gui.invokeAfterReading(new Runnable() {
                @Override
                public void run() {
                    postProcessTriggers();
                }
            }, true);
        }

        return b;
    }
    
    @Override
    protected ITextWrapper getActionValueWrapper(IComponent c){
        final IButton b = (IButton)c;
        return new ITextWrapper() {
            @Override
            public String getText() {
                return b.isEnabled() ? (b.isSelected() ? "1" : "0") : null;
            }
            @Override
            public void setText(String s) {
                b.setSelected(!"0".equals(s));
            }
        };
    }

    private List<String> mTargets;
    private boolean mDefault;
    private Element _element; // for error reporteing
}
