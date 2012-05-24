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

import javax.swing.DefaultListModel;

import org.xml.sax.SAXException;

import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.ITextField;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

/**
 * This class is the builder for a Guihili "textField" node to produce a
 * textField component.
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
 * Attributes are:
 * <ul>
 * <li>property
 * <li>font
 * <li>editable
 * <li>columns
 * </ul>
 * <P>
 * <B>TO DO: </B> Need to merge attributes from the old guihili
 * <code>text</code> node.
 * 
 * @author David Pickens
 */
public class TextFieldBuilder extends LabeledComponentBuilder {
    public TextFieldBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void addChild(Object obj, Element element) throws SAXException {
        /*
         * if (_action == null) _action = new ActionListener() { public void
         * actionPerformed(ActionEvent event) { _field.postActionEvent(); } };
         * if (obj instanceof IButton) { ((IButton)
         * obj).addActionListener(_action); } else if (obj instanceof IChoice) {
         * ((IChoice) obj).addActionListener(_action); } else if (obj instanceof
         * String) { // Action name // // Important: we must invoke this action
         * prior to any others. // If there is one pending, put this at the
         * front. // Action a = _gui.addAction((String) obj, _action); if (a
         * instanceof CompositeAction) { CompositeAction ca = (CompositeAction)
         * a; ca.remove(_action); ca.prepend(_action); } } else if (obj != null)
         * error( element, "Object " + obj.getClass().getName() + " not valid to
         * be tied to text field");
         *  
         */
    }

    /**
     * A list model that we're to append to.
     */
    public void setList(final String listModelName) {
        mListModelName = listModelName;
    }
    
    public void setEditable(boolean v){
        mEditable = v;
    }

    protected IContainer getParent() {
        return _gui.getParent();
    }

    @Override
    protected IComponent makeComponent() {
        _field = _gui.getComponentFactory().makeTextField(getParent());
        //Must check for this early because Swing implementation
        //can't alter layout after component is realized.
        //
        if (isExpandable()) {
            _field.setHorizontalAlignment(IComponent.FILL);
            _field.setHorizontalWeight(1.0);
        }
        if (mColumns == 0 && !isExpandable())
            mColumns = 8;
        if (mColumns != 0)
            _field.setColumns(mColumns);
        if (!mEditable)
            _field.setEditable(false);
        if (mListModelName != null) {
            Object m = _gui.getProperty(mListModelName);
            if (m != null && m instanceof DefaultListModel)
                _list = (DefaultListModel) m;
            _field.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (_list == null) {
                        try {
                            _list = (DefaultListModel) _gui
                                    .getProperty(mListModelName);
                            if (_list == null) {
                                System.err.println("Can't find list model \""
                                        + mListModelName + "\"");
                                //_field.removeActionListener(this);
                                return;
                            }
                        } catch (ClassCastException x) {
                            System.err
                                    .println("textField list reference \""
                                            + mListModelName
                                            + " is not a list model (it is an instance of "
                                            + _gui.getProperty(mListModelName)
                                                    .getClass().getName() + ")");
                            //_field.removeActionListener(this);
                            return;
                        }
                    }
                    if (!_field.getText().equals("")) {
                        _list.addElement(_field.getText());
                        _dontUpdateProperty++;
                        _field.setText("");
                        _dontUpdateProperty--;
                    }
                }
            });
        }
        if (_property != null)
            wireProperty();
        return _field;
    }
    
    @Override
    protected IComponent getActiveComponent(){
        return _field;
    }

    // An anachronism
    public void setText(String text) {
        if (text.length() > 0 && text.charAt(0) == '*')
            setProperty(text.substring(1));
//        else
//            mDefault = text;
    }

    public void setProperty(String name) {
        _property = name;
        if (getName() == null)
            setName(name);
    }

    private void wireProperty() {
        ITextWrapper wrapper = new ITextWrapper() {
            @Override
            public void setText(String text) {
                _field.setText(text);
            }

            @Override
            public String getText() {
                return _field.getText();
            }
        };
        _gui.processProperty(_property, wrapper);
        Object value = _gui.getProperty(_property);
        if (value == null)
            value = "";
        _field.setText(value.toString());
        //_gui.setPropertyForComponent(_field,_property);
        TextPropertyChange changeListener = new TextPropertyChange(_gui,
                wrapper);
        _gui.addPropertyChangeListener(_property, changeListener);
        
        _field.addObserver(new ITextField.IObserver() {
                        @Override
                        public void selectionChanged(ITextField f) {
                            try {
                                if (_dontUpdateProperty == 0)
                                    _gui.setProperty(_property, f.getText());
                            }
                            catch (PropertyVetoException x) {
                                // Change vetoed. Undo it.
                                _dontUpdateProperty++;
                                try {
                                    Object old = _gui.getProperty(_property);
                                    if (old == null) old = "";
                                    _field.setText(old.toString());
                                }finally{
                                    _dontUpdateProperty--;
                                }
                            }
                        }});
    }

    public void setColumns(int cnt) {
        mColumns = cnt;
    }

    private ITextField _field;

    private DefaultListModel _list;

    private transient int _dontUpdateProperty; //update only if zero

//    private ActionListener _action; // to update property w.r.t. text

    private String _property;

    //private String mDefault;

    private String mListModelName;

    private int mColumns;
    
    private boolean mEditable = true;

    /**
     * @see com.arc.xml.AbstractBuilder#startNewInstance(Element)
     */
    @Override
    protected void startNewInstance(Element element) throws SAXException {
    }
}
