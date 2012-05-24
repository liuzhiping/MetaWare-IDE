/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

/**
 * Marks ambiguous condition nodes.
 */
public interface IASTAmbiguousCondition extends IASTExpression {
    public static final ASTNodeProperty SUBCONDITION = new ASTNodeProperty( "IASTAmbiguousCondition.SUBCONDITION"); //$NON-NLS-1$

}
