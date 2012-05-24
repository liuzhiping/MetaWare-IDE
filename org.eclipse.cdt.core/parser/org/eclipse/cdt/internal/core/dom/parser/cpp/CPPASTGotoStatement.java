/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CPPASTGotoStatement extends ASTNode implements IASTGotoStatement {

	private IASTName name;

	
    public CPPASTGotoStatement() {
	}

	public CPPASTGotoStatement(IASTName name) {
		setName(name);
	}

	public CPPASTGotoStatement copy() {
		CPPASTGotoStatement copy = new CPPASTGotoStatement(name == null ? null : name.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTName getName() {
        return this.name;
    }

    public void setName(IASTName name) {
        assertNotFrozen();
       this.name = name;
       if (name != null) {
    	   name.setParent(this);
    	   name.setPropertyInParent(NAME);
       }
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( name != null ) if( !name.accept( action ) ) return false;

        if( action.shouldVisitStatements ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	public int getRoleForName(IASTName n) {
		if( name == n ) return r_reference;
		return r_unclear;
	}
}
