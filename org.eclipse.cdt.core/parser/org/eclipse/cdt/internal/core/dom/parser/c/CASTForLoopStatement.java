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
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForLoopStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * 
 * Representation of a MetaWare iterator forloop body.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CASTForLoopStatement extends ASTNode implements IASTForLoopStatement, IASTAmbiguityParent {

    private IScope scope = null;
    
    private IASTExpression iterator;
    private List<IASTDeclaration> vars = new ArrayList<IASTDeclaration>(3);
    private IASTStatement body;

    public IASTExpression getIteratorExpression() {
        return iterator;
    }

    public void setIteratorExpression(IASTExpression iterator) {
        this.iterator = iterator;
        iterator.setParent(this);
        iterator.setPropertyInParent(ITERATOR);
    }

    public IASTDeclaration[] getControlVariables() {
        return vars.toArray(new IASTDeclaration[vars.size()]);
    }

    public void addControlVariable(IASTDeclaration var) {
        this.vars.add(var);
        var.setParent(this);
        var.setPropertyInParent(VAR);
    }

    public IASTStatement getBody() {
        return body;
    }

    public void setBody(IASTStatement statement) {
        body = statement;
        statement.setParent(this);
        statement.setPropertyInParent(BODY);
    }

    public IScope getScope() {
    	 if( scope == null )
             scope = new CScope( this , EScopeKind.eLocal);
         return scope;
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
        for (IASTDeclaration var: vars){
            if (!var.accept( action ) ) return false;
        }
        if( iterator != null ) if( !iterator.accept( action ) ) return false;
        if( body != null ) if( !body.accept( action ) ) return false;
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( body == child )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            body = (IASTStatement) other;
        }
        for (IASTDeclaration var : vars) {
            if (child == var) {
                other.setPropertyInParent(child.getPropertyInParent());
                other.setParent(child.getParent());
                var = (IASTDeclaration) other;
            }
        }
        if( child == iterator)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            iterator = (IASTExpression) other;
        }
    }

	public IASTStatement copy() {
		CASTForLoopStatement s = new CASTForLoopStatement();
		copyForLoopStatement(s);
		return s;
	}
	protected void copyForLoopStatement(CASTForLoopStatement copy) {
		copy.setIteratorExpression(iterator == null ? null : iterator.copy());
		for (IASTDeclaration v: getControlVariables()){
			copy.addControlVariable(v.copy());
		}
		copy.setBody(body==null?null:body.copy());	
		copy.setOffsetAndLength(this);
	}
}
