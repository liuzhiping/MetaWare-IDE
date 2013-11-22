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
package com.arc.widgets.internal.swing;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

import javax.swing.UIManager;

import com.arc.widgets.IAttributedString;
import com.arc.widgets.IColor;
import com.arc.widgets.IFont;

/**
 * @author David Pickens
 */
class SwingAttributedString implements IAttributedString {
    private AttributedString mAttributedString;
    private int fTabStop;

    SwingAttributedString(String s){
        mAttributedString = new AttributedString(s);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#getString()
     */
    @Override
    public String getString() {
        return mAttributedString.toString();
    }
    
    AttributedString getAttributedString(){
        return mAttributedString;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#addForeground(int, int, int)
     */
    @Override
    public void addForeground(int color, int startIndex, int endIndex) {
        mAttributedString.addAttribute(TextAttribute.FOREGROUND,
                getColor(color),startIndex,endIndex);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#addBackground(int, int, int)
     */
    @Override
    public void addBackground(int color, int startIndex, int endIndex) {
        mAttributedString.addAttribute(TextAttribute.BACKGROUND,
                getColor(color),startIndex,endIndex);

    }
    
    @Override
    public void addBackground(IColor c, int startIndex, int endIndex) {
        if (c != null)
            mAttributedString.addAttribute(TextAttribute.BACKGROUND,
                c.getObject(),startIndex,endIndex);

    }
    
    private static Color SELECTED_LINE_BACKGROUND = new Color(0,255,255);
    private static Color SELECTED_TEXT_BACKGROUND = UIManager.getColor("TextArea.selectionBackground");
    private static Color SELECTED_TEXT_FOREGROUND = UIManager.getColor("TextArea.selectionForeground");
    private static Color PURPLE = new Color(128,0,128);
    
    private static Color getColor(int c){
        switch(c){
            case COLOR_SELECTED_LINE_BACKGROUND:
                return SELECTED_LINE_BACKGROUND;
            case COLOR_SELECTED_LINE_FOREGROUND:
                return Color.BLACK;
            case COLOR_SELECTED_TEXT_BACKGROUND:
                return SELECTED_TEXT_BACKGROUND;
            case COLOR_SELECTED_TEXT_FOREGROUND:
                return SELECTED_TEXT_FOREGROUND;
            case COLOR_WHITE:
                return Color.WHITE;
            case COLOR_GRAY:
                return Color.GRAY;
            case COLOR_BLACK:
                return Color.BLACK;
            case COLOR_RED:
                return Color.RED;
            case COLOR_PINK:
                return Color.PINK;
            case COLOR_ORANGE:
                return Color.ORANGE;
            case COLOR_YELLOW:
                return Color.YELLOW;
            case COLOR_GREEN:
                return Color.GREEN;
            case COLOR_MAGENTA:
                return Color.MAGENTA;
            case COLOR_CYAN:
                return Color.CYAN;
            case COLOR_BLUE:
                return Color.BLUE;
            case COLOR_DARK_GREEN:
                return Color.GREEN.darker();
            case COLOR_DARK_YELLOW:
                return Color.YELLOW.darker();
            case COLOR_DARK_RED:
                return Color.RED.darker();
            case COLOR_DARK_BLUE:
                return Color.BLUE.darker();
            case COLOR_DARK_PURPLE:
                return PURPLE;
            case COLOR_DARK_CYAN:
                return Color.CYAN.darker();
            case COLOR_DARK_GRAY:
                return Color.GRAY.darker();
            case COLOR_LIGHT_GRAY:
                return Color.GRAY.brighter();
            default:
                throw new IllegalArgumentException("Unrecognized color index: " +c);
        }
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#addFont(com.arc.widgets.IFont, int, int)
     */
    @Override
    public void addFont (IFont font, int startIndex, int endIndex) {
        mAttributedString.addAttribute(TextAttribute.FONT,font.getObject(),startIndex,endIndex);
        
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#addUnderline(int, int)
     */
    @Override
    public void addUnderline (int startIndex, int endIndex) {
        mAttributedString.addAttribute(TextAttribute.UNDERLINE,TextAttribute.UNDERLINE_ON,startIndex,endIndex);
        
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#setIndentation(int)
     */
    @Override
    public void setIndentation (int position) {
        fTabStop = position;
        
    }
    
    int getIndentation(){
        return fTabStop;
    }

}
