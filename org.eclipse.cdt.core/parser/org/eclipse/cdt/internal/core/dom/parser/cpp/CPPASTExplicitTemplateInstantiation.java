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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Models explicit instantiations.
 */
public class CPPASTExplicitTemplateInstantiation extends ASTNode implements
        ICPPASTExplicitTemplateInstantiation, IASTAmbiguityParent {

    private IASTDeclaration declaration;
    private int modifier;

    
    public CPPASTExplicitTemplateInstantiation() {
	}

	public CPPASTExplicitTemplateInstantiation(IASTDeclaration declaration) {
		setDeclaration(declaration);
	}

	public CPPASTExplicitTemplateInstantiation copy() {
		CPPASTExplicitTemplateInstantiation copy = new CPPASTExplicitTemplateInstantiation();
		copy.setDeclaration(declaration == null ? null : declaration.copy());
		copy.setModifier(modifier);
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTDeclaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(IASTDeclaration declaration) {
        assertNotFrozen();
        this.declaration = declaration;
        if (declaration != null) {
			declaration.setParent(this);
			declaration.setPropertyInParent(OWNED_DECLARATION);
		}
    }

    
    public int getModifier() {
		return modifier;
	}

	public void setModifier(int mod) {
		assertNotFrozen();
		modifier= mod;
	}

	@Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( declaration != null ) if( !declaration.accept( action ) ) return false;
        
        if( action.shouldVisitDeclarations ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }


    public void replace(IASTNode child, IASTNode other) {
        if( declaration == child )
        {
            other.setParent( child.getParent() );
            other.setPropertyInParent( child.getPropertyInParent() );
            declaration = (IASTDeclaration) other;
        }
    }
}
