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
 * A node of a tree list.
 * @author David Pickens
 */
public interface ITreeNode {
    /**
     * A callback interface that is invoked when
     * a treenode is selected.
     * @author David Pickens
     */
    static interface IObserver {
        /**
         * Called when observed node is selected.
         * @param node the node that was selected.
         */
        void onSelected(ITreeNode node);
    }
    
    /**
     * Return whether or not this node is the selected one.
     * @return whether or not this node is the selected one.
     */
    boolean isSelected();
    /**
     * Return the label.
     * @return the label.
     */
    String getLabel();
    
    void setLabel(String label);
    
    /**
     * Return the tooltip
     * @return the tool tip.
     */
    String getToolTip();
    
    /**
     * Set the tooltip text.
     * @param tip the tooltip text.
     */
    void setToolTip(String tip);
    /**
     * Define a subnode.
     * @param label the label of the new child, or null.
     * @param icon an icon to be used for node, or null if default to be used.
     * @param tooltip a tool tip or null.
     */
    ITreeNode defineChild(String label, Object icon, String tooltip);
    
    /**
     * Add an observer when that is called when this treenode 
     * is selected.
     * @param observer
     */
    void addObserver(IObserver observer);
    void removeObserver(IObserver observer);

    boolean isEnabled();
    void setEnabled(boolean v);
    
    /**
     * Programmatically expand/contract this treenode.
     * @param v if true, expand, otherwise, contract.
     */
    void setExpanded(boolean v);
    
    /**
     * Return whether or not if the treenode is expanded.
     * @return whether or not the treenode is expanded.
     */
    boolean isExpanded();
}
