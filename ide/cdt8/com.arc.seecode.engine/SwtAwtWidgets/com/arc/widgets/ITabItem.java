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
 * A tab for a {@link ITabbedPane}.
 */
public interface ITabItem {

    /**
     * Get title of tab.
     */
    String getText ();

    void setText (String txt);

    Object getImage ();

    void setImage (Object image);

    ITabbedPane getParent ();

    void setToolTipText (String tip);

    String getToolTipText ();

    /**
     * Get component occupying the pane.
     */
    Object getComponent ();
}
