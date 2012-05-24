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
package org.eclipse.cdt.core.dom.ast;

/**
 * High C's iterator for-loop body.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IASTForLoopStatement extends IASTStatement {
    /**
     * <code>VAR</code> represents the control variable, an IASTName.
     */
    public static final ASTNodeProperty VAR = new ASTNodeProperty(
            "IASTForLoopStatement.VAR - IASTName  of IASTForStatement"); //$NON-NLS-1$

    /**
     * <code>ITERATOR</code> represents the relationship between a
     * <code>IASTForLoopStatement</code> and its <code>IASTExpression</code>
     * iterator call.
     */
    public static final ASTNodeProperty ITERATOR = new ASTNodeProperty(
            "IASTForLooptatement.ITERATOR - IASTExpression iterator of IASTForLoopStatement"); //$NON-NLS-1$

    /**
     * <code>BODY</code> represents the relationship between a
     * <code>IASTForLoopStatement</code> and its <code>IASTStatement</code>
     * body.
     */
    public static final ASTNodeProperty BODY = new ASTNodeProperty("IASTForStatement.BODY - IASTStatement body of IASTForStatement"); //$NON-NLS-1$

    
    
    /**
     * Get the control variable for the loop. We return it as a declaration instead
     * of a name because that's what our search infrastructure expects.
     * 
     * @return control variable declaration.
     */
    public IASTDeclaration[] getControlVariables();

    /**
     * Set the control variable  for the loop.
     * 
     * @param var the control variable.
     */
    public void addControlVariable(IASTDeclaration var);

    /**
     * Get the iterator expression.
     * 
     * @return the iterator expression.
     */
    public IASTExpression getIteratorExpression();

    /**
     * Set the iterator expression/
     * 
     * @param iterator
     *         
     */
    public void setIteratorExpression(IASTExpression iterator);

    /**
     * Get the statements that this for loop controls.
     * 
     * @return <code>IASTStatement</code>
     */
    public IASTStatement getBody();

    /**
     * Set the body of the for loop.
     * 
     * @param statement
     *            <code>IASTStatement</code>
     */
    public void setBody(IASTStatement statement);

    /**
     * Get the <code>IScope</code> represented by this for loop.
     * 
     * @return <code>IScope</code>
     */
    public IScope getScope();
}
