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
package com.arc.dwarf2.model;

import java.util.List;

/**
 * A representation of a single Dwarf 2 line table.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ILineTableReader {
    /**
     * Return the offset of where this table starts in the ".dwarf_line" section. Typically used for
     * display purposes.
     * @return the offset of where this table starts in the ".dwarf_line" section.
     */
    public int getOffset();
    
    /**
     * 
     * Return the length of this table in bytes. Typically used for display purposes.
     * @return   the length of this table in bytes.
     */
    public int getLength();

    /**
     * Return the version number of this table.
     * @return the version number of this table.
     */
    public int getVersion();
    
    /**
     * Return an immutable list of include files.
     * @return an immutable list of include files.
     */
    public List<String> getIncludes();
    
    /**
     * Return the minimum instruction length.
     * @return the minimum instruction length.
     */
    public int getInstructionLength();
    
    /**
     * The definition of a file.
     */
    public static class FileRef {
        public String name;
        public int includeIndex;
        public int modTime;
        public int size;
    }
    
    /**
     * Return an immutable list of file definitions.
     *
     * @return an immutable list of file definitions.
     */
    public List<FileRef> getFiles();
    
    public boolean getDefaultIsStatement();
    
    /**
     * Return the line base.
     * @return the line base.
     */
    public int getLineBase();
    
    public int getLineRange();
    
    public int getOpcodeBase();
    
    /**
     * Return the number of operands for each of the standard opcodes.
     * @return the number of operands for each of the standard opcodes.
     */
    public byte[] getStandardOpcodeOperandCounts();
    
    interface IStatementReceiver {
        /**
         * Indicate that subsequent source line references will pertain to 
         * the given file.
         * @param includePath the include path if the fileName is not absolute.
         * @param fileName the fileName. If not absolute, then it is relative to the includePath.
         * @param modTime the timestamp on the source file during compilation.
         * @param size the size of the source file during compilation, if known.
         */
        public void setFile(String includePath, String fileName, long modTime, int size);
        
        /**
         * Recognize a source line definition that starts at the given PC.
         * 
         * @param pc the PC at which the statement starts.
         * @param sourceLine the source line at which a statement starts.
         * @param column the column in the source line, or 0, if the column isn't known.
         */
        public void addLine(int pc, int sourceLine, int column);
        
        /**
         * Indicate that the next {@link #addLine} call applies to the
         * start of a basic block. If this is not called prior to an {@link #addLine} call, then
         * the new line is assumed to not be on a basic block boundary.
         */
        public void setBasicBlock();
        
        /**
         * Set whether or not subsequence calls to {@link #addLine} apply to statements
         * on which breakpoints can be set.
         * @param v true if breakpoints can be set in lines defined by subsequent calls to {@link #addLine}.
         */
        public void setIsStatement(boolean v);
        
        /**
         * Indicate that from now on, subsequent calls to {@link #addLine} are no longer in the
         * functions prolog.
         */
        public void setPrologEnd();
        
        /**
         * Indicate that from now on, subsequent calls to {@link #addLine} are in the epilog of 
         * the function.
         */
        public void setEpilogBegin();
        
    }
    /**
     * Invoke a callback for each line table entry. A complete table can be constructed
     * by the client.
     * @param receiver callback to be invoked.
     */
    public void eachStatement(IStatementReceiver receiver);
    
}
