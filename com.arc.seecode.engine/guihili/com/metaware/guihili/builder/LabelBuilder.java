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

import org.xml.sax.SAXException;

import com.arc.widgets.IFont;
import com.arc.widgets.ILabel;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

/**
 * Construct a label.
 * Attributes are:
 * <ul>
 * <li>text
 * </ul>
 */
public class LabelBuilder extends ComponentBuilder {
    public LabelBuilder(Gui gui) {
        super(gui);
        // Legacy .opt files permit text attribute to be optional.
        // So initialize with blank
        _label = _gui.getComponentFactory().makeLabel(_gui.getParent(),"");
        setComponent(_label);
    }

    @Override
    public void startNewInstance(Element element) {
    }
    
    public void setItalic(boolean v){
        _italic = v;
    }
    
    public void setWrap(boolean v){
        _label.setWrap(v);
    }

    public void setText(String textString) {
        ITextWrapper wrapper = new ITextWrapper() {
            @Override
            public void setText(String text) {
                _label.setText(text);
            }
            @Override
            public String getText() {
                return _label.getText();
            }
        };
        _gui.processTextOrProperty(textString, wrapper);
    }
    
    @Override
    protected Object returnObject () throws SAXException {
        if (_italic){
            IFont font = _label.getFont();
            IFont newFont = _gui.getComponentFactory().makeFont(font.getName(),false,true,font.getSize());
            _label.setFont(newFont);
        }
        return super.returnObject();
    }
    private ILabel _label;
    private boolean _italic;
 
}
