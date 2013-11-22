/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Justin You (Synopsys inc)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.internal.core.dom.parser.ASTRegisterInitializer;

/**
 * Initializer with double equals sign as in <code>int x == 0;</code>.
 * it's the ARC specific syntax for direct register assignment.
 * it's very similar with the ASTEqualsInitializer
 */

//CUSTOMIZATION XKORAT
public class CASTRegisterInitializer extends ASTRegisterInitializer {
    public CASTRegisterInitializer() {
	}

	public CASTRegisterInitializer(IASTInitializerClause arg) {
		super(arg);
	}

	@Override
	public CASTRegisterInitializer copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CASTRegisterInitializer copy(CopyStyle style) {
		IASTInitializerClause arg = getInitializerClause();
		CASTRegisterInitializer copy = new CASTRegisterInitializer(arg == null ? null : arg.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
}
