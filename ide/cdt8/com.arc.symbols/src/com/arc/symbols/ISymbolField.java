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
package com.arc.symbols;


public interface ISymbolField extends ISymbolMember {
    /**
     * Return the address of the field if it is static.
     * @return the address of the field if it is static.
     * @throws IllegalStateException if the field is not static.
     */
    long getAddress() throws IllegalArgumentException;
    
    /**
     * The number of bytes that this field occupies.
     * @return the number of bytes that this field occupies.
     */
    int getSize(); 
    
    /**
     * Return the byte offset within the parent aggregate.
     * @return the byte offset within the parent aggregate.
     * @throws IllegalStateException if the field is static.
     */
    int getOffset() throws IllegalArgumentException;
    
    /**
     * Return bit length if this is a bit field; otherwise, returns 0.
     * @return bit length if this is a bit field; otherwise, returns 0.
     */
    int getBitLength();
    
    /**
     * Return the bit offset, if this field is a bit field. Otherwise, returns 0.
     * @return the bit offset if this is a bit field; otherwise 0.
     */
    int getBitOffset();
}
