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

import java.io.IOException;

/**
 * The minimal interface by which the Dwarf reader accesses section data.
 * <P>
 * An instance of this interface is provided by the client and is retrieved
 * by calling the {@link ISectionAccess#getSection} method that the
 * client provides.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ISectionReader {
    /**
     * Return the name of this section (for benefit of error diagnostics).
     * @return the name of this section.
     */
    String getName();
    /**
     * Return the size of the associated section data in bytes.
     * @return the size of the associated section data in bytes.
     */
    int getLength();
    /**
     * Return the byte at the given offset within the associated section. The byte
     * will be read as if it were unsigned.
     * @param the byte offset of the byte to be retrieved.
     * @return the unsigned byte at the given offset within the associated section.
     * @exception IOException if !(0 &lt;= offset &lt; {@link #getLength()}).
     * 
     */
    int getByteAt(int offset) throws IOException;
    
    /**
     * If we're actually reading from a composite of sections (e.g., ".debug_info$FOO", ".debug_info$BAR"),
     * then return the actual section name at the given offset. Used for display purposes. If this isn't
     * a composite, then this method should simply call {@link #getName}.
     * @param offset the composite offset.
     * @return the actual debug section being written (e.g., ".debug_info$FOO")
     */
    String getActualName(int offset);
    
    /**
     * Return the actual section offset corresponding to "offset". If we're not reading a composite of
     * sections, it merely returns the argument.
     * @param offset
     * @return the actual section offset corresponding to the composite offset.
     */
    int getSectionOffset(int offset);
    
//    /**
//     * Return the unsigned halfword value at the given offset, taking into account
//     * endianness of the associated section data. 
//     * @param offset the byte offset of the halfword to be retrieved.
//     * @return the unsigned halfword value at the given offset.
//     * @exception IOException if !(0 &lt;= offset &lt; {@link #getLength()}-1).
//     */
//    int getHalfwordAt(int offset) throws IOException;
//    
//    /**
//     * Return the word value at the given offset, taking into account
//     * endianness of the associated section data. 
//     * @param offset the byte offset of the word to be retrieved.
//     * @return the signed word value at the given offset.
//     * @exception IOException if !(0 &lt;= offset &lt; {@link #getLength()}-3).
//     */
//    int getWordAt(int offset) throws IOException;

}
