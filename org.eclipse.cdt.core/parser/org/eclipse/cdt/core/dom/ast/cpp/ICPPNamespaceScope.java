/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;

/**
 * A namespace scope is either a block-scope or a namespace-scope or global scope.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPNamespaceScope extends ICPPScope {

	/**
	 * Add a directive that nominates another namespace to this scope.
	 */
	public void addUsingDirective(ICPPUsingDirective usingDirective) throws DOMException;

	/**
	 * Get the using directives that have been added to this scope to nominate other
	 * namespaces during lookup. 
	 */
	public ICPPUsingDirective[] getUsingDirectives() throws DOMException;
}
