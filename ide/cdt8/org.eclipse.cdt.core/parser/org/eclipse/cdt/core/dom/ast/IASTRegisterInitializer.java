/*******************************************************************************
 * Copyright (c) 2013 Synopsys Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Justin You - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast;

/**
 * Initializer with double equals sign as in <code>int x == 0;</code>.
 * it's the ARC specific syntax for direct register assignment.
 * it's very similar with the IASTEqualsInitializer
 * @since 8.1
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
//CUSTOMIZATION XKORAT
public interface IASTRegisterInitializer extends IASTInitializer {
	ASTNodeProperty INITIALIZER = new ASTNodeProperty(
			"IASTRegisterInitializer - INITIALIZER [IASTInitializerClause]"); //$NON-NLS-1$
		
		/**
		 * Returns the expression or braced initializer list of this initializer.
		 */
		IASTInitializerClause getInitializerClause();
		
		/**
		 * Not allowed on frozen ast.
		 */
		void setInitializerClause(IASTInitializerClause clause);

}
