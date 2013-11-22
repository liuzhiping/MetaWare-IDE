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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.arc.widgets.ICardContainer;
import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.IScrollPane;
import com.arc.widgets.ISplitPane;
import com.arc.widgets.ITreeList;
import com.arc.widgets.ITreeNode;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * @author David Pickens
 */
public class PreferencesPanelBuilder extends ContainerBuilder implements IPreferencesTreeNodeMaker {
    private List<ITreeNode> _nodes = new ArrayList<ITreeNode>();
    /**
     * @param gui
     */
    public PreferencesPanelBuilder(Gui gui) {
        super(gui);
    }

    /**
     * @see com.arc.xml.AbstractBuilder#addChild(Object, Element)
     */
    @Override
    protected void addChild(Object object, Element child) throws SAXException {
    }
    
    @Override
    public ITreeNode makeTreeNode(String label, Object icon, String tooltip){
        ITreeNode tn = _treeList.defineRoot(label,icon,tooltip);
        _nodes.add(tn);
        return tn;
    }
    
    @Override
    public void wireComponent(final ITreeNode tn, IComponent c){
        // We have contrived things so that each component is within its
        // own scroller. So reference the parent scroller instead.
        if (c.getParent() instanceof IScrollPane){
            c = c.getParent();
        }
        _cards.setCardName(c,""+_index++); // is this necessary??
        EnableMonitor m = _gui.getEnableMonitor(c);
        if (m != null){
            m.addObserver(new EnableMonitor.IEnableObserver(){

                @Override
                public void enablePropertyChanged(IComponent comp) {
                    tn.setEnabled(comp.isEnabled());
                    
                }});
        }
        final IComponent card = c;
        tn.addObserver(new ITreeNode.IObserver(){
            @Override
            public void onSelected(ITreeNode node) {
                _cards.showCard(card);
                
            }});           
    }

    @Override
    protected IContainer makeContainer() {
        ISplitPane container = _gui.getComponentFactory().makeSplitPane(_gui.getParent(),true);
        container.setResizeWeight(0.33);
        container.setDividerLocation(0.33);
        container.setHorizontalAlignment(IComponent.FILL);
        container.setVerticalAlignment(IComponent.FILL);
        _treeList = _gui.getComponentFactory().makeTreeList(container);
        _treeList.setHorizontalAlignment(IComponent.FILL);
        _treeList.setVerticalAlignment(IComponent.FILL);
        _treeList.getComponent();
        _cards = _gui.getComponentFactory().makeCardContainer(container);
        _cards.setHorizontalAlignment(IComponent.FILL);
        _cards.setVerticalAlignment(IComponent.FILL);
        _cards.setMargins(5,7,5,5);
        setComponent(container);
        finishComponent(container);
        _container = container;
        return _cards; // where new panels are placed.
    }

    private int _index; // number of tabs added
    private IContainer _container;
    private ITreeList _treeList;
    private ICardContainer _cards;

    /* (non-Javadoc)
     * @see com.arc.xml.AbstractBuilder#returnObject()
     */
    @Override
    protected Object returnObject() throws SAXException {
        if (_nodes.size() > 0){
            _treeList.select(_nodes.get(0));
        }
//        // Expand all top-most nodes.
//        for (int i = 0; i < _nodes.size(); i++){
//            (_nodes.get(i)).setExpanded(true);
//        }
        return _container;
    }
}

