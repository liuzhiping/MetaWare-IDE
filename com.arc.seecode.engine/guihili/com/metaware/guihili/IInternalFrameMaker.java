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
package com.metaware.guihili;


/**
 * A callback for making an internal dialog frame.
 * We set this in the {@link Gui} object so that guihili can
 * construct an appropiate dialog when necessary.
 * If {@link #makeInternalDialog(Object,boolean)} returns null, then an instance of
 * JDialog will be created.
 *
 * @author David Pickens
 * @version May 21, 2002
 */
public interface IInternalFrameMaker {
    /**
     * Create an internal frame that is possibly modal, or else return null if
     * one can't be made.
     * @param owner the "owner" if this is a dialog; a Component
     * under AWT, or a Control under SWT.
     * @param modal true if it is a modal dialog.
     * @return an instance of JDialog or JInternalFrame, or
     * its SWT equivalent.
     */
    public Object makeInternalDialog(Object owner, boolean modal);
    /**
     * Given a frame created by {@link #makeInternalDialog(Object,boolean)}, make it visible.
     */
    public void showFrame(Object frame);
    }
