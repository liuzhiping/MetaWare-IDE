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
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.arc.widgets.IChoice;
import com.arc.widgets.IComponent;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

/**
 * Construct a combo box.
 * Attributes are:
 * <ul>
 * <li>editable
 * <li>property
 * </ul>
 */
public class SelectBuilder extends LabeledComponentBuilder {
    private IChoice mCombo = null;
    private String _optionsProperty;
    private String _valuesProperty = null;
    
    public SelectBuilder(Gui gui) {
        super(gui);
        _options = new ArrayList<OptionBuilder>();
    }

    @Override
    public void startNewInstance(Element element) {
    }

    public void setEditable(boolean v) {
        _editable = v;
    }
    public void setProperty(String p) {
        _property = p;
        if (getName() == null) setName(p);
    }
    
    public void setOptionsProperty(String name){
        _optionsProperty = name;
    }
    
    public void setValuesProperty(String name){
        _valuesProperty = name;
    }

    @Override
    public void addChild(Object child, Element element) throws SAXException {
        OptionBuilder o = (OptionBuilder) child;
        if (o.getCond()) {
           _options.add(o);
           _gui.processTextOrProperty(o.getText(), o);
        }
    }
    
    private static boolean containsArgActions(List<OptionBuilder>list){
        for (OptionBuilder b: list){
            if (b.getArg_action() != null) return true;
        }
        return false;
    }

    @Override
    public Object returnObject() throws SAXException {
        int size = _options.size();
        mCombo.clear();
        Object initValue = "";
        for (int i = 0; i < size; i++) {
            OptionBuilder o =  _options.get(i);
            mCombo.addItem(o.getText());
            }
        if (size > 0) initValue = _options.get(0).getText();
        if (_optionsProperty != null) {
            _gui.setNonpersistent(_optionsProperty); // don't persist
            _gui.setNonpersistent(_optionsProperty); // don't persist
            PropertyChangeListener listener = new PropertyChangeListener() {

                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    updateComboFromOptionsProperty(mCombo);

                }
            };
            _gui.addPropertyChangeListener(_optionsProperty, listener);
            if (_valuesProperty != null) {
                _gui.addPropertyChangeListener(_valuesProperty, listener);
            }
            Object v = _gui.getProperty(_optionsProperty);
            if (v != null) {
                updateComboFromOptionsProperty(mCombo);
            }
        }
        if (_property != null) {
            mCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    int i = mCombo.getSelectionIndex();
                    Object o = mCombo.getText();                  
                    if (i >= 0 && i < _options.size()) {
                        o = _options.get(i).getValue();
                    }
                    else if (mItemValues != null){
                        // Given name, set the property to the actual object.
                        for (Object item: mItemValues){
                            if (item.toString().equals(o)){
                                if (item instanceof OptionValue){
                                    o = ((OptionValue)item).value;
                                }
                                else
                                    o = item;
                                break;
                            }
                        }
                    }
                    try {
                        _gui.setProperty(_property, o);
                    }
                    catch (PropertyVetoException x) {
                    }
                }
            });
            _gui
                .addPropertyChangeListener(
                    _property,
                    new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    String newValue = (String) event.getNewValue();
                    setSelection(mCombo, newValue);
                }
            });
            Object v = _gui.getProperty(_property);
            if (v != null) initValue = v;
            setSelection(mCombo, initValue);
        }
        if (containsArgActions(_options)) {
            ActionListener a = new ActionListener() {

                @Override
                public void actionPerformed (ActionEvent e) {
                    if (mCombo.isEnabled()) {
                        int i = mCombo.getSelectionIndex();
                        if (i < _options.size() && i >= 0) {
                            Object argAction = _options.get(i).getArg_action();
                            if (argAction != null) {
                                SelectBuilder.this.appendArgAction(ARG_ACTION, argAction);
                            }
                        }
                    }

                }
            };
            _gui.addAction(Gui.GEN_ARG_ACTION, a);
        }
        return super.returnObject();
    }

    @Override
    protected ITextWrapper getActionValueWrapper(IComponent c){
        return new ITextWrapper() {
            @Override
            public String getText() {
                int i = mCombo.getSelectionIndex();
                Object o;
                if (i < _options.size())
                    o = _options.get(i).getValue();
                else o = mCombo.getText();
                return mCombo.isEnabled() ? (String) o : null;
            }
            @Override
            public void setText(String s) {
            }
        };
    }

    private void setSelection(IChoice combo, Object value) {
        int size = _options.size();
        for (int i = 0; i < size; i++) {
            OptionBuilder o = _options.get(i);
            if (o.getValue().equals(value)) {
                combo.setSelectionIndex(i);
                return;
            }
        }
        
        if (mItemValues != null) {
            size = mItemValues.size();
            for (int i = 0; i < size; i++){
                Object o = mItemValues.get(i);
                if (o instanceof OptionValue){
                    if (((OptionValue)o).value.equals(value)){
                        combo.setSelectionIndex(i);
                        return;
                    }
                }
                else if (o.equals(value)){
                    combo.setSelectionIndex(i);
                    return;
                }
            }
        }
        if (_editable || combo.getItemCount() == 0)
            combo.setSelection(value.toString());
        else combo.setSelectionIndex(0);
    }
    private String _property;
    private boolean _editable;
    private List<OptionBuilder> _options;
    private List<Object> mItemValues = null;
    /* (non-Javadoc)
     * @see com.metaware.guihili.builder.LabeledComponentBuilder#makeComponent()
     */
    @Override
    protected IComponent makeComponent() {
        mCombo =
            _gui.getComponentFactory().makeComboBox(
                _gui.getParent(),
                _editable);
        setComponent(mCombo);
        return mCombo;
    }
    
    @Override
    protected IComponent getActiveComponent(){
        return mCombo;
    }
    
    static class OptionValue { 
        OptionValue(Object label, Object value){
            this.label = label;
            this.value = value;
        }
        @Override
        public String toString() { return label.toString(); }
        Object label;
        Object value;
    }
    
    protected void updateComboFromOptionsProperty(IChoice combo){      
        Object current = _property != null ? _gui.getProperty(_property) : combo.getText();
        
        combo.removeAllItems();
        Object optionsValues = _gui.getProperty(_optionsProperty);
        List<Object> items = _gui.coerceToList(optionsValues);
        Object firstValue = null;
        boolean found = false;
        if (_valuesProperty != null) {
            Object values = _gui.getProperty(_valuesProperty);
            List<Object> itemValues = _gui.coerceToList(values);
            if (items.size() <= itemValues.size()){
                List<Object> valueList = new ArrayList<Object>(items.size());
                for (int i = 0; i < items.size(); i++) {
                    if (itemValues.get(i).equals(current)) found = true;
                    valueList.add(new OptionValue(items.get(i),itemValues.get(i)));
                }
                items = valueList;
                if (items.size() > 0) firstValue = itemValues.get(0);
            }
        }
        mItemValues  = items;
        
        for (Object v : items) {
            combo.addItem(v.toString());
        }

        // If no default is yet assigned to the property, then assign first
        // value in the list.
        if (!found && firstValue != null) {
            current = firstValue;
            if (this._property != null){
                try {
                    _gui.setProperty(_property,current);
                }
                catch (PropertyVetoException e) {
                    
                }
            }
        }
        if (current != null)
            setSelection(combo,current);
        else if (combo.getItemCount() > 0) {
            combo.setSelectionIndex(0);
        }
    }
}
