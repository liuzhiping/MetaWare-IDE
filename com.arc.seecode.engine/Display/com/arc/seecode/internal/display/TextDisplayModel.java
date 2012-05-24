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

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Pickens
 */
class TextDisplayModel {
    private Line[] mLines = new Line[0];
    private Map<Integer,Line[]> mProfilingMap = null;

    TextDisplayModel() {
    }

    /**
     * Set of update a line of text int he model
     * 
     * @param lineNumber
     * @param lineText
     */
    public void setLine(int lineNumber, String lineText) {
        if (lineNumber >= 0 && lineNumber < mLines.length) {
            mLines[lineNumber].update(lineText);
        }
    }

    /**
     * Return the lines of the model.
     * 
     * @return the lines of the model.
     */
    public Line[] getLines() {
        return mLines;
    }
    
    public int getLongestLineWidth(){
        int max = 0;
        for (int i = 0; i < mLines.length; i++){
            if (mLines[i] != null){
                max = Math.max(max,mLines[i].getLength());
            }
        }
        return max;
    }

    public void setSize(int lineCount) {
        if (lineCount != mLines.length) {
            Line lines[] = new Line[lineCount];
            int moveCount = Math.min(lineCount, mLines.length);
            System.arraycopy(mLines, 0, lines, 0, moveCount);
            for (int i = moveCount; i < lineCount; i++) {
                lines[i] = new Line();
            }
            mLines = lines;
        }
    }

    public void clearAll() {
        for (int i = 0; i < mLines.length; i++) {
            mLines[i].update("");
        }
    }

    public void clearLines(int lineNumber, int cnt) {
        cnt = Math.min(cnt, mLines.length - lineNumber);
        for (int i = 0; i < cnt; i++) {
            mLines[i + lineNumber].update("");
        }
    }

    /**
     * Set or reset the highlight of a line at a given index.
     * 
     * @param ix
     *            index of line to modify
     * @param engineHighlightCode
     *            highlight to set or reset See {@link Line Line}for list of
     *            valid highlights.
     */
    public void setHighlight(int ix, int engineHighlightCode) {
        if (ix >= 0 && ix < mLines.length) {
            mLines[ix].updateHighlight(Line.toHighlight(engineHighlightCode));
        }
    }

    /**
     * Remove highlights
     * 
     * @param engineHighlightCode
     *            highlight code from engine to be removed.
     */
    public void removeHighlights(int engineHighlightCode) {
        //We synchronize because this method is invoked
        // from the command-queue thread and the
        // AWT thread may be reducing the number of lines
        // underneath us as the display shrinks.
        int h = Line.toHighlight(engineHighlightCode);
        synchronized (mLines) {
            for (int ix = 0; ix < mLines.length; ix++) {
                mLines[ix].clearHighlights(h);
            }
        }
    }
    
    /**
     * Scroll vertically by the given amount.
     * <P>
     * If <code>lineCount</code> is >= 0, scroll
     * forwards; else scroll backwards.
     * 
     * @param lineCount the amount to scroll (<0 means scroll
     * backwards).
     * @param displaySize the number of lines actually
     * being seen in the scroll window.
     */
    public void scroll(int lineCount, int displaySize){
        int keepCount = displaySize - Math.abs(lineCount);

        if (keepCount <=0) {
            // nothing to keep
            clearAll();
            return;
            }

        if (lineCount > 0) {
            // scroll forward
            for (int i = 0; i < keepCount; i++) {
                int to = i;
                int from = to + lineCount;
                moveLine(from, to);
                }
            synchronized(mLines){
                // race condition could have caused
                // line count to be less then displaySize
                int cnt = Math.min(displaySize,mLines.length);
                for (int j = keepCount; j < cnt; j++) {
                    mLines[j] =  new Line();
                    }
                }
            }
        else if (lineCount < 0) {
            // scroll backward
            int last_line = displaySize - 1;
            for (int i = 0; i < keepCount; i++) {
                int to = last_line - i;
                int from = to + lineCount;
                moveLine(from, to);
                }
            synchronized(mLines){
                int cnt = Math.min(Math.abs(lineCount),mLines.length);
                for (int j = 0; j < cnt; j++) {
                    mLines[j] =  new Line();
                    }
                }
            } 
    }
    private void moveLine(int from, int to) {
        synchronized(mLines){
            int size = mLines.length;
            if (to < size && from < size) {
                mLines[to] = mLines[from];
                }
            }
        }
    
    public void setColumnData(int column, String lineContent[]){
        if (mProfilingMap == null){
            mProfilingMap = new HashMap<Integer,Line[]>();
        }
        Line lines[] = new Line[lineContent.length];
        for (int i = 0; i < lines.length; i++){
            lines[i] = new Line(columnizeInteger(lineContent[i]));
        }
        mProfilingMap.put(new Integer(column),lines);
    }
    
    public Line[] getColumnData(int column){
        if (mProfilingMap != null){
            return mProfilingMap.get(new Integer(column));
        }
        return null;
    }

    private static String columnizeInteger (String v) {
        if (v.length() > 0 && Character.isDigit(v.charAt(0))) {
            try {
                long value = Long.parseLong(v);
                // If the data is an integer > 6 digits, reformat
                // so it fits in 6 digits. E.g.,
                // 123,456 -> "123456"
                // 1,234,567 -> "1234 K"
                // 1,234,567,890 -> "1234 M"
                if (value > 999999999L) {
                    v = (value + 500000L) / 1000000L + " M";
                }
                else if (value > 999999) {
                    v = (value + 500L) / 1000L + " K";
                }
            }
            catch (NumberFormatException e) {
                // Bad integer; no adjustment
            }
        }
        int padding = 7 - v.length();
        if (padding > 0){
            v = "           ".substring(0,padding) + v;
        }
        return v;
    }
}
