/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Yuan Zhang / Beth Tibbitts (IBM Research)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CASTReturnStatement extends ASTNode implements
        IASTReturnStatement, IASTAmbiguityParent {

    private IASTExpression retValue;

    public CASTReturnStatement() {
	}

	public CASTReturnStatement(IASTExpression retValue) {
		setReturnValue(retValue);
	}
	
	public CASTReturnStatement copy() {
		CASTReturnStatement copy = new CASTReturnStatement(retValue == null ? null : retValue.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public IASTExpression getReturnValue() {
        return retValue;
    }

    public void setReturnValue(IASTExpression returnValue) {
        assertNotFrozen();
        retValue = returnValue;
        if (returnValue != null) {
			returnValue.setParent(this);
			returnValue.setPropertyInParent(RETURNVALUE);
		}
    }

    public IASTInitializerClause getReturnArgument() {
    	return getReturnValue();
	}

	public void setReturnArgument(IASTInitializerClause returnValue) {
		if (returnValue instanceof IASTExpression) {
			setReturnValue((IASTExpression) returnValue);
		} else {
			setReturnValue(null);
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
        if( retValue != null ) if( !retValue.accept( action ) ) return false;
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
        if( child == retValue )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            retValue  = (IASTExpression) other;
        }
    }
}