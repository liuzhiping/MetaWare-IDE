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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * Switch statement in c++.
 */
public class CPPASTSwitchStatement extends ASTNode implements
        ICPPASTSwitchStatement, IASTAmbiguityParent {

	private IScope scope;
    private IASTExpression controllerExpression;
    private IASTDeclaration controllerDeclaration;
    private IASTStatement body;

    
    public CPPASTSwitchStatement() {
	}

	public CPPASTSwitchStatement(IASTDeclaration controller, IASTStatement body) {
		setControllerDeclaration(controller);
		setBody(body);
	}
    
    public CPPASTSwitchStatement(IASTExpression controller, IASTStatement body) {
		setControllerExpression(controller);
		setBody(body);
	}
    
    public CPPASTSwitchStatement copy() {
		CPPASTSwitchStatement copy = new CPPASTSwitchStatement();
		copy.setControllerDeclaration(controllerDeclaration == null ? null : controllerDeclaration.copy());
		copy.setControllerExpression(controllerExpression == null ? null : controllerExpression.copy());
		copy.setBody(body == null ? null : body.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public IASTExpression getControllerExpression() {
        return controllerExpression;
    }

    public void setControllerExpression(IASTExpression controller) {
        assertNotFrozen();
        this.controllerExpression = controller;
        if (controller != null) {
			controller.setParent(this);
			controller.setPropertyInParent(CONTROLLER_EXP);
			controllerDeclaration= null;
		}
    }

    public IASTStatement getBody() {
        return body;
    }
    
    public void setBody(IASTStatement body) {
        assertNotFrozen();
        this.body = body;
        if (body != null) {
			body.setParent(this);
			body.setPropertyInParent(BODY);
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
        if( controllerExpression != null ) if( !controllerExpression.accept( action ) ) return false;
        if( controllerDeclaration != null ) if( !controllerDeclaration.accept( action ) ) return false;
        if( body != null ) if( !body.accept( action ) ) return false;
        
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
		if (body == child) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			body = (IASTStatement) other;
		} else if (controllerDeclaration == child || controllerExpression == child) {
			if (other instanceof IASTExpression) {
				setControllerExpression((IASTExpression) other);
			} else if (other instanceof IASTDeclaration) {
				setControllerDeclaration((IASTDeclaration) other);
			}
		}
	}

    public IASTDeclaration getControllerDeclaration() {
        return controllerDeclaration;
    }

    public void setControllerDeclaration(IASTDeclaration d) {
        assertNotFrozen();
        controllerDeclaration = d;
        if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(CONTROLLER_DECLARATION);
			controllerExpression= null;
		}
    }

	public IScope getScope() {
		if( scope == null )
            scope = new CPPBlockScope( this );
        return scope;	
    }

}
