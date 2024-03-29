/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * Represents a function definition contained in a try block.
 * @see ICPPASTFunctionWithTryBlock
 */
public class CPPASTFunctionWithTryBlock extends CPPASTFunctionDefinition implements ICPPASTFunctionWithTryBlock {

    public CPPASTFunctionWithTryBlock() {
	}

	public CPPASTFunctionWithTryBlock(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement) {
		super(declSpecifier, declarator, bodyStatement);
	}
	
	@Override
	public CPPASTFunctionWithTryBlock copy() {
		IASTDeclSpecifier declSpecifier = getDeclSpecifier();
	    IASTFunctionDeclarator declarator = getDeclarator();
	    IASTStatement bodyStatement = getBody();
		
		CPPASTFunctionWithTryBlock copy = new CPPASTFunctionWithTryBlock();
		copy.setDeclSpecifier(declSpecifier == null ? null : declSpecifier.copy());
		copy.setDeclarator(declarator == null ? null : declarator.copy());
		copy.setBody(bodyStatement == null ? null : bodyStatement.copy());
		
		for(ICPPASTConstructorChainInitializer initializer : getMemberInitializers())
			copy.addMemberInitializer(initializer == null ? null : initializer.copy());
		for(ICPPASTCatchHandler handler : getCatchHandlers())
			copy.addCatchHandler(handler == null ? null : handler.copy());
		
		copy.setOffsetAndLength(this);
		return copy;
	}

	public void addCatchHandler(ICPPASTCatchHandler statement) {
        assertNotFrozen();
    	if (statement != null) {
    		catchHandlers = (ICPPASTCatchHandler[]) ArrayUtil.append( ICPPASTCatchHandler.class, catchHandlers, ++catchHandlersPos, statement );
    		statement.setParent(this);
			statement.setPropertyInParent(CATCH_HANDLER);
    	}
    }

    public ICPPASTCatchHandler [] getCatchHandlers() {
        if( catchHandlers == null ) return ICPPASTCatchHandler.EMPTY_CATCHHANDLER_ARRAY;
        catchHandlers = (ICPPASTCatchHandler[]) ArrayUtil.removeNullsAfter( ICPPASTCatchHandler.class, catchHandlers, catchHandlersPos );
        return catchHandlers;
    }


    private ICPPASTCatchHandler [] catchHandlers = null;
    private int catchHandlersPos=-1;
    
    @Override
	protected boolean acceptCatchHandlers( ASTVisitor action ){
    	final ICPPASTCatchHandler [] handlers = getCatchHandlers();
        for (int i=0; i<handlers.length; i++) {
            if (!handlers[i].accept(action)) 
            	return false;
        }
        return true;
    }
}
