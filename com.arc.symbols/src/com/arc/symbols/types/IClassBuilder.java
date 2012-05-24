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

import java.util.List;

import com.arc.symbols.Visibility;


public interface IClassBuilder {
    void addBaseClass(ITypeClass klass, boolean isVirtual, Visibility visibility);
    
    /**
     * Create a builder for a nested class.
     * @param name name of nested class.
     * @return builder for nested class.
     */
    IClassBuilder makeClassBuilder(String name, Visibility visibility);
    
    void addMethod(String name, List<IType>parameters, IType returnType, Visibility visibility, 
        boolean isStatic, boolean isVirtual, boolean isAbstract);
    /**
     * 
     * Define an instance or static member to the class.
     * @param name name of the member.
     * @param type the type of the member.
     * @param visibility its visibility.
     * @param isStatic whether or not it is static.
     * @param addressOrOffset its absolute address if static, or byte offset if not static.
     */
    void addField(String name, IType type, Visibility visibility, boolean isStatic, long addressOrOffset);
    
    /**
     * Add a bitlength field.
     * For unions, the offset will be zero.
     * @param name name of the field.
     * @param type type of the field.
     * @param byteOffset byte offset within parent struct.
     * @param bitOffset bit offset within the byte, short, or word.
     * @param bitLength bit length.
     */
    void addBitField(String name, ITypeInteger type, Visibility visibility, int byteOffset, int bitOffset, int bitLength);
    
    /**
     * Called after all is constucted to complete the build.
     * @return return the type being constructed.
     */
    ITypeClass completeBuild();
}
