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
package com.arc.widgets.internal.swt;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import com.arc.widgets.IAttributedString;
import com.arc.widgets.IColor;
import com.arc.widgets.IFont;


/**
 * @author David Pickens
 */
class SWTAttributedString implements IAttributedString {

    private class Style {

        int start;

        int end;

        Color foreground;

        Color background;

        Font font;

        boolean underline;

        Style(int start, int end) {
            this.start = start;
            this.end = end;
            
        }

        Style(int start, int end, Style s) {
            this(start, end);
            foreground = s.foreground;
            font = s.font;
            background = s.background;
            underline = s.underline;
        }

    }

    private List<Style> fRuns = null;

    private String fText;

    private Display fDisplay;

    private int fTabPosition = 0;

    SWTAttributedString(Display display, String s) {
        fDisplay = display;
        fText = s;
    }

    /*
     * (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#getString()
     */
    @Override
    public String getString () {
        return fText;
    }

    private Style[] computeStyleRuns (int startIndex, int endIndex) {
        if (fRuns == null)
            fRuns = new ArrayList<Style>();
        int runStart = -1;
        for (int i = 0; i < fRuns.size(); i++) {
            Style s = fRuns.get(i);
            if (s.end <= startIndex)
                continue;

            if (runStart < 0) {
                // If new run is completely at the left of any existing run.
                if (s.start >= endIndex) {
                    Style style = new Style(startIndex, endIndex);
                    fRuns.add(i, style);
                    return new Style[] { style };
                }
                if (startIndex < s.start && endIndex > s.start) {
                    Style newS = new Style(startIndex, s.start);
                    fRuns.add(i, newS);
                    s = newS;
                }
                // If existing run element overlays new one:
                // If they match exactly, return the existing one.
                // If new one references a fragment, then split the run.
                // If the new one covers more than one run,then return all that are referenced.
                if (s.start <= startIndex && s.end > startIndex) {
                    // If existing range overlaps at its tail, split it up.
                    if (s.start < startIndex) {
                        Style newS = new Style(startIndex, s.end, s);
                        s.end = startIndex;
                        fRuns.add(i + 1, newS);
                        s = newS;
                        i++;
                    }
                    if (s.start == startIndex) { // always true
                        // Exactly match existing run? then return it.
                        if (s.end == endIndex) {
                            return new Style[] { s };
                        }
                        // New one is a subset of existing one. Split the existing one.
                        if (endIndex < s.end) {
                            Style newS = new Style(s.start, endIndex, s);
                            s.start = endIndex;
                            fRuns.add(i, newS);
                            return new Style[] { newS };
                        }
                        // endIndex > s.fEnd
                        int end = endIndex;
                        if (i + 1 < fRuns.size() && fRuns.get(i + 1).start < end) {
                            end = fRuns.get(i + 1).start;
                        }
                        if (s.end < end) {
                            Style newS = new Style(s.end, end, s);
                            fRuns.add(i + 1, newS);
                        }
                        runStart = i;
                    }
                }
            }
            else {
                // New range crosses more than one existing.
                if (endIndex <= s.start){
                    Style last = fRuns.get(i-1);
                    Style newS = new Style(last.end,endIndex);
                    fRuns.add(i,newS);
                    s = newS;
                }
                if (s.end >= endIndex) {
                    if (s.end > endIndex) {
                        Style newS = new Style(endIndex, s.end, s);
                        fRuns.add(i + 1, newS);
                        s.end = endIndex;
                    }
                    Style styles[] = new Style[i - runStart + 1];
                    for (int j = runStart; j <= i; j++) {
                        styles[j - runStart] = fRuns.get(j);
                    }
                    return styles;
                }
            }
        }
        if (runStart < 0){
            Style newS = new Style(startIndex,endIndex);
            fRuns.add(newS);
            return new Style[]{newS};
        }
        Style last = fRuns.get(fRuns.size()-1);
        Style newS = new Style(last.end,endIndex);
        fRuns.add(newS);
        Style styles[] = new Style[fRuns.size()- runStart];
        for (int j = runStart; j < fRuns.size(); j++) {
            styles[j - runStart] = fRuns.get(j);
        }
        return styles;
    }

    /*
     * (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#addForeground(int, int, int)
     */
    @Override
    public void addForeground (int color, int startIndex, int endIndex) {
        int c = getColor(color);
        for (Style style: computeStyleRuns(startIndex,endIndex)){
            style.foreground = fDisplay.getSystemColor(c);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#addBackground(int, int, int)
     */
    @Override
    public void addBackground (int color, int startIndex, int endIndex) {
        int c = getColor(color);
        for (Style style: computeStyleRuns(startIndex,endIndex)){
            style.background = fDisplay.getSystemColor(c);
        }
    }
    
    @Override
    public void addBackground (IColor color, int startIndex, int endIndex) {
        if (color != null) {
            Color c = (Color) color.getObject();
            for (Style style : computeStyleRuns(startIndex, endIndex)) {
                style.background = c;
            }
        }
    }

    private static int getColor (int c) {
        switch (c) {
            case COLOR_SELECTED_LINE_BACKGROUND:
                return SWT.COLOR_LIST_SELECTION;
            case COLOR_SELECTED_LINE_FOREGROUND:
                return SWT.COLOR_LIST_SELECTION_TEXT;
            case COLOR_SELECTED_TEXT_BACKGROUND:
                return SWT.COLOR_INFO_BACKGROUND;
            case COLOR_SELECTED_TEXT_FOREGROUND:
                return SWT.COLOR_INFO_FOREGROUND;
            case COLOR_WHITE:
                return SWT.COLOR_WHITE;
            case COLOR_GRAY:
                return SWT.COLOR_GRAY;
            case COLOR_BLACK:
                return SWT.COLOR_BLACK;
            case COLOR_RED:
                return SWT.COLOR_RED;
            case COLOR_PINK:
                return SWT.COLOR_MAGENTA; // there is no pink
            case COLOR_ORANGE:
                return SWT.COLOR_RED; // there is no orange
            case COLOR_YELLOW:
                return SWT.COLOR_YELLOW;
            case COLOR_GREEN:
                return SWT.COLOR_GREEN;
            case COLOR_MAGENTA:
                return SWT.COLOR_MAGENTA;
            case COLOR_CYAN:
                return SWT.COLOR_CYAN;
            case COLOR_BLUE:
                return SWT.COLOR_BLUE;
            case COLOR_DARK_GREEN:
                return SWT.COLOR_DARK_GREEN;
            case COLOR_DARK_YELLOW:
                return SWT.COLOR_DARK_YELLOW;
            case COLOR_DARK_RED:
                return SWT.COLOR_DARK_RED;
            case COLOR_DARK_BLUE:
                return SWT.COLOR_DARK_BLUE;
            case COLOR_DARK_PURPLE:
                return SWT.COLOR_BLUE; // there is no purple
            case COLOR_DARK_CYAN:
                return SWT.COLOR_DARK_CYAN;
            case COLOR_DARK_GRAY:
                return SWT.COLOR_DARK_GRAY;
            case COLOR_LIGHT_GRAY:
                return SWT.COLOR_GRAY; // there is no light gray
            default:
                throw new IllegalArgumentException("Unrecognized color index: " + c);
        }
    }

    @Override
    public void addFont (IFont font, int startIndex, int endIndex) {
        for (Style style: computeStyleRuns(startIndex,endIndex)){
            style.font = (Font)font.getObject();
        }
    }

    @Override
    public void addUnderline (int startIndex, int endIndex) {
        for (Style style: computeStyleRuns(startIndex,endIndex)){
            style.underline = true;
        }
    }
    
    /**
     * Draw the text string at an x and y position, applying styles
     * appropriately.
     * @param gc the graphics context.
     * @param x the x coordinate to where the line starts.
     * @param y the y coordinate to where the line starts.
     * @param indexAtX character position at x coordinate.
     * @param startIndex the start position (>= indexAtX) at which we are to start drawing.
     * @param endIndex one beyond the last character to draw.
     * @param useDefaultColor if true, then use inherited foreground and
     * background color.
     */
    public void draw(GC gc, int x, int y, int indexAtX, int  startIndex, int endIndex, boolean useDefaultColor){
        int last = indexAtX;
        if (startIndex < indexAtX) startIndex = indexAtX;
        if (endIndex > fText.length()) endIndex = fText.length();
        if (endIndex <= startIndex) return;
        if (fRuns != null) {
            Color defaultForeground = gc.getForeground();
            Color defaultBackground = gc.getBackground();
            Font defaultFont = gc.getFont();
            for (Style r: fRuns){
                if (r.start > last){
                	if (last < startIndex && r.start >= startIndex) {
                		// Skip these characters. They are to the left of the one's we're drawing.
                		x += gc.textExtent(fText.substring(last,startIndex)).x;
                		last = startIndex;
                	}
                	if (last < r.start) {
                		String s = fText.substring(last,Math.min(r.start,endIndex));
                		if (last >= startIndex){
                			gc.drawText(s,x,y);
                		}
                		x += gc.textExtent(s).x;
                		last = Math.min(r.start,endIndex);
                	}
                }
                if (!useDefaultColor) {
                	if (r.foreground != null){
                        gc.setForeground(r.foreground);                    
                    }
                    if (r.background != null){
                        gc.setBackground(r.background);   
                    }
                }
                if (r.font != null){
                    gc.setFont(r.font);                    
                }
                int end = Math.min(endIndex,r.end);
                if (end <= startIndex) {
                	if (last < end) {
                	    String s = fText.substring(last,end);
                	    x += gc.textExtent(s).x;
                	    last = end;
                	}
                }
                else
                if (last < startIndex){
                	String s = fText.substring(last,startIndex);
                	x += gc.textExtent(s).x;
                	last = startIndex;
                }
              
                if (end > last) {
                    String s = fText.substring(last,end);
                    gc.drawText(s,x,y);
                    int w = gc.textExtent(s).x;
                    if (r.underline){
                        int h = gc.textExtent(s).y;
                        gc.drawLine(x,y+h-1,x+w,y+h-1);
                    }
                    x += w;
                    last = end;
                }
                gc.setForeground(defaultForeground);
                gc.setBackground(defaultBackground);
                gc.setFont(defaultFont);
                if (last == endIndex) break;
            }
        }
        
        if (last < startIndex) {
        	String s = fText.substring(last,startIndex);
        	x += gc.textExtent(s).x;
        	last = startIndex;
        }
        if (last < endIndex){
            gc.drawText(fText.substring(last,endIndex),x,y);
        }
    }
    
    /**
     * Record state in XML format for benefit of GUI tester.
     * @param out
     */
    void recordState(PrintStream out, int indent){
        if (fRuns == null) return;
        for (Style r: fRuns) {
            for (int i = 0; i < indent; i++) out.print(' ');
            out.print("<range start=\"" + r.start + "\" end=\"" + r.end + '"');
            if (r.foreground != null){
                out.printf(" foreground=\"#%02x%02x%02x\"", r.foreground.getRed(),r.foreground.getGreen(),r.foreground.getBlue());               
            }
            if (r.background != null){
                out.printf(" background=\"#%02x%02x%02x\"", r.background.getRed(),r.background.getGreen(),r.background.getBlue());               
            }
            if (r.underline) {
                out.print(" underline=\"true\"");
            }
            out.println(">");
            // don't mess with font for time being.
        }
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IAttributedString#setIndentation(int)
     */
    @Override
    public void setIndentation (int position) {
        fTabPosition  = position;
        
    }
    
    int getIndentation(){ return fTabPosition; }

}
