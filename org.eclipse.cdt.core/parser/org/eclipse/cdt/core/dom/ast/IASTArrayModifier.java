/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the portion of the node that represents the portions when someone
 * declares a variable/type which is an array.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTArrayModifier extends IASTNode {

	/**
	 * Node property that describes the relationship between an
	 * <code>IASTArrayModifier</code> and an <code>IASTExpression</code>.
	 */
	public static final ASTNodeProperty CONSTANT_EXPRESSION = new ASTNodeProperty(
			"IASTArrayModifier.CONSTANT_EXPRESSION - IASTExpression for IASTArrayModifier"); //$NON-NLS-1$

	/**
	 * <code>EMPTY_ARRAY</code> is referred to in implementations
	 */
	public static final IASTArrayModifier[] EMPTY_ARRAY = new IASTArrayModifier[0];

	/**
	 * Get the constant expression that represents the size of the array.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getConstantExpression();

	/**
	 * Set the constant expression that represents the size of the array.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setConstantExpression(IASTExpression expression);

	/**
	 * @since 5.1
	 */
	public IASTArrayModifier copy();
}
