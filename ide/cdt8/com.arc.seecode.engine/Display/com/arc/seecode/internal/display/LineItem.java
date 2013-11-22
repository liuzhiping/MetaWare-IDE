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

import com.arc.widgets.IAttributedString;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IFont;

/**
 * An item in a line. Each item has a tabstop, text, and style characters.
 * Typically, there is just one item per line, but  multi-column table may have
 * any number.
 * @author David Pickens
 */
class LineItem {
    // Control Code Constants. These are also in the engine gui.h:
    public static final char CONTROL_CODE_START = '\u8000';

    private static final char CONTROL_CODE_COLOR = '\u8001';

    private static final char CONTROL_CODE_COLOR_END = '\u8019';

    //private static final char CONTROL_CODE_RGB       = '\u8020';
    public static final char CONTROL_CODE_BAR = '\u8021'; // "--" <-- ascii art, not sent by engine

    public static final char CONTROL_CODE_BAR_BLANK = '\u8022'; // "| " <-- ascii art

    public static final char CONTROL_CODE_BAR_TAIL = '\u8023'; // "|-" <-- ascii art

    public static final char CONTROL_CODE_BAR_ELBOW = '\u8024'; // "|_" <-- ascii art

    public static final char CONTROL_CODE_BAR_TAIL_EXPAND = '\u8025';

    public static final char CONTROL_CODE_BAR_TAIL_CONTRACT = '\u8026';

    public static final char CONTROL_CODE_BAR_ELBOW_EXPAND = '\u8027';

    public static final char CONTROL_CODE_BAR_ELBOW_CONTRACT = '\u8028';

    //private static final char CONTROL_CODE_PLAIN = '\u8030'; // turn off bold, italic, and underline

    //The following three constants may be "OR'd" together
    private static final char CONTROL_CODE_BOLD = '\u8031'; // set bold

    private static final char CONTROL_CODE_ITALIC = '\u8032'; // set italic

    private static final char CONTROL_CODE_UNDERLINE = '\u8034'; // Plain with underline
    
    public static final char CONTROL_CODE_TAB_STOP = '\u8038';  // following word is tab stop in chars
    private static final char CONTROL_CODE_FONT_PROPORTIONAL = '\u8039'; //switch to proportional font
    private static final char CONTROL_CODE_FONT_MONOSPACE = '\u803A';

    private char[] fText;
    private String fRaw;
    private int fTabStop;
    private IAttributedString fAttr;

    public LineItem(int tabStop, String text){
        fTabStop = tabStop;
        fText = text.toCharArray();
    }
    
    public int getTabStop() { return fTabStop; }
    
    /**
     * Return the attributed string which will be passed to a TextLayout to render the item.
     * <P>
     * <B>NOTE:</B> the returned value may be cached so that subsequent calls will ignore
     * the arguments. If the default font is changed, then @{link {@link #invalidateAttributedString()}
     * should be called to clear the cached value.
     * @param monospace the default monospace font.
     * @param prev previous value of the item if differences are to be highlighted.
     * @return the attributed string.
     */
    public IAttributedString getAttributedString(IComponentFactory factory,IFont monospace, IFont proportional, LineItem prev) {
        if (fAttr != null)
            return fAttr;
        String raw = getString();
        IAttributedString atr = factory.makeAttributedString(raw);
        if (raw.length() > 0) {
            if (prev != null) {
                applyDiffs(atr, raw, prev.getString());
            }
            applyColors(atr, fText);
            applyStyles(atr, factory, fText, monospace,proportional);
        }
        atr.setIndentation(getTabStop());
        fAttr = atr;
        return atr;
    }
    
    /**
     * Returns the raw text value of this string -- that is, with all meta characters
     * removed.
     * @return the raw text.
     */
    public String getString(){
        if (fRaw == null) fRaw = computeRaw(fText);
        return fRaw;
    }
    
    public void invalidateAttributedString(){
        fAttr = null;
    }
    
    private static final int[] CONTROL_COLOR = new int[] {
        IAttributedString.COLOR_WHITE, IAttributedString.COLOR_GRAY,
        IAttributedString.COLOR_BLACK, IAttributedString.COLOR_RED,
        IAttributedString.COLOR_PINK, IAttributedString.COLOR_ORANGE,
        IAttributedString.COLOR_YELLOW, IAttributedString.COLOR_GREEN,
        IAttributedString.COLOR_MAGENTA, IAttributedString.COLOR_CYAN,
        IAttributedString.COLOR_BLUE, IAttributedString.COLOR_DARK_GREEN,// dark
                                                                        // green
        IAttributedString.COLOR_DARK_YELLOW, // dark yellow
        IAttributedString.COLOR_DARK_RED, // dark red
        IAttributedString.COLOR_DARK_BLUE, // dark blue
        IAttributedString.COLOR_DARK_PURPLE, // purple
        IAttributedString.COLOR_DARK_CYAN, // dark cyan
        IAttributedString.COLOR_DARK_GRAY,
        IAttributedString.COLOR_LIGHT_GRAY, };
    
    private static void applyColors(IAttributedString atr, char[] text) {
        int[] fgColor = getForegroundColors(text);

        if (fgColor.length != 0) {
            for (int i = 0; i < fgColor.length; i += 3) {
                atr.addForeground(
                        CONTROL_COLOR[fgColor[i]], fgColor[i + 1],
                        fgColor[i + 2]);
            }
        }
    }
    
    private static void applyStyles(IAttributedString atr, IComponentFactory factory, char[]text,
            IFont monospace, IFont proportional) {

        int[] styles = getStyles(text);
        for (int i = 0; i < styles.length; i += 3) {
            int code = styles[i];
            if ((code & (STYLE_BOLD|STYLE_ITALIC|STYLE_PROPORTIONAL)) != 0) {
                IFont font = monospace;
                if ( (code & STYLE_PROPORTIONAL) != 0) {
                    font = proportional;
                }
                boolean bold = (code & STYLE_BOLD) != 0;
                boolean italic = (code & STYLE_ITALIC) != 0;
                if (bold || italic)
                    font = factory.makeFont(font.getName(),bold,italic,font.getSize());
                atr.addFont(font,styles[i + 1], styles[i + 2]);
            }
            if ((code & STYLE_UNDERLINE) != 0) {
                atr.addUnderline(styles[i + 1],styles[i + 2]);
            }
        }
    }

    private static int[] getDiffs(String rawLine, String prevRawLine) {
       
        int maxLen = Math.max(rawLine.length(),prevRawLine.length())+1;

        // worse case: if every other char is different, then we have maxLen/2 differences.
        // since we need start and end indices for each diff, the diffs array need maxLen 
        // elements.
        //    diffs[i+0] - start index of i-th different substring
        //    diffs[i+1] - end index of i-th different substring

        int[] diffs = new int[maxLen];
        int d = 0;

        if ((rawLine.length() == 0) || (rawLine.equals(prevRawLine))) {
            return new int[0];
        }

        if (prevRawLine.length() == 0) {
            // we don't have a previous line. so how line is different?
            diffs[d++] = 0;
            diffs[d++] = rawLine.length();
        } else {
            // at this point we now now that line and prevRawLine
            // are not zero length strings and have a least one difference
            // between them.
            char[] l = rawLine.toCharArray();
            char[] pl = prevRawLine.toCharArray();

            int i = 0;

            boolean inDiff = false;

            for (i = 0; (i < l.length) && (i < pl.length); i++) {
                if (l[i] != pl[i]) {
                    if (!inDiff) {
                        // entered new diff region
                        diffs[d++] = i;
                    }
                    inDiff = true;
                } else {
                    if (inDiff) {
                        // leaving diff region
                        diffs[d++] = i;
                    }
                    inDiff = false;
                }
            }

            if (inDiff) {
                // still in a diff region. handle end of line.
                // since we only display the current line end the 
                // diff at the last character of the current line.
                diffs[d++] = l.length;
            }
        }

        //truncate and return array
        int[] returnDiffs = new int[d];
        for (int i = 0; i < returnDiffs.length; i++) {
            returnDiffs[i] = diffs[i];
        }

        return returnDiffs;
    }

    private static int[] getForegroundColors(char l[]) {

        // fgColor[i+0] - color index of i-th region
        // fgColor[i+1] - start index in string stripped of control characters of i-th region
        // fgColor[i+2] - end index in string stripped of control characters or i-th region
        int[] fgColor = new int[l.length * 3];

        int c = 0;
        boolean coloring = false;
        boolean textHighlighted = false;
        char currentColor = '\u0000';

        int i; // index of string with control characters
        int j; // new index of string after all the control characters are stripped
        //System.out.println("get foreground color");
        for (i = 0, j = 0; i < l.length; i++) {
            if ((l[i] >= CONTROL_CODE_COLOR && l[i] <= CONTROL_CODE_COLOR_END)
                    && (i < l.length - 1) && (l[i] != currentColor)) {
                //System.out.println("  found new color at " + i + " and not at eol");
                // we have a new color control character and we are not on the 
                // last character of the line

                if (textHighlighted) {
                    // we have some text that uses the current hightlight
                    // wrap up the bookkeeping for the current color and 
                    // increment the data structure
                    fgColor[c + 2] = j;
                    c += 3;
                    textHighlighted = false;
                }

                currentColor = l[i];
                fgColor[c] = currentColor - CONTROL_CODE_COLOR;
                fgColor[c + 1] = j;
                coloring = true;
            }
            if (l[i] < CONTROL_CODE_START) {
                if (coloring)
                    textHighlighted = true;
                j++;
            }
        }

        if (textHighlighted) {
            // we are at the last character of the line.
            fgColor[c + 2] = j;
            c += 3;
        }
        //System.out.println("  c = " + c);
        //truncate and return array
        int[] returnFgColors = new int[c];
        for (int k = 0; k < returnFgColors.length; k += 3) {
            if (fgColor[k + 1] == fgColor[k + 2]) {
                // color on a zero length substring, skip.
                continue;
            }
            returnFgColors[k] = fgColor[k];
            returnFgColors[k + 1] = fgColor[k + 1];
            returnFgColors[k + 2] = fgColor[k + 2];
        }

        return returnFgColors;
    }
    
    private static void applyDiffs(IAttributedString atr, String rawString, String prevRawString) {
        if (prevRawString != null) {
            int[] diffs = getDiffs(rawString,prevRawString);
            if (diffs.length != 0) {
                for (int i = 0; i < diffs.length; i += 2) {
                    //System.out.println("diffs[" + i + "]=" + diffs[i]);
                    //System.out.println("diffs[" + (i+1) + "]=" + diffs[i+1]);
                    atr.addBackground(IAttributedString.COLOR_RED,
                            diffs[i], diffs[i + 1]);
                    atr.addForeground(IAttributedString.COLOR_WHITE,
                            diffs[i], diffs[i + 1]);
                }
            }
        }       
    }
    
    private static int[] EMPTY_INT_ARRAY = new int[0];
    
    private static String computeRaw(char[] text){
        StringBuilder b = null;
        int lastAscii = 0;
        for (int i = 0; i < text.length; i++) {
            if (text[i] >= CONTROL_CODE_START){
                if (b == null){
                    b = new StringBuilder(text.length);
                }
                b.append(text,lastAscii,i-lastAscii);
                lastAscii = i+1;
            }             
        }
        if (b != null){
            b.append(text,lastAscii,text.length-lastAscii);
            return b.toString();
        }
        return new String(text);
    }

    private static int[] getStyles(char[] text) {
        // styles[i+0] - style flags
        // styles[i+1] - start index in string stripped of control characters of i-th region
        // styles[i+2] - end index in string stripped of control characters or i-th region
        int[] styles = null;
        int i = 0;
        int j = 0; // char index
        int pendingStyle = 0;
        int pendingFont = 0;
        for (char c : text) {
            if (c >= CONTROL_CODE_START) {
                boolean isStyle = isStyleCode(c);
                if (isStyle || isFontCode(c)) {
                    if (styles == null) {
                        styles = new int[text.length * 3];
                    }
                    int code = 0;
                    if (isBold(c))
                        code |= STYLE_BOLD;
                    if (isItalic(c))
                        code |= STYLE_ITALIC;
                    if (isUnderline(c))
                        code |= STYLE_UNDERLINE;
                    if (c == CONTROL_CODE_FONT_PROPORTIONAL){
                        code |= STYLE_PROPORTIONAL;
                    }
                    int fontAndStyle = code;
                    if (isStyle){
                        fontAndStyle = code | pendingFont;
                        pendingStyle = code;
                    }
                    else {
                        fontAndStyle = code | pendingStyle;
                        pendingFont = code;
                    }                  
                    if (i > 0) {
                        if (styles[i-2] == j) { // empty item.
                            i -=3;
                        }
                        else
                            styles[i - 1] = j;
                    }
                    styles[i] = fontAndStyle;
                    styles[i + 1] = j;
                    i += 3;
                }
            } else
                j++;
        }
        if (styles != null && j > 0) {
            if (i > 0)
                styles[i - 1] = j;
            int result[] = new int[i];
            System.arraycopy(styles, 0, result, 0, i);
            return result;
        }
        return EMPTY_INT_ARRAY;
    }
    private static boolean isStyleCode(char code) {
        return (code & 0xFFF8) == 0x8030;
    }
    
    private static boolean isFontCode(char code){
        return code == CONTROL_CODE_FONT_PROPORTIONAL || code == CONTROL_CODE_FONT_MONOSPACE;
    }

    private static boolean isBold(char code) {
        return isStyleCode(code) && (code & CONTROL_CODE_BOLD) == CONTROL_CODE_BOLD;
    }

    private static boolean isItalic(char code) {
        return isStyleCode(code) && (code & CONTROL_CODE_ITALIC) == CONTROL_CODE_ITALIC;
    }

    private static boolean isUnderline(char code) {
        return isStyleCode(code) && (code & CONTROL_CODE_UNDERLINE) == CONTROL_CODE_UNDERLINE;
    }
    
    private static final int STYLE_BOLD = 1 << 0;

    private static final int STYLE_ITALIC = 1 << 1;

    private static final int STYLE_UNDERLINE = 1 << 2;
    
    private static final int STYLE_PROPORTIONAL = 1 << 3;



}
