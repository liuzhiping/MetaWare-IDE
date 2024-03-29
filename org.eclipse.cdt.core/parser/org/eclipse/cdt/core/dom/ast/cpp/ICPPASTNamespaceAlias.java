/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This interface represents a namespace alias in C++. e.g. namespace ABC { int
 * x; } namspace DEF = ABC;
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTNamespaceAlias extends IASTDeclaration, IASTNameOwner {

	/**
	 * <code>ALIAS_NAME</code> represents the new namespace name being
	 * introduced.
	 */
	public static final ASTNodeProperty ALIAS_NAME = new ASTNodeProperty(
			"ICPPASTNamespaceAlias.ALIAS_NAME - New namespace name introduced"); //$NON-NLS-1$

	/**
	 * <code>MAPPING_NAME</code> represents the pre-existing namespace which
	 * the new symbol aliases.
	 */
	public static final ASTNodeProperty MAPPING_NAME = new ASTNodeProperty(
			"ICPPASTNamespaceAlias.MAPPING_NAME - Pre-existing namespace the new symbol aliases"); //$NON-NLS-1$

	/**
	 * Get the new alias name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getAlias();

	/**
	 * Set the new alias name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setAlias(IASTName name);

	/**
	 * Get the mapping name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getMappingName();

	/**
	 * Set the mapping name.
	 * 
	 * @param qualifiedName
	 *            <code>IASTName</code>
	 */
	public void setMappingName(IASTName qualifiedName);
	
	
	/**
	 * @since 5.1
	 */
	public ICPPASTNamespaceAlias copy();

}
