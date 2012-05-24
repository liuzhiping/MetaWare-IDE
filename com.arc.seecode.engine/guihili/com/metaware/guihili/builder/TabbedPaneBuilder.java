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


import javax.swing.SwingConstants;

import org.xml.sax.SAXException;

import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ITabItem;
import com.arc.widgets.ITabbedPane;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

/**
 * Class for constructing a tabbed pane.
 */
public class TabbedPaneBuilder extends ContainerBuilder {

	public TabbedPaneBuilder(Gui gui) {
		super(gui);
	}

	public void setPosition(String name) {
		mStyle = lookupPlacement(name);
	}

	/**
	* @see com.arc.xml.AbstractBuilder#addChild(Object, Element)
	*/
    @Override
	protected void addChild(Object object, Element child) throws SAXException {
		TabComponent tc = (TabComponent) object;
		String title = tc.getTitle();
		String iconName = tc.getIcon();
		Object icon = _gui.extractIcon(iconName);
		IComponent co = tc.getComponent();
		if (co == null)
			co =
				_gui.getComponentFactory().makeContainer(
					_pane,
					IComponentFactory.COLUMN_STYLE);
		// Anything
		final ITabItem item = _pane.addTab(co, tc.getTitle());
		if (icon != null)
			item.setImage(icon);
		if (tc.getTooltip() != null)
			item.setToolTipText(tc.getTooltip());
		if (title.length() > 1 && title.charAt(0) == '*') {
			_gui.processTextOrProperty(title, new ITextWrapper() {
				@Override
                public void setText(String text) {
					item.setText(text);
				}
				@Override
                public String getText() {
					return item.getText();
				}
			});
		}
		/*
		      if (tc.getForeground() != null)
		          _pane.setForegroundAt(_index, tc.getForeground());
		      if (tc.getBackground() != null)
		          _pane.setBackgroundAt(_index, tc.getBackground());
		 */
		_index++;
	}

	private static int lookupPlacement(String name) {
		name = name.toUpperCase();
		if (name.equals("TOP"))
			return IComponentFactory.TABS_ON_TOP;
		if (name.equals("BOTTOM"))
			return IComponentFactory.TABS_ON_BOTTOM;
		if (name.equals("LEFT"))
			return IComponentFactory.TABS_ON_LEFT;
		if (name.equals("RIGHT"))
			return SwingConstants.RIGHT;
		return IComponentFactory.TABS_ON_RIGHT;
	}

    @Override
	protected IContainer makeContainer() {
		_pane =
			_gui.getComponentFactory().makeTabbedPane(_gui.getParent(), mStyle);
		setComponent(_pane);
		finishComponent(_pane);
		return _pane;
	}

	private ITabbedPane _pane;
	private int _index; // number of tabs added
	private int mStyle = IComponentFactory.TABS_ON_TOP;

}
