/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CASTCaseStatement extends ASTNode implements IASTCaseStatement, IASTAmbiguityParent {

    private IASTExpression expression;


    public CASTCaseStatement() {
	}

	public CASTCaseStatement(IASTExpression expression) {
		setExpression(expression);
	}
	
	public CASTCaseStatement copy() {
		CASTCaseStatement copy = new CASTCaseStatement(expression == null ? null : expression.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public IASTExpression getExpression() {
        return expression;
    }

    public void setExpression(IASTExpression expression) {
        assertNotFrozen();
        this.expression = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(EXPRESSION);
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
        if( expression != null ) if( !expression.accept( action ) ) return false;
        if( action.shouldVisitStatements ){
        	switch( action.leave( this ) ){
        		case ASTVisitor.PROCESS_ABORT : return false;
        		case ASTVisitor.PROCESS_SKIP  : return true;
        		default : break;
        	}
        }      
        
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == expression )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            expression = (IASTExpression) other;
        }
    }
}
