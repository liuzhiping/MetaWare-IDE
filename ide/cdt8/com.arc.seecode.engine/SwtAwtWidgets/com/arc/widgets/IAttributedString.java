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
 * A string with foreground and background color
 * attributes.
 * @author David Pickens
 */
public interface IAttributedString {
    /**
     * Get string stripped being attributed
     * @return string stripped being attributed
     */
    String getString();
    
    /**
     * The color for the background of a selected line.
     */
    public static final int COLOR_SELECTED_LINE_BACKGROUND = 2;
    /**
     * The color for the foreground of a selected line.
     */
    public static final int COLOR_SELECTED_LINE_FOREGROUND = 3;
    /**
     * The color for the background of a selected text.
     */
    public static final int COLOR_SELECTED_TEXT_BACKGROUND = 4;
    /**
     * The color for the foreground of a selected text.
     */
    public static final int COLOR_SELECTED_TEXT_FOREGROUND = 5;
    public static final int COLOR_WHITE = 6; 
    public static final int COLOR_GRAY = 7;
    public static final int COLOR_BLACK = 8; 
    public static final int COLOR_RED = 9;
    public static final int COLOR_PINK = 10; 
    public static final int COLOR_ORANGE = 11;
    public static final int COLOR_YELLOW = 12; 
    public static final int COLOR_GREEN = 13;
    public static final int COLOR_MAGENTA = 14; 
    public static final int COLOR_CYAN = 15;
    public static final int COLOR_BLUE = 16; 
    public static final int COLOR_DARK_GREEN = 17;// dark
    public static final int COLOR_DARK_YELLOW = 18; // dark yellow
    public static final int COLOR_DARK_RED = 19; // dark red
    public static final int COLOR_DARK_BLUE = 20; // dark blue
    public static final int COLOR_DARK_PURPLE = 21; // purple
    public static final int COLOR_DARK_CYAN = 22; // dark cyan
    public static final int COLOR_DARK_GRAY = 23;
    public static final int COLOR_LIGHT_GRAY = 24;
    
    /**
     * Specify foreground color.
     * @param color
     * @param startIndex
     * @param endIndex
     */
    void addForeground(int color,int startIndex, int endIndex);
    
    /**
     * Specify background color from predefined colors.
     * @param color index of predefined color.
     * @param startIndex
     * @param endIndex
     */
    void addBackground(int color,int startIndex, int endIndex);
    
    /**
     * Specify arbitrary background color.
     * @param color the color to apply to the background.
     * @param startIndex
     * @param endIndex
     */
    void addBackground(IColor color,int startIndex, int endIndex);
    
    /**
     * Set underline
     * @param startIndex
     * @param endIndex
     */
    void addUnderline(int startIndex, int endIndex);
    
    /**
     * Add a new font.
     * @param font the font, or <code>null</code> to denote a default font.
     * @param startIndex
     * @param endIndex
     */
    void addFont(IFont font, int startIndex, int endIndex);
    
    /**
     * Set starting position of this string, in characters. An average character width is
     * used. This is how we do tab stops.
     * @param position number of preceeding spaces to emit before emiting this string.
     */
    void setIndentation(int position);

}
