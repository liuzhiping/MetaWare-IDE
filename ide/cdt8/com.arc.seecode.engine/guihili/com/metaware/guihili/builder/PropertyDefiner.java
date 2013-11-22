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
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.MalformedExpressionException;

/**
 * This class  processes a "property" node.
 * <pre>
 * &lt;property name="..." default="..."/&gt;
 * </pre>
 */
public class PropertyDefiner extends Builder {
    private IEnvironment mEnv;

    public PropertyDefiner(Gui gui) {
        super(gui);
        mEnv = gui.getEnvironment();
    }

    /**
     * Called from XML processor via reflection to set
     * the "name" property.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Called from XML processor via reflection to set
     * the "default" property.
     */
    public void setDefault(String value) {
        _default = value;
    }
    
    public void setValue(Object v){
        _value = v;
    }
    
    private static Map<String,Gui.PropertyType> TYPE_MAP = new HashMap<String,Gui.PropertyType>();
    static {
        TYPE_MAP.put("any",Gui.PropertyType.ANY);
        TYPE_MAP.put("int",Gui.PropertyType.INT);
        TYPE_MAP.put("uint",Gui.PropertyType.UINT);
        TYPE_MAP.put("hex",Gui.PropertyType.HEX);
       // TYPE_MAP.put("page8k",Gui.PropertyType.PAGE8K);
    }
    
    /**
     * Set the property type.
     */
    public void setType(String value){
        Gui.PropertyType type = TYPE_MAP.get(value.toLowerCase());
        if (type == null){
            throw new IllegalArgumentException("Invalid property type: " + value);
        }
        _type = type;
    }

    /**
     * Called after XML has been processed.
     */
    @Override
    public Object returnObject() throws SAXException {
        Object obj = _gui.getProperty(_name);
        if (obj == null) {
            try {
                _gui.setProperty(_name, _default);
                _gui.setPropertyType(_name,_type);
            } catch (PropertyVetoException x) {
                throw new SAXException("Can't set attribute " + _name + " to "
                        + _default, x);
            }
        }
        if (_value != null) {
            updatePropertyValue();
            _gui.addPropertyChangeListener(new PropertyChangeListener(){
                private boolean pending = false;
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (!pending) {
                        try {
                            pending = true;
                            updatePropertyValue();
                        } finally{ pending = false; }
                    }                                
                }});
        }
        return null;
    }

    /**
     * @throws PropertyVetoException
     * @throws MalformedExpressionException
     */
    private void updatePropertyValue() {
        try {
            _gui.setProperty(_name, _gui.getEvaluator().evaluate(_value,mEnv));
        } catch (PropertyVetoException e) {
            
        } catch (MalformedExpressionException e) {
            e.printStackTrace();
        }
    }

    private String _name;

    private String _default;
    
    private Object _value;
    
    private Gui.PropertyType _type = Gui.PropertyType.ANY;
}
