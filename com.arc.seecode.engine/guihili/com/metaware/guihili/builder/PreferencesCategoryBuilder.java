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

import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IScrollPane;
import com.arc.widgets.ITreeNode;
import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.arc.xml.IBuilder;
import com.metaware.guihili.Gui;

/**
 * @author David Pickens
 */
public class PreferencesCategoryBuilder extends Builder implements
        IPreferencesTreeNodeMaker {
    private boolean _componentSeen = false;

    private String _label = null;

    private String _tooltip = null;

    private Object _icon = null;

    private IPreferencesTreeNodeMaker _parentBuilder;

    private ITreeNode _treeNode;

    private boolean _cond = true; // whether or not this category is included
    
    private boolean _scroll = true; // set to false if not scrolling.

    /**
     * @param gui
     */
    public PreferencesCategoryBuilder(Gui gui) {
        super(gui);
    }

    public void setTitle(String t) {
        _label = t;
    }

    public void setIcon(String name) {
        _icon = _gui.extractIcon(name);
    }

    public void setTooltip(String tip) {
        _tooltip = tip;
    }

    /**
     * If false, then this category is skipped
     * 
     * @param cond
     */
    public void setCond(boolean cond) {
        _cond = cond;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.xml.AbstractBuilder#addChild(java.lang.Object,
     *      com.arc.xml.Element, com.arc.xml.IBuilder)
     */
    @Override
    protected void addChild(Object object, Element child) throws SAXException {

        if (object instanceof IComponent) {
            IComponent c = (IComponent)object;
            // We've contrived things so that each things goes
            //into its own scroll pane. Thus, reference the scroll pane.
            if (_scroll && c.getParent() instanceof IScrollPane){
                object = c = c.getParent();
            }
            if (_componentSeen)
                throw new SAXException(
                        "Only one component under \"category\" node permitted");
            _componentSeen = true;
            _parentBuilder.wireComponent(_treeNode, c);
        }
        super.addChild(object, child);
    }
    
    public void setScroll(boolean v){
        _scroll = v;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.xml.AbstractBuilder#beginChildren(com.arc.xml.Element)
     */
    @Override
    protected void beginChildren(Element element) throws SAXException {
        super.beginChildren(element);
        if (_cond)
            _treeNode = _parentBuilder.makeTreeNode(_label, _icon, _tooltip);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.xml.AbstractBuilder#returnObject()
     */
    @Override
    protected Object returnObject() throws SAXException {
        if (_treeNode != null && !_componentSeen) {
            // Put out blank
            IContainer c = _gui.getComponentFactory().makeContainer(
                    _gui.getParent(), IComponentFactory.COLUMN_STYLE);
            wireComponent(_treeNode, c);
        }
        return _treeNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.metaware.guihili.builder.IPreferencesTreeNodeMaker#makeTreeNode(java.lang.String,
     *      java.lang.Object, java.lang.String)
     */
    @Override
    public ITreeNode makeTreeNode(String label, Object icon, String tooltip) {
        return _treeNode.defineChild(label, icon, tooltip);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.metaware.guihili.builder.IPreferencesTreeNodeMaker#wireComponent(com.arc.widgets.ITreeNode,
     *      com.arc.widgets.IComponent)
     */
    @Override
    public void wireComponent(ITreeNode tn, IComponent c) {
        _parentBuilder.wireComponent(tn, c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.xml.IBuilder#build(com.arc.xml.Element,
     *      com.arc.xml.IBinding, com.arc.xml.IBuilder)
     */
    @Override
    public Object build(Element e, IBinding binding, IBuilder parentBuilder)
            throws SAXException {
        _parentBuilder = (IPreferencesTreeNodeMaker) parentBuilder;
        return super.build(e, binding, parentBuilder);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.xml.AbstractBuilder#doChild(com.arc.xml.Element,
     *      com.arc.xml.IBinding)
     */
    @Override
    protected Object doChild (Element child, IBinding parentBinding) throws SAXException {
        if (_cond) {
            if (_scroll && !child.getName().equals("category")) {
                // Make each "card" individually scrollable.
                IContainer parent = _gui.getParent();
                IScrollPane scrollable = _gui.getComponentFactory().makeScrollPane(parent, false);
                _gui.setParent(scrollable);
                try {
                    super.doChild(child, parentBinding);
                    return scrollable;
                }
                finally {
                    _gui.setParent(parent);
                }
            }
            else
                return super.doChild(child, parentBinding);
        }
        return null;
    }
}
