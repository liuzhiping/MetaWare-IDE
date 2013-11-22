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
package com.arc.widgets;


/**
 * Defines a scrollable tree list.
 * @author David Pickens
 */
public interface ITreeList extends IComponent {
    /**
     * Define a root tree node.
     * @param label label to for the node, or null.
     * @param icon icon for the node, or null if a default is to be used.
     * @param tooltip atool tip or null.
     * @return a new tree node.
     */
    ITreeNode defineRoot(String label, Object icon, String tooltip);
    
    /**
     * Set the given tree as selected.
     * @param node the tree node to select.
     */
    void select(ITreeNode node);

    /**
     * Add an observer that is notified when <i>any</i> treenode
     * is selected in this list.
     * @param observer
     */
    void addObserver(ITreeNode.IObserver observer);
    /**
     * Remove an observer.
     * @param observer the observer to be removed.
     */
    void removeObserver(ITreeNode.IObserver observer);
}
