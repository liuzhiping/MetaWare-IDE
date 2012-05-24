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


public interface IFactory {
    ITypeInteger makeIntegerType(String name, int size, boolean isSigned);
    
    ITypeFloat makeFloatType(String name, int size);
    
    ITypePointer makePointerType(String name, IType base, int size);
    
    ITypeRef makeRefType(String name, IType base, int size);
    
    ITypeArray makeArrayType(String name, IType elementType, int dimension);
    
    IClassBuilder makeClassBuilder(String name);
    
    IStructBuilder makeStructBuilder(String tagName, boolean isUnion);
}
