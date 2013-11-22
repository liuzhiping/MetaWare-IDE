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
 * A dialog or frame window.
 * The factory determines whether or not it is modal.
 */
public interface IWindow {
    /**
     * Open dialog. If modal, it won't return until dismissed.
     */
    void open();

    /**
     * Pack every component to its preferred size.
     */
    void pack();

    /**
     * Set size.
     */
    void setSize(int width, int height);

    /**
     * Set position on screen.
     */
    void setPosition(int x, int y);

    /**
     * Hide dialog by making it invisible.
     */
    void hide();

    /**
     * Dispose of this dialog.
     */
    void dispose();
    
    /**
     * Return true if this window has been disposed.
     * @return true if this window has been disposed.
     */
    boolean isDisposed();

    /**
     * Get content in which we can add components.
     *  For swing, the result will be an instance of <code>java.awt.Container</code>.
     */
    IContainer getContents();
    
    /**
     * Return the actual underlying widget.
     */
    Object getComponent();
    
    /**
     *  Set location on screen
     */
    void setLocation(int x, int y);
    
    int getWidth();
    
    int getHeight();

    /**
     * Finish laying out contents after everything added.
     */
    void layout();

    /**
     * Set contents.
     */
    void setContents(IContainer container);

    /**
     * Set title
     */
    void setTitle(String title);
    
    /**
     *  Set image to be shown when iconified.
     */    
    void setImage(IImage image);
    
    /**
     * Return title
     */
    String getTitle();
    
    /**
     * Given a button that is embedded in this window, declare it to be
     * the "default". That is, it will be pressed when the enter key is pressed
     * when this window has focus.
     * @param button button to be pressed in enter key pressed.
     */
    void setDefaultButton(IButton button);

    /**
     * Add window listener
     */
    void addWindowObserver(IObserver l);
    void removeWindowObserver(IObserver l);
    
    public interface IObserver {
        public void windowClosing(IWindow w);
        public void windowClosed(IWindow w);
        public void windowActivated(IWindow w);
        public void windowDeactivated(IWindow w);
        public void windowIconified(IWindow w);
        public void windowDeiconified(IWindow w);
    }

}
