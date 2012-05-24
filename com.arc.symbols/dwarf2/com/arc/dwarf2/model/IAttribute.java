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

import com.arc.dwarf2.Dwarf2;


/**
 * An attribute of a tag ({@link ITag} instance).
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IAttribute {
	/**
	 * Return the attribute ID (e.g., DW_AT_name, DW_AT_low_pc, etc.).
	 * @return the attribute ID.
	 */
	public int getID();
    
    /**
     * Return the form ID (e.g., DW_FORM_string, etc.).
     * @return Return the form ID.
     */
    public int getFormID();
	
	/**
	 * Return the attribute format.
	 * @return the attribute format.
	 */
	public Dwarf2.AttributeFormat getFormat();
	
	/**
	 * Return the value if this attribute is an integer (for which
	 * {@link #getFormat()} will return {@link Dwarf2.AttributeFormat#INT_FORM}).
	 * @return the value if this attribute is an integer.
	 * @throws IllegalStateException if this attribute is not of an integer format.
	 */
	public long getIntValue() throws IllegalStateException;
	
	/**
	 * Return the value if this attribute is a string (for which
	 * {@link #getFormat()} will return {@link Dwarf2.AttributeFormat#STRING_FORM}).
	 * @return the value if this attribute is an string.
	 * @throws IllegalStateException if this attribute is not of a string format.
	 */
	public String getStringValue()throws IllegalStateException;
	
	/**
	 * Return the value if this attribute is a block (for which
	 * {@link #getFormat()} will return {@link Dwarf2.AttributeFormat#BLOCK_FORM}).
	 * @return the value if this attribute is a block.
	 * @throws IllegalStateException if this attribute is not of a block format.
	 */
	public byte[] getBlock()throws IllegalStateException;
    
    /**
     * Return the value as an object.
     * @return the value as an object.
     */
    public Object getValue();

}
