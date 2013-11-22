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


import java.awt.Dimension;
import java.awt.Point;


/**
 * A call-back interface that the form builder uses to construct widgets. It is designed so that the underlying
 * framework can be AWT, Swing, or SWT.
 * <P>
 * The parent container of this component is assumed to have been established by the factory that creates the concrete
 * instance.
 * @author David Pickens
 * @version 9.11.02
 */
public interface IComponent extends IWidget {

    /**
     * Register and observer that is notified when this component's size is changed.
     * @param observer the observer.
     */
    void addObserver (IComponentObserver observer);

    /**
     * Remove a previously registered observer.
     * @param observer the observer to be removed.
     */
    void removeObserver (IComponentObserver observer);

    /**
     * Once all attributes have been set, return the component. Depending on the implementation, this method may lazily
     * instantiate it.
     * <P>
     * The object returned may be an AWT Component, a Swing JComponent, or an SWT Widget.
     * <P>
     * Each call must return the same object.
     */
    Object getComponent ();

    /**
     * Return the parent that this component is in.
     * @return the parent that this component is in.
     */
    IContainer getParent ();

    /**
     * Set titled border on the underlying GUI component.
     * @param title the title to appear on the border.
     * @exception IllegalArgumentException Titled border not valid for this component.
     */
    void setBorderTitle (String title);

    /**
     * Return the border title, if any.
     */
    String getBorderTitle ();

    public static final int NO_BORDER = 0;

    public static final int ETCHED_BORDER = 1;

    public static final int BEVEL_IN_BORDER = 2;

    public static final int BEVEL_OUT_BORDER = 3;

    /**
     * Set the border around the underlying component.
     * @param border one of BEVEL_IN_BORDER, BEVEL_OUT_BORDER, ETCHED_BORDER
     * @exception IllegalArgumentException border not valid for this component.
     */
    void setBorder (int border);

    /**
     * Set the font of the associated component.
     */
    void setFont (IFont font);

    /**
     * Return associated font.
     * @return associated font.
     */
    IFont getFont ();

    /**
     * Set foreground color as a hint.
     */
    void setForeground (IColor color);

    /**
     * Set background color as a hint.
     */
    void setBackground (IColor color);

    /**
     * Return whether or not the underlying component is visible.
     */
    boolean isVisible ();

    /**
     * Set "visible" property.
     */
    void setVisible (boolean v);

    /**
     * Set the grid span in a grid-based container.
     * @param rowSpan the number of cells occupied vertically (typically 1)
     * @param colSpan the number of cells occupied horizontally (typically 1)
     */
    void setGridSpan (int rowSpan, int colSpan);

    /**
     * A value between 0 and 1 that determines how the space for component is stretched vertically when container is
     * lengthened. If 0, it doesn't stretch at all. If 1 it stretches maximally. NOTE: a component's space can stretch
     * but not necessarily fill.
     */
    void setVerticalWeight (double weight);

    /**
     * A value between 0 and 1 that determines how the space for component is stretched horizontally when container is
     * lengthened. If 0, it doesn't stretch at all. If 1 it stretches maximally. NOTE: a component's space can stretch
     * but not necessarily fill.
     */
    void setHorizontalWeight (double weight);

    public static final int CENTER = 0;

    public static final int BEGINNING = 1;

    public static final int END = 2;

    public static final int FILL = 3;

    /**
     * Set alignment of component in its allocated space of the parent. CENTER means in the vertical center. This is the
     * default. BEGINNING means at the top. END means at the bottom. FILL means fill vertically.
     * @param position CENTER, BEGINNING, END, or FILL
     */
    void setVerticalAlignment (int position);

    /**
     * Set alignment of component in its allocated space of the parent. CENTER means in the horizontal center. This is
     * the default. BEGINNING means at the left. END means at the right. FILL means fill horizontally.
     * @param position CENTER, BEGINNING, END, or FILL
     */
    void setHorizontalAlignment (int position);

    public final int DEFAULT = -1;

    /**
     * Given hints of width and height, compute the preferred width and height of this component.
     * <P>
     * More specifically, given a width, what is the preferred height? And, given a height, what is the preferred width?
     * <P>
     * If there are no constraints, then the hints should be zero.
     * @param wHint the constrained width, or {@link #DEFAULT}
     * @param hHint the constrained height, or {@link #DEFAULT}.
     * @return the preferred size
     */
    Dimension computeSize (int wHint, int hHint);

    /**
     * Dispose of this component so that it no longer exists in its parent.
     */
    void dispose ();

    /**
     * Set preferred size.
     * @param width the preferred width
     * @param height the preferred height
     */
    void setPreferredSize (int width, int height);

    /**
     * Set margins around this component. A margin is the number of pixels of space that is appear around this
     * component.
     * @param top the number of pixels to appear at top.
     * @param left the number of pixels to appear at left.
     * @param bottom the number of pixels to appear at bottom.
     * @param right the number of pixels to appear at right.
     */
    void setMargins (int top, int left, int bottom, int right);

    /**
     * Given a pixel position in this component, return the equivalent position using the display coordinates.
     * @param x the x coordinate in this component.
     * @param y the y coordinate in this component.
     * @return the position in the display.
     */
    Point convertToScreenPoint (int x, int y);

    /**
     * Return the width of this component in pixels.
     * @return the width of this component in pixels.
     */
    int getWidth ();

    /**
     * Return the height of this component in pixels.
     * @return the height of this component in pixels.
     */
    int getHeight ();

    /**
     * Do whatever is necessary to get the UI system to repaint this widget.
     */
    void repaint ();

    /**
     * Called when this component's size has changed. Mus tb re-layed out in parents container.
     */
    void revalidate ();

    /**
     * Called by {@linkplain ILayoutManager layout managers} to set the location of this component relative to the
     * parent container.
     * @param x the x position where the upper left-hand-corner is located inside the parent container.
     * @param y the y position where the upper left-hand-corner is located inside the parent container.
     * @param width the width to be assigned this component.
     * @param height the height ot be assigned this component.
     */
    void setBounds (int x, int y, int width, int height);

    /**
     * Return the layout data that is assocated the layout manager of the parent container.
     * @return the layout data associate with the layout manager of the parent container.
     */
    Object getLayoutData ();

    /**
     * Set the layout data that is associated with the parent's layout manager.
     * @param object the layout data.
     */
    void setLayoutData (Object object);

    /**
     * Get the actual size of this component as it is rendered within its parent container.
     * @return the size of this component as it is rendered in its parent container.
     */
    Dimension getSize ();

    /**
     * Assign a name to this component for the benefit of a GUI tester that can reference components by name.
     * @param name name of the component.
     */
    @Override
    void setName (String name);
}
