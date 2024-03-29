/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the root class of expressions.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTExpression extends IASTInitializerClause {
	/**
	 * Empty expression array.
	 */
	public static final IASTExpression[] EMPTY_EXPRESSION_ARRAY = new IASTExpression[0];
	
	public IType getExpressionType();
	
	/**
	 * Returns whether this expression is an lvalue. LValues are for instance required on the
	 * left hand side of an assignment expression.
	 * @since 5.2
	 */
	public boolean isLValue();
	
	/**
	 * @since 5.1
	 */
	public IASTExpression copy();
}
