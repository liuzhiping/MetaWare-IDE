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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;

/**
 * @deprecated Replaced by {@link CPPASTSimpleDeclSpecifier}
 */
@Deprecated
public class GPPASTSimpleDeclSpecifier extends CPPASTSimpleDeclSpecifier implements
		IGPPASTSimpleDeclSpecifier {

    public GPPASTSimpleDeclSpecifier() {
	}
    
	public GPPASTSimpleDeclSpecifier(IASTExpression typeofExpression) {
		super();
		setDeclTypeExpression(typeofExpression);
	}
	
	@Override
	public GPPASTSimpleDeclSpecifier copy() {
		GPPASTSimpleDeclSpecifier copy = new GPPASTSimpleDeclSpecifier();
		copySimpleDeclSpec(copy);
		return copy;
	}


    public void setTypeofExpression(IASTExpression typeofExpression) {
    	setDeclTypeExpression(typeofExpression);
    }

    public IASTExpression getTypeofExpression() {
        return getDeclTypeExpression();
    }
}
