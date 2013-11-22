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
package com.arc.symbols.types;



public interface IStructBuilder {
    /**
     * Add a field.
     * For unions, the offset will be zero.
     * @param name name of the field.
     * @param type type of the field.
     * @param offset byte offset within parent struct.
     */
    void addField(String name, IType type, int offset);
    
    /**
     * Add a bitlength field.
     * For unions, the offset will be zero.
     * @param name name of the field.
     * @param type type of the field.
     * @param byteOffset byte offset within parent struct.
     * @param bitOffset bit offset within the byte, short, or word.
     * @param bitLength bit length.
     */
    void addBitField(String name, ITypeInteger type, int byteOffset, int bitOffset, int bitLength);
    
    /**
     * Called after all is constucted to complete the build.
     * @return return the type being constructed.
     */
    ITypeClass completeBuild();
}
