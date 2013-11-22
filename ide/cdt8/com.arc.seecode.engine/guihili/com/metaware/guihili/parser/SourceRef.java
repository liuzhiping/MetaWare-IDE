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
package com.metaware.guihili.parser;

/**
 * A source code reference. Typically, it is a simple pathname. However, a
 * source file that is being read from an "include" directive will also contain
 * the source reference from which the include occurred.
 * 
 * @author David Pickens
 * @version April 26, 2002
 */
public class SourceRef {
    public SourceRef(String name) {
        mName = name;
    }

    public SourceRef(SourceRef parent, int line, String name) {
        mParent = parent;
        mParentLine = line;
        mName = name;
    }

    /**
     * Return the name of associated source.
     */
    public String getName() {
        return mName;
    }

    /**
     * If this is an "include" file, return the parent source.
     */
    public SourceRef getParentSource() {
        return mParent;
    }

    /**
     * If this is an "include" file, return the line number within the parent
     * that the include was located.
     */
    public int getParentLine() {
        return mParentLine;
    }

    /**
     * This method is called once at the start of the parsing. It is expected to
     * initial an object for which all others will be children.
     */
    public static SourceRef makeFileName(String filename) {
        return new SourceRef(filename);
    }

    public static SourceRef makeInclude(SourceRef parent, int line,
            String includeName) {
        return new SourceRef(parent, line, includeName);
    }

    public boolean isInclude() {
        return getParentSource() != null;
    }

    @Override
    public String toString() {
        if (isInclude())
            return getName() + "(@" + getParentSource() + ",line "
                    + getParentLine() + ")";
        return getName();
    }

    private String mName;

    private SourceRef mParent;

    private int mParentLine;
}
