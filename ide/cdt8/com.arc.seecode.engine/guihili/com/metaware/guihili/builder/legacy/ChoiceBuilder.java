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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.arc.mw.util.Command;
import com.arc.widgets.IButton;
import com.arc.widgets.IChoice;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImage;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.IEnvironment;
import com.metaware.guihili.IEvaluator;
import com.metaware.guihili.ITextWrapper;
import com.metaware.guihili.builder.LabeledComponentBuilder;

/**
 * Construct an noneditable combobox with a button on the right for clearing it.
 * Attributes are:
 * <dl>
 * <dt>property : name
 * <dd>name of property to be updated by the combo box.
 * <dt>columns: int
 * <dd>width of the combobox
 * <dt>expandable: boolean
 * <dd>boolean to determine if this component stretches in its layout
 * <dt>list: list
 * <dd>list of values
 * <dt>arg_action: expression
 * <dd>evaluated when OK button pressed
 * <dt>default: string
 * <dd>initial setting.
 * <dt>exec_list_lines: list
 * <dd>external process to invoke; output serves as list.
 * <dt>label: string
 * <dd>Label to prefix combo box.
 * </dl>
 * 
 * For hysterical reasons, the VALUE symbol is set in the environment to the
 * value of the combobox.
 */
public class ChoiceBuilder extends LabeledComponentBuilder {
    private List<String> mValues;

    public ChoiceBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) {
    }

    /**
     * A list model that we're to append to.
     */
    public void setList(List<String> list) {
        if (mList == null)
            mList = new ArrayList<String>(list.size());
        mList.addAll(list);
    }

    /**
     * An argument that evaluates to a list, but is deferred.
     */
    @SuppressWarnings("unchecked")
    public void setEval_list(Object list) {
        if (list instanceof List) {
            setList((List<String>)list);
        } else if (list instanceof String) {
            if (mList == null)
                mList = new ArrayList<String>();
            mList.add((String) list);
        }
    }

    /**
     * A program to invoke from which we capture the list. Each line sent to
     * standard output appears in the list.
     */
    public void setExec_list_lines(List<String> list) {
        try {
            String cmd[] = list.toArray(new String[list.size()]);
            IEvaluator eval = _gui.getEvaluator();
            IEnvironment env = _gui.getEnvironment();
            for (int i = 0; i < cmd.length; i++) {
                cmd[i] = eval.expandString(cmd[i], env).toString();
            }
            StringWriter capture = new StringWriter(200);
            Command.invoke(cmd, capture, capture);
            BufferedReader reader = new BufferedReader(new StringReader(capture
                    .toString()));
            if (mList == null)
                mList = new ArrayList<String>();
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                mList.add(line);
            }
        } catch (Exception x) {
            _gui.handleException(x);
        }
    }
    
    public void setValues(List<String> list){
         if (mValues == null)
            mValues = new ArrayList<String>(list.size());
        mValues.addAll(list);
    }

    public void setAction(Object o) {
        super.setActionProc(o);
    }

    public void setProperty(String property) {
        mProperty = property;
        // If property is given and no name has been assigned, then use the property
        // name as the name. GUI tester needs something to look for.
        if (getName() == null && _gui.getComponent(property) == null){
            setName(property);
        }
    }

    private static String listNameOf(String name) {
        return name + "_LIST";
    }

    private void updateProperties() {
        String value = null;
        if (mValues != null){
            int i = mCombo.getSelectionIndex();
            if ( i >= 0 && i < mValues.size())
                value = mValues.get(i);           
        }
        if (value == null)
            value = mCombo.getText();
        try {
            _gui.setProperty(mProperty, value);
        } catch (PropertyVetoException x) {
        }
    }

    private void updateListProperty() {
        try {
            _gui.setProperty(listNameOf(mProperty), getItemList());
        } catch (PropertyVetoException e) {
        }
    }

    @SuppressWarnings("unchecked")
    private void connectProperty(final String property) {
        //_gui.setPropertyForComponent(mCombo, property);
        final String listPropName = listNameOf(property);
        ITextWrapper wrapper = new ITextWrapper() {
            @Override
            public void setText(String text) {
                if (text.length() > 0 || mCombo.getItemCount() > 0) {
                    boolean set = false;
                    if (mValues != null){
                        int i = mValues.indexOf(text);
                        if (i >= 0){ 
                            mCombo.setSelectionIndex(i);
                            set = true;
                        }
                    }
                    if (!set)
                        mCombo.setSelection(text);
                    //updateListProperty();
                    if (mClear != null)
                        mClear.setEnabled(mCombo.isEnabled());
                }
            }

            @Override
            public String getText() {
                if (mValues != null) {
                    int i = mCombo.getSelectionIndex();
                    if ( i >= 0 && i < mValues.size()){
                        return mValues.get(i);
                    }
                }
                return mCombo.getText();
            }
        };
        _gui.processProperty(property, wrapper);
        mCombo.addObserver(new IChoice.IObserver() {
            @Override
            public void selectionChanged(IChoice c) {
                updateProperties();
            }
        });
        // Action listener is invoked after combobox loses focus, or user hit enter key.
        // Update the dropdown list in such a case.
        mCombo.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed (ActionEvent e) {
                updateListProperty();               
            }});
        _gui.addPropertyChangeListener(listPropName,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        Object o = _gui.getProperty(listPropName);
                         if (o instanceof List)
                            setItemListFromProperty((List<Object>)o);
                    }
                });
        Object list = _gui.getProperty(listPropName);
        if (list instanceof List)
            setItemListFromProperty((List<Object>) list);
    }

    private void setItemListFromProperty(List<Object> l) {
        List<String> existing = getItemList();
        String current = mCombo.getText();
        for (Object le: l){
            if (!existing.contains(le))
                mCombo.addItem(le.toString());
        }
        mCombo.setSelection(current);
        if (l.size() > 0 && mClear != null)
            mClear.setEnabled(mCombo.isEnabled());
    }

    @Override
    public Object returnObject() throws SAXException {
        Object result = super.returnObject();
        String name = getName();
        // If tagged with ID, make sure it references
        // the combobox.
        if (name != null){
            _gui.setComponent(name,mCombo);
        }
        return result;
    }
    
    @Override
    protected ITextWrapper getActionValueWrapper(IComponent c){
        return new ITextWrapper() {
            @Override
            public String getText() {
                return mCombo.isEnabled() ? (String) mCombo.getText() : null;
            }

            @Override
            public void setText(String s) {
                mCombo.setSelection(s);
            }
        };
    }

    /**
     * Initial value
     */
    public void setDefault(String def) {
        mDefault = def;
    }

    public void setDefault_evaluated(List<String> list) {
        if (mList == null)
            mList = new ArrayList<String>();
        mList.addAll(list);
    }

    public void setColumns(int cnt) {
        mColumns = cnt;
    }

    List<String> getItemList() {
        if (mValues != null){
            return mValues;
        }
        int cnt = mCombo.getItemCount();
        ArrayList<String> list = new ArrayList<String>(cnt);
        for (int i = 0; i < cnt; i++)
            list.add((String) mCombo.getItemAt(i));
        return list;
    }
    
    /**
     * This is the one to assign the name to.
     * @return the component to assign the name to.
     */
    @Override
    protected IComponent getActiveComponent(){
        return mCombo;
    }

    @Override
    protected IComponent makeComponent() {
        // If is editable, then we add a "clear" button
        IContainer parent = _gui.getParent();
        IComponent result = null;
        IContainer container = null;
        if (isEditable()) {
            container = _gui.getComponentFactory().makeContainer(parent,
                    IComponentFactory.ROW_STYLE);
            container.setHorizontalSpacing(0);
            if (isExpandable()) {
                container.setHorizontalAlignment(IComponent.FILL);
                container.setHorizontalWeight(1.0);
            }
            parent = container;
            result = container;
        }
        mCombo = _gui.getComponentFactory().makeComboBox(parent, isEditable());
        if (mColumns < 0 && isEditable()) mColumns = 16; // a reasonable default.
        if (mColumns > 0)
            mCombo.setColumns(mColumns);
        // Must check for this early because Swing implementation
        // can't alter layout after component is realized.
        if (isExpandable()) {
            mCombo.setHorizontalAlignment(IComponent.FILL);
            mCombo.setHorizontalWeight(1.0);
        }
        if (result == null)
            result = mCombo;
        else {
            mCombo.getComponent(); // instantiate
            mClear = _gui.getComponentFactory().makeButton(parent);
            mClear.setEnabled(false);
            mCombo.addObserver(new IChoice.IObserver() {
                @Override
                public void selectionChanged(IChoice c) {
                    mClear.setEnabled(c.isEnabled()
                            && c.getItemCount() > 0);
                }
            });
            //	    mCombo.addPropertyChangeListener("enabled", new
            // PropertyChangeListener(){
            //		public void propertyChange(PropertyChangeEvent event){
            //		    mClear.setEnabled(mCombo.isEnabled() && mCombo.getItemCount() >
            // 0);
            //		    }
            //		});
            URL gif = ChoiceBuilder.class.getResource("x.gif");
            IImage image = _gui.getComponentFactory().makeImage(gif);
            if (image != null)
                mClear.setImage(image);

            mClear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int i = mCombo.getSelectionIndex();
                    if (i >= 0)
                        mCombo.removeItemAt(i);
                    String value = "";
                    if (mCombo.getItemCount() == 0)
                        mClear.setEnabled(false);
                    else
                        value = mCombo.getText();
                    // Deleting an item does not necessarily fire an
                    // action event; so, we do its effects here.
                    updateProperties();
                    if (mProperty != null) {
                        try {
                            _gui.setProperty(mProperty, value);
                            _gui.setProperty(listNameOf(mProperty),
                                    getItemList());
                        } catch (PropertyVetoException x) {
                        }
                    }
                }
            });
        }
        if (mList == null && mValues != null){
            mList = mValues; // Old legacy treats "list" and "values" as the same
        }
        if (mList != null) {
            int cnt = mList.size();
            for (int i = 0; i < cnt; i++) {
                mCombo.addItem((String) _gui.evaluate(mList.get(i)));
            }
            if (cnt > 0 && mClear != null)
                mClear.setEnabled(mCombo.isEnabled());
            // If no default specified, default to first element of
            // list.
            if (mDefault == null && cnt > 0)
                mDefault = mList.get(0);
        }
        if (mProperty != null)
            connectProperty(mProperty);

        if (mDefault != null && mDefault.length() > 0) {
            if (mProperty != null) {
                if (_gui.getProperty(mProperty) == null)
                    try {
                        _gui.setProperty(mProperty, mDefault);
                    } catch (PropertyVetoException e) {
                    }
            } else
                mCombo.setSelection(mDefault);
        }
        //finishComponent(mCombo); -- done by caller
        return result;
    }

    protected boolean isEditable() {
        return false;
    }
    
    private int mColumns = -1; // default

    private IChoice mCombo;

    private IButton mClear;

    private String mDefault;

    private List<String> mList;

    private String mProperty;
}
