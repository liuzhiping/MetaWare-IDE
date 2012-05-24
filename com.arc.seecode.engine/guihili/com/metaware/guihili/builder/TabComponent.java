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

import java.awt.Color;

import com.arc.widgets.IComponent;

/**
 * A class for representing a tab of a tabbed pane.
 *
 * @author J. David Pickens
 * @version 6/30/99
 */

class TabComponent {
    TabComponent(){ }
    String getTooltip() { return _tooltip; }
    IComponent getComponent() { return _component; }
    String getIcon() { return _icon; }
    String getTitle() { return _title; }
    Color getBackground() { return _background; }
    Color getForeground() { return _foreground; }

    void setIcon(String icon) { _icon = icon; }
    void setTitle(String title) { _title = title; }
    void setTooltip(String tooltip) { _tooltip = tooltip; }
    void setComponent(IComponent component) { _component = component; }
    void setForeground(Color color) { _foreground = color; }
    void setBackground(Color color) { _background = color; }

    private String _tooltip;
    private String _icon;
    private String _title;
    private Color _foreground;
    private Color _background;
    private IComponent _component;
    }
