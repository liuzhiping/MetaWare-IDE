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
package com.arc.seecode.internal.display;

import java.util.ArrayList;
import java.util.List;

import com.arc.seecode.display.IColorPreferences;
import com.arc.widgets.IAttributedString;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IFont;

/**
 * A line of text with highlight attributes. Some examples of Line hightlights
 * include:
 * <ul>
 * <li>an arrow image representing the current program counter location in
 * source code</li>
 * <li>an image denoting that a breakpoint is set for a line of source code
 * </li>
 * <li>contrasting background color to default background color for characters
 * in the line that have changed since the last update</li>
 * </ul>
 */
public class Line {

    private String mLine; // may contain control characters

    private LineItem fItem = null; // if no tab stops
    
    private List<LineItem> fItems = null; // if there are tab stops.
    
    private List<LineItem> fPreviousItems = null;
    private LineItem fPreviousItem = null;

    private boolean mDirty;

    private long mHighlights;

    private int fTreePrefixLength;

    final static char VERTICAL_BAR = 0x7f;

    /**
     * Represents all Highlights
     */
    public final static int HL_ALL; // Initialized in static block below

    /**
     * Turn on the program counter highlight (typically an arrow image).
     */
    public final static int HL_PC; // Initialized in static block below
    
    /**
     * Turn on the program counter highlight (typically an arrow image) that is
     * on the far left.
     */
    public final static int HL_PC_UNINDENTED; // Initialized in static block below

    // These highlights are all cumulative, except for HL_NO_PC,
    // which means to turn off HL_PC.

    /**
     * Turn on line change highlighting. See
     * {@link #HL_SHOW_CHANGE HL_SHOW_CHANGE}
     */
    public final static int HL_CHGD; // Initialized in static block below

    /**
     * Turn on break point highlighting.
     */
    public final static int HL_BKPT; // Initialized in static block below

    /**
     * Turn on watch pint highlighting
     */
    public final static int HL_WPT; // Initialized in static block below

    /**
     * Turn off the program counter highlight. (i.e. turn off {@link #HL_PC}
     * HL_PC})
     */
    public final static int HL_NO_PC; // Initialized in static block below

    /**
     * Turn on line change highlighting. See {@link #HL_CHGD HL_CHGD}
     */
    public final static int HL_SHOW_CHANGE; // Initialized in static block below

    public final static int HL_OLD_PC; // Initialized in static block below

    /**
     * Turn on break point disabled highlight.
     */
    public final static int HL_BKPT_DISABLED; // Initialized in static block below

    /**
     * Turn off all break point highlights
     */
    public final static int HL_NO_BKPT; // Initialized in static block below

    /**
     * Turn on disabled watch point highlight.
     */
    public final static int HL_WPT_DISABLED; // Initialized in static block below

    /**
     * Turn on multiple breakpoint highlight. A multiple breakpoint means means
     * that there many break point instances on a line.
     */
    public final static int HL_BKPT_MULTIPLE; // Initialized in static block below

    /**
     * Turn on break point highlight for a function.
     */
    public final static int HL_BKPT_SAME_FUNC; // Initialized in static block below

    /**
     * Turn on disabled break point highlight for a function.
     */
    public final static int HL_BKPT_SAME_FUNC_DISABLED; // Initialized in static block

    // below

    /**
     * Turn on expand (-) highlight (for tree node).
     */
    public final static int HL_EXPAND; // Initialized in static block below

    /**
     * Turn on contract (+) highlight (for tree node).
     */
    public final static int HL_CONTRACT; // Initialized in static block below

    /**
     * Turn off expand and contract highlights.
     */
    public final static int HL_NO_EXPAND_CONTRACT; // Initialized in static block

    // below

    /**
     * Turn off change highlighting
     */
    public final static int HL_NO_SHOW_CHANGE; // Initialized in static block below

    /**
     * Turn on lock highlight.
     */
    public final static int HL_LOCK; // Initialized in static block below

    /**
     * Turn off lock highlight.
     */
    public final static int HL_NO_LOCK; // Initialized in static block below

    /**
     * Turn on thread specific breakpoint highlight
     */
    public final static int HL_BKPT_THREAD_SPECIFIC; // Initialized in static block

    // below

    /**
     * Turn on focus highlight
     */
    public final static int HL_FOCUS; // Initialized in static block below

    /**
     * Turn off focus highlight
     */
    public final static int HL_NO_FOCUS; // Initialized in static block below

    /**
     * Turn on other thread breakpoint highlight. Break point is on this line
     * but on some other thread.
     */
    public final static int HL_BKPT_OTHER_THREAD; // Initialized in static block below

    /**
     * Turn on marked highlight.
     */
    public final static int HL_MARKED; // Initialized in static block below

    /**
     * Turn on found highlight.
     */
    public final static int HL_FOUND; // Initialized in static block below

    /**
     * Turn on breakpoint hardware highlight.
     */
    public final static int HL_BKPT_HARDWARE; // Initialized in static block below

    /**
     * Highlight for lines that can have breakpoints set.
     */
    public final static int HL_CAN_SET_BKPT; // Initialized in static block below
    
    /**
     * Highlight to denote a region within an overlay
     */
    public final static int HL_OVERLAY; // Initialized in static block below
    
    /**
     * Highlight to denote a region of read-only memory.
     */
    public final static int HL_READONLY; // Initialized in static block below
    
    /**
     * Highlight to denote a region of misaligned memory.
     */
    public final static int HL_MISALIGNED; // Initialized in static block below

    // If you add another highlight here, be sure to initialize in the static
    // block  below.

    static {
        // Initialize highlight constants and accumulate highlights into
        // HL_ALL.
        int buffer = 0;

        buffer |= (HL_PC = 1);
        buffer |= (HL_CHGD = (1 << 1));
        buffer |= (HL_BKPT = (1 << 2));
        buffer |= (HL_WPT = (1 << 3));
        buffer |= (HL_NO_PC = (1 << 4));
        buffer |= (HL_SHOW_CHANGE = (1 << 5));
        buffer |= (HL_OLD_PC = (1 << 6));
        buffer |= (HL_BKPT_DISABLED = (1 << 7));
        buffer |= (HL_NO_BKPT = (1 << 8));
        buffer |= (HL_WPT_DISABLED = (1 << 9));
        buffer |= (HL_BKPT_MULTIPLE = (1 << 10));
        buffer |= (HL_BKPT_SAME_FUNC = (1 << 11));
        buffer |= (HL_BKPT_SAME_FUNC_DISABLED = (1 << 12));
        buffer |= (HL_EXPAND = (1 << 13));
        buffer |= (HL_CONTRACT = (1 << 14));
        buffer |= (HL_NO_EXPAND_CONTRACT = (1 << 15));
        buffer |= (HL_NO_SHOW_CHANGE = (1 << 16));
        buffer |= (HL_LOCK = (1 << 17));
        buffer |= (HL_NO_LOCK = (1 << 18));
        buffer |= (HL_BKPT_THREAD_SPECIFIC = (1 << 19));
        buffer |= (HL_FOCUS = (1 << 20));
        buffer |= (HL_NO_FOCUS = (1 << 21));
        buffer |= (HL_BKPT_OTHER_THREAD = (1 << 22));
        buffer |= (HL_MARKED = (1 << 23));
        buffer |= (HL_FOUND = (1 << 24));
        buffer |= (HL_BKPT_HARDWARE = (1 << 25));
        buffer |= (HL_CAN_SET_BKPT = (1 << 26));
        buffer |= (HL_PC_UNINDENTED = (1<<27));
        buffer |= (HL_READONLY = (1 << 28));
        buffer |= (HL_OVERLAY = ( 1 << 29));
        buffer |= (HL_MISALIGNED = (1 << 30));

        HL_ALL = buffer;
    }

    public static final char CONTROL_CODE_BAR = LineItem.CONTROL_CODE_BAR; // "--" <-- ascii art, not sent by engine

    public static final char CONTROL_CODE_BAR_BLANK = LineItem.CONTROL_CODE_BAR_BLANK; // "| " <-- ascii art

    public static final char CONTROL_CODE_BAR_TAIL = LineItem.CONTROL_CODE_BAR_TAIL; // "|-" <-- ascii art

    public static final char CONTROL_CODE_BAR_ELBOW = LineItem.CONTROL_CODE_BAR_ELBOW; // "|_" <-- ascii art

    public static final char CONTROL_CODE_BAR_TAIL_EXPAND = LineItem.CONTROL_CODE_BAR_TAIL_EXPAND;

    public static final char CONTROL_CODE_BAR_TAIL_CONTRACT = LineItem.CONTROL_CODE_BAR_TAIL_CONTRACT;

    public static final char CONTROL_CODE_BAR_ELBOW_EXPAND = LineItem.CONTROL_CODE_BAR_ELBOW_EXPAND;

    public static final char CONTROL_CODE_BAR_ELBOW_CONTRACT = LineItem.CONTROL_CODE_BAR_ELBOW_CONTRACT;
    
    private static final char CONTROL_CODE_TAB_STOP = LineItem.CONTROL_CODE_TAB_STOP;  // following word is tab stop in chars
   
    /**
     * Create a new line
     */
    Line() {
        this("");
    }

    Line(String text) {
        // Java initializes array to false.
        mHighlights = 0;
        mLine = text;
        mDirty = true;
        analyze();
    }

    /**
     * Create a new line by making a copy of an existing line.
     *
     * @param l existing line.
     */
    public Line(Line l) {
        mHighlights = l.mHighlights;
        mLine = l.mLine;
        fItem = l.fItem;
        fItems = l.fItems;
        mDirty = true;
    }

    /**
     * Update the text of this line.
     */
    public void update(String text) {
        //printCodedString(text);
        fPreviousItem = fItem;
        fPreviousItems = fItems;
        mLine = text;
        mDirty = true;
        fItems = null;
        fItem = null;
        analyze();
    }

    /**
     * Return whether or not this line needs repainting. If it doesn, then reset
     * this property so that subsequent calls will return false.
     * 
     * @return whether or not this line is "dirty", then reset it.
     */
    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean v) {
        mDirty = v;
        this.invalidateAttributedString();
    }
    
    private void analyze(){
        String lineString = mLine;
        char line[] = lineString.toCharArray();
        int len = line.length;
        // Strip off tree characters
        int i = 0;
        for (i = 0; i < len; i++) {
            char c = line[i];
            if (c == VERTICAL_BAR && i < len-1) {
                char next = line[i+1];
                switch(next){
                case ' ':
                case '-':
                case '_':
                case 'b': i++; break;
                }              
            } else if (c < CONTROL_CODE_BAR || c > CONTROL_CODE_BAR_ELBOW) {
                break;
            }
        }
        fTreePrefixLength = i;
        // Now scan for other control characters
        // If there are tab stops, then split string accordingly.
        int lastTabStop = 0;
        int lastTabStopPosition = i;
        for (; i < len-1; i++) {
            if (line[i] == CONTROL_CODE_TAB_STOP){
                if (fItems == null){
                    fItems = new ArrayList<LineItem>();
                }
                if (lastTabStopPosition < i){
                    fItems.add(new LineItem(lastTabStop,lineString.substring(lastTabStopPosition,i)));
                }
                lastTabStop = line[++i];
                lastTabStopPosition = i+1;
            }
        }
        if (fItems != null){
            fItems.add(new LineItem(lastTabStop,lineString.substring(lastTabStopPosition,len)));
        }
        else if (fTreePrefixLength == 0){
            fItem = new LineItem(0,lineString);
        }
        else fItem = new LineItem(0,lineString.substring(fTreePrefixLength));
       
    }


    /**
     * Convert an engine highlight code to a Line highlight value.
     * 
     * @param engineHighlightCode
     *            a highlight code from the engine
     */
    public static int toHighlight(int engineHighlightCode) {
        if (engineHighlightCode >= 0)
            return (1 << engineHighlightCode);
        else
            return Line.HL_ALL;
    }

    /**
     * Set a line lighlight by processing an engine highlight value for this
     * line. Setting an engine highlight value may turn off previously set
     * values. (e.g. HL_NO_PC will turn off HL_PC and HL_OLD_PC)
     * 
     * <p>
     * <b>Note: </b> Use {@link #toHighlight(int) toHighlight}to convert a
     * highlight code from the engine to Line highlight value. For example: <br>
     * <br>
     * 
     * <code>
     * // converts engine code 2 to Line.HL_BKPT<b>
     * updateHighlight(Line.convertEngineHighlightCode(1));
     * </code>
     * 
     * @param highlight
     *            Line highlight value to update.
     * @return true if any highlights for the line were changed.
     */
    public boolean updateHighlight(int highlight) {
        boolean changed = false;

        long mask = 0L;

        if ((highlight & Line.HL_NO_PC) != 0) {
            mask = (Line.HL_PC | Line.HL_OLD_PC);
        } else if ((highlight & Line.HL_NO_SHOW_CHANGE) != 0) {
            mask = Line.HL_SHOW_CHANGE;
        } else if ((highlight & Line.HL_NO_EXPAND_CONTRACT) != 0) {
            mask = (Line.HL_EXPAND | Line.HL_CONTRACT);
        } else if ((highlight & Line.HL_EXPAND) != 0) {
            mask = Line.HL_CONTRACT;
            // since the mask will keep the highlight from getting turned
            // on below, we turn it on now.
            mHighlights |= Line.HL_EXPAND;
        } else if ((highlight & Line.HL_CONTRACT) != 0) {
            mask = HL_EXPAND;
            // since the mask will keep the highlight from getting turned
            // on below, we turn it on now.
            mHighlights |= Line.HL_CONTRACT;
        } else if ((highlight & Line.HL_NO_BKPT) != 0) {
            mask = (Line.HL_BKPT | Line.HL_BKPT_DISABLED
                    | Line.HL_BKPT_MULTIPLE | Line.HL_BKPT_THREAD_SPECIFIC
                    | Line.HL_BKPT_OTHER_THREAD | Line.HL_BKPT_SAME_FUNC
                    | Line.HL_BKPT_SAME_FUNC_DISABLED | Line.HL_CAN_SET_BKPT
                    | Line.HL_BKPT_HARDWARE);
        } else if ((highlight & Line.HL_NO_LOCK) != 0) {
            mask = HL_LOCK;
        } else if ((highlight & Line.HL_NO_FOCUS) != 0) {
            mask = HL_FOCUS;
        }

        // Use mask to determine if we are turning highlights off or on.
        // NOTE: we do *not* set the highlight bit corresponding to
        // and engine highlight value.
        if (mask != 0) {
            // if mask was set, then we intend to turn some highlights off
            if ((mHighlights & mask) != 0) {
                // turn off highlight
                changed = true;
                mHighlights &= ~mask;
            }
        } else {
            if ((mHighlights & highlight) == 0) {
                // turn on highlight. in the case of a toggle between HL_EXPAND
                // and HL_CONTRACT highlight, the appropriate highlight was
                // already
                // turned on. see above where the mask was set.
                changed = true;
                mHighlights |= highlight;
            }
        }

        //if (changed) mAtrStr = null;

        if (changed)
            setDirty(true);
        return changed;

    }

    /**
     * Check if a highlight is on
     * 
     * @return true if highlight is on
     */
    public boolean isHighlightOn(int highlight) {
        return ((mHighlights & highlight) != 0);
    }

    public boolean isExpandContractIndented() {
        String treeChars = getTreeChars();

        return ((treeChars.indexOf(Line.CONTROL_CODE_BAR_TAIL_EXPAND) != -1)
                || (treeChars.indexOf(Line.CONTROL_CODE_BAR_TAIL_CONTRACT) != -1)
                || (treeChars.indexOf(Line.CONTROL_CODE_BAR_ELBOW_EXPAND) != -1) || (treeChars
                .indexOf(Line.CONTROL_CODE_BAR_ELBOW_CONTRACT) != -1));
    }

    //    public boolean equals(Line l) {
    //        return ((mLine.equals(l.mLine)) && (mHighlights == l.mHighlights));
    //        }

    public void clear() {
        if (mLine.length() > 0)
            mDirty = true;
        mLine = "";
        fItem = null;
        fItems = null;
        mHighlights = 0;
        this.fTreePrefixLength = 0;
    }


    /**
     * clear highlights for the line
     * 
     * @param highlight
     *            highlight to clear. Highlights to clear can be or'd
     *            (Line.HL_PC|Line.HL_BKPT) together
     * @return true if any highlights changed.
     */
    public boolean clearHighlights(long highlight) {
        if ((mHighlights & highlight) == 0) {
            return false;
        } else {
            mHighlights &= ~highlight;
            setDirty(true);
            invalidateAttributedString();
            return true;
        }
    }

    public String getTreeChars() {
        int i;
        int endOfTreeChars = fTreePrefixLength;

        if (endOfTreeChars == 0)
            return new String("");

        String treeChars = mLine.substring(0, endOfTreeChars);
        if (treeChars.indexOf(VERTICAL_BAR) != -1) {
            String convert = "";

            for (i = 0; i < treeChars.length(); i += 2) {
                switch (treeChars.charAt(i + 1)) {
                    case 'b':
                        convert += CONTROL_CODE_BAR_BLANK;
                        break;
                    case ' ':
                        convert += CONTROL_CODE_BAR;
                        break;
                    case '-':
                        convert += CONTROL_CODE_BAR_TAIL;
                        break;
                    case '_':
                        convert += CONTROL_CODE_BAR_ELBOW;
                        break;
                }
            }

            treeChars = convert;
        }

        if (isHighlightOn(Line.HL_EXPAND | Line.HL_CONTRACT)) {
            i = treeChars.indexOf(Line.CONTROL_CODE_BAR_ELBOW);
            if (i != -1) {
                if (isHighlightOn(Line.HL_EXPAND)) {
                    treeChars = treeChars.replace(Line.CONTROL_CODE_BAR_ELBOW,
                            Line.CONTROL_CODE_BAR_ELBOW_EXPAND);
                } else {
                    treeChars = treeChars.replace(Line.CONTROL_CODE_BAR_ELBOW,
                            Line.CONTROL_CODE_BAR_ELBOW_CONTRACT);
                }
            } else {
                i = treeChars.indexOf(Line.CONTROL_CODE_BAR_TAIL);
                if (i != -1) {
                    if (isHighlightOn(Line.HL_EXPAND)) {
                        treeChars = treeChars.replace(
                                Line.CONTROL_CODE_BAR_TAIL,
                                Line.CONTROL_CODE_BAR_TAIL_EXPAND);
                        printCodedString(treeChars);
                    } else {
                        treeChars = treeChars.replace(
                                Line.CONTROL_CODE_BAR_TAIL,
                                Line.CONTROL_CODE_BAR_TAIL_CONTRACT);
                    }
                }
            }
        }

        return treeChars;
    }

    /**
     * Map a Square_canvas index to an index that the engine understands.
     * 
     * @param index
     *            a Square_canvas index value
     * @return a engine index value.
     */
    public int engineIndex(int index) {

        // The engine's starting index for text is 1, not 0.
        // 0 is reserved to indicate that something left of the text
        // was mouse clicked. (e.g. The expand and contract images
        // for tree displays). The starting index for text in
        // Square_canvas starting index for text
        // is 0. Square_canvas uses the index -1 to indciate that the
        // mouse was clicked left of the text.
        // 
        // Note that when a tree is displayed, the engine uses specical
        // characters to instruct the UI to draw the lines that
        // connect the tree nodes to it's leafs. So we account for that
        // as well when mapping to engine index values.

        if (index < 0)
            return 0;
        else
            return 1 + index + this.fTreePrefixLength;
    }


    private void printCodedString(String s) {
        char[] a = s.toCharArray();
        String outbuf = "";

        for (int i = 0; i < a.length; i++) {
            if (a[i] < 128) {
                outbuf += a[i];
            } else {
                outbuf += "'\\u" + Integer.toHexString(a[i]) + "'";
            }
        }
        //        System.out.println(outbuf);
    }
    
    /**
     * Return the tab stop position for the give item number (0 based).
     * @return the tab stop position.
     */
    public int getItemTabStop(int index){
        if (fItems == null){
            if (index != 0) throw new IndexOutOfBoundsException("" + index);
            return fItem.getTabStop();
        }
        return fItems.get(index).getTabStop();
    }
    
    public int getItemCount(){
        if (fItems != null) return fItems.size();
        return 1;
    }
    
    public int getLength(){
        if (fItems == null) 
            if (fItem == null) return 0;
            else return fItem.getTabStop() + fItem.getString().length();
        if (fItems.size() == 0) return 0;
        LineItem item = fItems.get(fItems.size()-1);
        return item.getTabStop() + item.getString().length();
    }
    
    public String getString(int index){
        if (fItems == null) {
            if (index != 0) throw new IndexOutOfBoundsException(""+index);
            return fItem.getString();
        }
        return fItems.get(index).getString();
    }
    
    public void invalidateAttributedString(){
        if (fItem != null) fItem.invalidateAttributedString();
        if (fItems != null){
            for (LineItem item: fItems){
                item.invalidateAttributedString();
            }
        }
    }
    
    public String getString(){
        if (getItemCount() == 1) return getString(0);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < getItemCount(); i++){
            if (i > 0) b.append('\t');
            b.append(getString(i));
        }
        return b.toString();
    }
    
    /**
     * The background color if the line is to be highlighted in a special way.
     * @param prefs color preferences.
     * @return the background color to be used, or <code>null</code> if the default is used.
     */
    public IColor getBackgroundColor(IColorPreferences prefs){
        IColor background = null;
        if (isHighlightOn(Line.HL_READONLY|Line.HL_OVERLAY|Line.HL_MISALIGNED)){
            if (isHighlightOn(Line.HL_READONLY)){
                background = prefs.getReadonlyBackgroundColor();
            }
            else if (isHighlightOn(Line.HL_OVERLAY)){
                background = prefs.getOverlayBackgroundColor();
            }
            else if (isHighlightOn(Line.HL_MISALIGNED)){
                background = prefs.getMisalignedBackgroundColor();
            }
        }
        return background;
    }
    
    
    public IAttributedString[] getAttributedStrings (IComponentFactory factory, IFont monospace, IFont proportional) {
        LineItem prev = null;
       

        if (fItems == null) {
            if (isHighlightOn(Line.HL_CHGD) || isHighlightOn(Line.HL_SHOW_CHANGE))
                prev = fPreviousItem;
            return new IAttributedString[] { fItem.getAttributedString(factory, monospace, proportional, prev) };
        }
       
        IAttributedString[] result = new IAttributedString[fItems.size()];
        for (int i = 0; i < result.length; i++) {
            LineItem item = fItems.get(i);
            if (fPreviousItems != null && (isHighlightOn(Line.HL_CHGD) || isHighlightOn(Line.HL_SHOW_CHANGE))) {

                prev = fPreviousItems.get(i);
            }
            result[i] = item.getAttributedString(factory, monospace, proportional, prev);
        }
        return result;
    }


    public boolean equals(Line l) {
        if (l == null)
            return false;
        if (!mLine.equals(l.mLine))
            return false;
        if (mHighlights != l.mHighlights)
            return false;
        return true;
    }
    
    @Override
    public String toString(){
    	return getString();
    }

}
