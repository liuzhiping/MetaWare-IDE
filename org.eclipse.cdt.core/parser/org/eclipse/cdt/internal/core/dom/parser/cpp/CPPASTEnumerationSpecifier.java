/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalEnumerationSpecifier;

/**
 * AST node for c++ enumeration specifiers.
 */
public class CPPASTEnumerationSpecifier extends CPPASTBaseDeclSpecifier
		implements IASTInternalEnumerationSpecifier, ICPPASTEnumerationSpecifier {

	private boolean fIsScoped;
	private boolean fIsOpaque;
	private IASTName fName;
	private ICPPASTDeclSpecifier fBaseType;

	private IASTEnumerator[] fItems = null;
	private int fItemPos=-1;

	private boolean fValuesComputed= false;
	private CPPEnumScope fScope;

	public CPPASTEnumerationSpecifier() {
	}

	public CPPASTEnumerationSpecifier(boolean isScoped, IASTName name, ICPPASTDeclSpecifier baseType) {
		fIsScoped= isScoped;
		setName(name);
		setBaseType(baseType);
	}
	
	public CPPASTEnumerationSpecifier copy() {
		CPPASTEnumerationSpecifier copy = new CPPASTEnumerationSpecifier(fIsScoped, 
				fName == null ? null : fName.copy(), 
				fBaseType == null ? null : fBaseType.copy());
		copy.fIsOpaque= fIsOpaque;
		for (IASTEnumerator enumerator : getEnumerators())
			copy.addEnumerator(enumerator == null ? null : enumerator.copy());
		copyBaseDeclSpec(copy);
		return copy;
	}
	
	
	public boolean startValueComputation() {
		if (fValuesComputed)
			return false;
		
		fValuesComputed= true;
		return true;
	}

	public void addEnumerator(IASTEnumerator enumerator) {
        assertNotFrozen();
		if (enumerator != null) {
			enumerator.setParent(this);
			enumerator.setPropertyInParent(ENUMERATOR);
			fItems = (IASTEnumerator[]) ArrayUtil.append( IASTEnumerator.class, fItems, ++fItemPos, enumerator );
		}
	}

	public IASTEnumerator[] getEnumerators() {
		if (fItems == null)
			return IASTEnumerator.EMPTY_ENUMERATOR_ARRAY;
		
		fItems = (IASTEnumerator[]) ArrayUtil.removeNullsAfter(IASTEnumerator.class, fItems, fItemPos);
		return fItems;
	}

	public void setName(IASTName name) {
        assertNotFrozen();
		fName = name;
		if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ENUMERATION_NAME);
		}
	}

	public IASTName getName() {
		return fName;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		if (fName != null && !fName.accept(action))
			return false;
		
		if (fBaseType != null && !fBaseType.accept(action)) {
			return false;
		}
		
		for (IASTEnumerator e : getEnumerators()) {
			if (!e.accept(action))
				return false;
		}
				
		if (action.shouldVisitDeclSpecifiers && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	public int getRoleForName(IASTName n) {
		if (fName == n)
			return isOpaque() ? r_declaration : r_definition;
		return r_unclear;
	}

	public void setIsScoped(boolean isScoped) {
		assertNotFrozen();
		fIsScoped= isScoped;
	}

	public boolean isScoped() {
		return fIsScoped;
	}

	public void setBaseType(ICPPASTDeclSpecifier baseType) {
		assertNotFrozen();
		fBaseType= baseType;
		if (baseType != null) {
			baseType.setParent(this);
			baseType.setPropertyInParent(BASE_TYPE);
		}
	}

	public ICPPASTDeclSpecifier getBaseType() {
		return fBaseType;
	}

	public void setIsOpaque(boolean isOpaque) {
		assertNotFrozen();
		fIsOpaque= isOpaque;
	}

	public boolean isOpaque() {
		return fIsOpaque;
	}

	public ICPPScope getScope() {
		if (isOpaque())
			return null;
		if (fScope == null) {
			fScope= new CPPEnumScope(this);
		}
		return fScope;
	}
}
