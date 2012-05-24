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

import com.arc.widgets.IComponent;
import com.arc.widgets.ITreeNode;

/**
 * A callback for creating a treenode as the XML description is
 * walked. See {@link PreferencesPanelBuilder}.
 * @author David Pickens
 */
interface IPreferencesTreeNodeMaker {
    /**
     * Create a tree node.
     * @param label label for the node, or null.
     * @param icon icon for the node, or null.
     * @param tooltip tooltip for the node, or null.
     * @return a new treenode.
     */
    ITreeNode makeTreeNode(String label, Object icon, String tooltip);
    
    /**
     * Arrange for when treenode is selected, an associated
     * component will appear.
     * @param tn the tree node.
     * @param c the component to appear when selected.
     */
    void wireComponent(ITreeNode tn, IComponent c);
}
