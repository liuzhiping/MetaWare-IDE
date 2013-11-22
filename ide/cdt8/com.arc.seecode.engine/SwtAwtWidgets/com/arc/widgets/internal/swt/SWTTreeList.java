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
package com.arc.widgets.internal.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IContainer;
import com.arc.widgets.ITreeList;
import com.arc.widgets.ITreeNode;
import com.arc.widgets.ITreeNode.IObserver;

/**
 * @author David Pickens
 */
class SWTTreeList extends Component implements ITreeList, SelectionListener {
    private Tree _tree;

    private TreeItem _selectedItem;

    private Map<TreeItem,TreeNode> _itemMap = new HashMap<TreeItem,TreeNode>();

    private Map<TreeItem,List<IObserver>> _observerMap = new HashMap<TreeItem,List<IObserver>>();

    private List<IObserver> _observers = null;

    class TreeNode implements ITreeNode {
        private TreeItem _item;

        private boolean _enabled = true;

        TreeNode(TreeItem item) {
            _item = item;
        }

 
        @Override
        public boolean isSelected() {
            return _item == SWTTreeList.this._selectedItem;
        }

 
        @Override
        public String getLabel() {
            return _item.getText();
        }


        @Override
        public void setLabel(String label) {
            _item.setText(label);

        }

        @Override
        public String getToolTip() {
            return null;
        }

        @Override
        public void setToolTip(String tip) {
            //no API call!!
        }

        @Override
        public ITreeNode defineChild(String label, Object icon, String tooltip) {
            TreeItem child = new TreeItem(_item, 0);
            _itemMap.put(child, this);
            child.setText(label);
            if (icon != null)
                child.setImage((Image) icon);
            child.setExpanded(false);
            //child.setToolTip(tooltip);
            return new TreeNode(child);
        }

        @Override
        public void addObserver(IObserver observer) {
            SWTTreeList.this.addObserver(_item, observer);

        }

        @Override
        public void removeObserver(IObserver observer) {
            SWTTreeList.this.removeObserver(_item, observer);
        }
 
        @Override
        public boolean isEnabled() {
            return _enabled;
        }

        @Override
        public void setEnabled(boolean v) {
            if (_enabled != v) {
                _enabled = v;
                _item.setGrayed(v);
            }
        }
        
        public TreeItem getItem(){ return _item; }

        @Override
        public void setExpanded(boolean v) {
             _item.setExpanded(v);       
        }
        
        @Override
        public boolean isExpanded() {
            return _item.getExpanded();
        }
    }

    /**
     * @param parent
     * @param mapper
     */
    public SWTTreeList(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        _tree = new Tree(getParentComposite(), SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL);
        _tree.addSelectionListener(this);
        return _tree;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.ITreeList#defineRoot(java.lang.String,
     *      java.lang.Object, java.lang.String)
     */
    @Override
    public ITreeNode defineRoot(String label, Object icon, String tooltip) {
        getComponent(); // force creation
        TreeItem t = new TreeItem(_tree, 0);
        if (label != null)
            t.setText(label);
        if (icon != null)
            t.setImage((Image) icon);
        return new TreeNode(t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.ITreeList#addObserver(com.arc.widgets.ITreeNode.IObserver)
     */
    @Override
    public synchronized void addObserver(IObserver observer) {
        if (_observers == null) {
            _observers = new ArrayList<IObserver>();
        }
        _observers.add(observer);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.ITreeList#removeObserver(com.arc.widgets.ITreeNode.IObserver)
     */
    @Override
    public synchronized void removeObserver(IObserver observer) {
        if (_observers != null) {
            _observers.remove(observer);
        }
    }

    private synchronized void addObserver(TreeItem t, ITreeNode.IObserver o) {
        List<IObserver> list =  _observerMap.get(t);
        if (list == null) {
            list = new ArrayList<IObserver>();
            _observerMap.put(t, list);
        }
        list.add(o);
    }

    private synchronized void removeObserver(TreeItem t, ITreeNode.IObserver o) {
        List<IObserver> list =  _observerMap.get(t);
        if (list != null) {
            list.remove(o);
            if (list.size() == 0)
                _observerMap.remove(t);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.item instanceof TreeItem) { // should always be true
            TreeItem item = (TreeItem) e.item;
            _selectedItem = item;
            ITreeNode tn = _itemMap.get(item);
            if (/*tn.isEnabled()*/true) {
                ITreeNode.IObserver observers[];
                synchronized (this) {
                    List<IObserver> allObservers = _observers;

                    List<IObserver> list =  _observerMap.get(item);
                    if (allObservers == null) {
                        allObservers = list;
                    } else if (list != null) {
                        allObservers = new ArrayList<IObserver>(_observers.size()
                                + list.size());
                        allObservers.addAll(_observers);
                        allObservers.addAll(list);
                    }
                    if (allObservers == null || allObservers.size() == 0)
                        return;
                    observers =  allObservers
                            .toArray(new ITreeNode.IObserver[allObservers
                                    .size()]);
                }

                for (int i = 0; i < observers.length; i++) {
                    observers[i].onSelected(tn);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITreeList#select(com.arc.widgets.ITreeNode)
     */
    @Override
    public void select(ITreeNode node) {
        _tree.setSelection(new TreeItem[]{((TreeNode)node).getItem()});
        
    }

}
