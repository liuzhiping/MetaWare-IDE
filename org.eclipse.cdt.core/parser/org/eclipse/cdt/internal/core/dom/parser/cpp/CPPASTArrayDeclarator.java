/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Array declarator for c++.
 */
public class CPPASTArrayDeclarator extends CPPASTDeclarator implements ICPPASTArrayDeclarator {
    
    private IASTArrayModifier [] arrayMods = null;
    private int arrayModsPos=-1;

    
    public CPPASTArrayDeclarator(IASTName name, IASTInitializer initializer) {
		super(name, initializer);
	}

	public CPPASTArrayDeclarator(IASTName name) {
		super(name);
	}
	
	
    public CPPASTArrayDeclarator() {
	}

    @Override
	public CPPASTArrayDeclarator copy() {
    	CPPASTArrayDeclarator copy = new CPPASTArrayDeclarator();
		copyBaseDeclarator(copy);
		for(IASTArrayModifier modifier : getArrayModifiers())
			copy.addArrayModifier(modifier == null ? null : modifier.copy());
		return copy;
	}
    
    
	public IASTArrayModifier[] getArrayModifiers() {
        if( arrayMods == null ) return IASTArrayModifier.EMPTY_ARRAY;
        arrayMods = (IASTArrayModifier[]) ArrayUtil.removeNullsAfter( IASTArrayModifier.class, arrayMods, arrayModsPos );
        return arrayMods;
    }

    public void addArrayModifier(IASTArrayModifier arrayModifier) {
        assertNotFrozen();
    	if (arrayModifier != null) {
    		arrayMods = (IASTArrayModifier[]) ArrayUtil.append( IASTArrayModifier.class, arrayMods, ++arrayModsPos, arrayModifier );
    		arrayModifier.setParent(this);
			arrayModifier.setPropertyInParent(ARRAY_MODIFIER);
    	}
    }
    
    @Override
	protected boolean postAccept(ASTVisitor action) {
		IASTArrayModifier[] mods = getArrayModifiers();
		for (int i = 0; i < mods.length; i++) {
			if (!mods[i].accept(action))
				return false;
		}
		return super.postAccept(action);
	}
}
