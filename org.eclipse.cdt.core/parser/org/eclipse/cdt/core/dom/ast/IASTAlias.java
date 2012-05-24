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

// CUSTOMIZATION
public interface IASTAlias extends IASTDeclaration {
	/**
	 * Name being bound to another.
	 * @return the name being bound to another
	 */
	public IASTName getSourceName();
	
	/**
	 * Name to which the source is bound.
	 * @return the name to which the source is bound.
	 */
	public IASTName getTargetName();
}
