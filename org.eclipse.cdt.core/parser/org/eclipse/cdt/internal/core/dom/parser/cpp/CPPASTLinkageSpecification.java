/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Extern "C" construct.
 */
public class CPPASTLinkageSpecification extends ASTNode implements
        ICPPASTLinkageSpecification, IASTAmbiguityParent {

    private String fLiteral;
	private IASTDeclaration[] fAllDeclarations;
	private IASTDeclaration[] fActiveDeclarations;
    private int fLastDeclaration=-1;
    
    public CPPASTLinkageSpecification() {
	}

	public CPPASTLinkageSpecification(String literal) {
		this.fLiteral = literal;
	}
	
	public CPPASTLinkageSpecification copy() {
		CPPASTLinkageSpecification copy = new CPPASTLinkageSpecification(fLiteral);
		for(IASTDeclaration declaration : getDeclarations())
			copy.addDeclaration(declaration == null ? null : declaration.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	

	public String getLiteral() {
        return fLiteral;
    }

    public void setLiteral(String value) {
        assertNotFrozen();
        this.fLiteral = value;
    }

	public final void addDeclaration(IASTDeclaration decl) {
		if (decl != null) {
			decl.setParent(this);
			decl.setPropertyInParent(OWNED_DECLARATION);
			fAllDeclarations = (IASTDeclaration[]) ArrayUtil.append( IASTDeclaration.class, fAllDeclarations, ++fLastDeclaration, decl);
			fActiveDeclarations= null;
		}
	}

	public final IASTDeclaration[] getDeclarations() {
		IASTDeclaration[] active= fActiveDeclarations;
		if (active == null) {
			active = ASTQueries.extractActiveDeclarations(fAllDeclarations, fLastDeclaration+1);
			fActiveDeclarations= active;
		}
		return active;
	}

	public final IASTDeclaration[] getDeclarations(boolean includeInactive) {
		if (includeInactive) {
			fAllDeclarations= (IASTDeclaration[]) ArrayUtil.removeNullsAfter(IASTDeclaration.class, fAllDeclarations, fLastDeclaration);
			return fAllDeclarations;
		}
		return getDeclarations();
	}


    @Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
		IASTDeclaration[] decls = getDeclarations(action.includeInactiveNodes);
		for (IASTDeclaration decl : decls) {
			if (!decl.accept(action)) return false;
		}

		if (action.shouldVisitDeclarations && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
        
        return true;
    }

    public final void replace(IASTNode child, IASTNode other) {
		assert child.isActive() == other.isActive();
		for (int i = 0; i <= fLastDeclaration; ++i) {
			if (fAllDeclarations[i] == child) {
				other.setParent(child.getParent());
				other.setPropertyInParent(child.getPropertyInParent());
				fAllDeclarations[i] = (IASTDeclaration) other;
				fActiveDeclarations= null;
				return;
			}
		}
	}
}
