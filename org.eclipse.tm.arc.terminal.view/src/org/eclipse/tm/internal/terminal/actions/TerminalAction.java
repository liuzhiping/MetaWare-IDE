/*******************************************************************************
 * Copyright (c) 2004, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.tm.internal.terminal.view.ITerminalView;
import org.eclipse.tm.internal.terminal.view.TerminalViewPlugin;

abstract public class TerminalAction extends Action {
    protected final ITerminalView fTarget;
    public TerminalAction(ITerminalView target, String strId) {
		this(target,strId,0);
	}
    public TerminalAction(ITerminalView target, String strId, int style) {
		super("",style); //$NON-NLS-1$

		fTarget = target;

		setId(strId);
	}
	abstract public void run();
	protected void setupAction(String strText, String strToolTip,
			String strImage, String strEnabledImage, String strDisabledImage,
			boolean bEnabled) {
		TerminalViewPlugin plugin;
		ImageRegistry imageRegistry;

		plugin = TerminalViewPlugin.getDefault();
		imageRegistry = plugin.getImageRegistry();
		setupAction(strText, strToolTip, strImage, strEnabledImage,
				strDisabledImage, bEnabled, imageRegistry);
	}
	protected void setupAction(String strText, String strToolTip,
			String strHoverImage, String strEnabledImage, String strDisabledImage,
			boolean bEnabled, ImageRegistry imageRegistry) {
		setupAction(strText,
				strToolTip,
				imageRegistry.getDescriptor(strHoverImage),	
				imageRegistry.getDescriptor(strEnabledImage),	
				imageRegistry.getDescriptor(strDisabledImage),
				bEnabled);
	}
	protected void setupAction(String strText, String strToolTip,
			ImageDescriptor hoverImage, ImageDescriptor enabledImage, ImageDescriptor disabledImage,
			boolean bEnabled) {
		setText(strText);
		setToolTipText(strToolTip);
		setEnabled(bEnabled);
		if (enabledImage != null) {
			setImageDescriptor(enabledImage);
		}
		if (disabledImage != null) {
			setDisabledImageDescriptor(disabledImage);
		}
		if (hoverImage != null) {
			setHoverImageDescriptor(hoverImage);
		}
	}
}
