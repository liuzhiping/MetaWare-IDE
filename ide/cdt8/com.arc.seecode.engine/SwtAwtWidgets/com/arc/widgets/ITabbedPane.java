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
 * A GUI component that serves as a container for tabbed items.
 * Tab items are added by the factory method.
 */
public interface ITabbedPane extends IContainer {
    void setSelectedItem(ITabItem item);

    ITabItem addTab(IComponent component, String title);

//    void layout();
    }

