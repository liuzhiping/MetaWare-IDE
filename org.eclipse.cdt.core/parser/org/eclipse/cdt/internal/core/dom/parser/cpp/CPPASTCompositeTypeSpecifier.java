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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * c++ specific composite type specifier
 */
public class CPPASTCompositeTypeSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTCompositeTypeSpecifier, IASTAmbiguityParent {

    private int fKey;
    private IASTName fName;
    private ICPPClassScope fScope;
	private IASTDeclaration[] fAllDeclarations;
	private IASTDeclaration[] fActiveDeclarations;
    private int fDeclarationsPos=-1;
	private ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[] baseSpecs = null;
	private int baseSpecsPos = -1;


    public CPPASTCompositeTypeSpecifier() {
	}

	public CPPASTCompositeTypeSpecifier(int k, IASTName n) {
		this.fKey = k;
		setName(n);
	}

	public CPPASTCompositeTypeSpecifier copy() {
		CPPASTCompositeTypeSpecifier copy = new CPPASTCompositeTypeSpecifier(fKey, fName == null ? null : fName.copy());
		copyBaseDeclSpec(copy);
		for(IASTDeclaration member : getMembers())
			copy.addMemberDeclaration(member == null ? null : member.copy());
		for(ICPPASTBaseSpecifier baseSpecifier : getBaseSpecifiers())
			copy.addBaseSpecifier(baseSpecifier == null ? null : baseSpecifier.copy());
		return copy;
	}
	
    public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
        if( baseSpecs == null ) return ICPPASTBaseSpecifier.EMPTY_BASESPECIFIER_ARRAY;
        baseSpecs = (ICPPASTBaseSpecifier[]) ArrayUtil.removeNullsAfter( ICPPASTBaseSpecifier.class, baseSpecs, baseSpecsPos );
        return baseSpecs;
    }

    public void addBaseSpecifier(ICPPASTBaseSpecifier baseSpec) {
        assertNotFrozen();
    	if (baseSpec != null) {
    		baseSpec.setParent(this);
			baseSpec.setPropertyInParent(BASE_SPECIFIER);
    		baseSpecs = (ICPPASTBaseSpecifier[]) ArrayUtil.append( ICPPASTBaseSpecifier.class, baseSpecs, ++baseSpecsPos, baseSpec );
    	}
    }

    public int getKey() {
        return fKey;
    }

    public void setKey(int key) {
        assertNotFrozen();
        fKey = key;
    }

    public IASTName getName() {
        return fName;
    }

    public void setName(IASTName name) {
        assertNotFrozen();
        this.fName = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(TYPE_NAME);
		}
    }

	public IASTDeclaration[] getMembers() {
		IASTDeclaration[] active= fActiveDeclarations;
		if (active == null) {
			active = ASTQueries.extractActiveDeclarations(fAllDeclarations, fDeclarationsPos+1);
			fActiveDeclarations= active;
		}
		return active;
	}

	public final IASTDeclaration[] getDeclarations(boolean includeInactive) {
		if (includeInactive) {
			fAllDeclarations= (IASTDeclaration[]) ArrayUtil.removeNullsAfter(IASTDeclaration.class, fAllDeclarations, fDeclarationsPos);
			return fAllDeclarations;
		}
		return getMembers();
	}

	public void addMemberDeclaration(IASTDeclaration decl) {
		if (decl == null)
			return;
		
		// ignore inactive visibility labels
		if (decl instanceof ICPPASTVisibilityLabel && !decl.isActive())
			return;

		assertNotFrozen();
		decl.setParent(this);
		decl.setPropertyInParent(decl instanceof ICPPASTVisibilityLabel ? VISIBILITY_LABEL : MEMBER_DECLARATION);
		fAllDeclarations = (IASTDeclaration[]) ArrayUtil.append(IASTDeclaration.class, fAllDeclarations,
				++fDeclarationsPos, decl);
		fActiveDeclarations= null;
	}
    
    public final void addDeclaration(IASTDeclaration decl) {
    	addMemberDeclaration(decl);
    }

	public ICPPClassScope getScope() {
		if (fScope == null)
			fScope = new CPPClassScope(this);
		return fScope;
	}
    
    public void setScope(ICPPClassScope scope) {
        this.fScope = scope;
    }

    @Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclSpecifiers) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT: return false;
			case ASTVisitor.PROCESS_SKIP: return true;
			default: break;
			}
		}
		
		if (fName != null && !fName.accept(action))
			return false;

		ICPPASTBaseSpecifier[] bases = getBaseSpecifiers();
		for (int i = 0; i < bases.length; i++) {
			if (!bases[i].accept(action)) return false;
		}

		IASTDeclaration[] decls = getDeclarations(action.includeInactiveNodes);
		for (int i = 0; i < decls.length; i++) {
			if (!decls[i].accept(action)) return false;
		}
		
		if (action.shouldVisitDeclSpecifiers && action.leave(this) == ASTVisitor.PROCESS_ABORT) 
			return false;

		return true;
	}
	
	public int getRoleForName(IASTName name) {
		if( name == this.fName )
			return r_definition;
		return r_unclear;
	}

	public void replace(IASTNode child, IASTNode other) {
		assert child.isActive() == other.isActive();
		for (int i = 0; i <= fDeclarationsPos; ++i) {
			if (fAllDeclarations[i] == child) {
				other.setParent(child.getParent());
				other.setPropertyInParent(child.getPropertyInParent());
				fAllDeclarations[i] = (IASTDeclaration) other;
				fActiveDeclarations= null;
			}
		}
	}
}
