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
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAlias;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;


// CUSTOMIZATION
// Represent a _Pragma alias(source,target)
//
public class CASTAlias extends ASTNode implements IASTAlias {
	
	public static final ASTNodeProperty ALIAS_SOURCE = new ASTNodeProperty("ALIAS_SOURCE"); //$NON-NLS-1$
	public static final ASTNodeProperty ALIAS_TARGET = new ASTNodeProperty("ALIAS_TARGET"); //$NON-NLS-1$
	//CUSTOMIZATION
	private List<IASTAttribute> attrs  = new ArrayList<IASTAttribute>(3);

	private IASTName source;
	private IASTName target;

	public CASTAlias(IASTName source, IASTName target) {
		this.source = source;
		this.target = target;
		if (source != null) {
			source.setParent(this);
			source.setPropertyInParent(ALIAS_SOURCE);
		}
		if (target != null) {
			target.setParent(this);
			target.setPropertyInParent(ALIAS_TARGET);
		}
	}

	public IASTName getSourceName() {
		return source;
	}

	public IASTName getTargetName() {
		return target;
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public boolean contains(IASTNode node) {
		return node == source || node == target;
	}

	public IASTDeclaration copy() {
		return new CASTAlias(source, target);
	}
	
	//CUSTOMIZATION
	//8_1_REQUIRE
	public IASTAttribute[] getAttributes(){
		return attrs.toArray(new IASTAttribute[attrs.size()]);

	}

	/**
	 * Adds an attribute to the node.
	 */
	//CUSTOMIZATION
	//8_1_REQUIRE
	public void addAttribute(IASTAttribute attribute){
		attrs.add(attribute);

	}

	//CUSTOMIZATION
	//8.1_REQUIRE
	public IASTDeclaration copy(CopyStyle style){

		return super.copy(this, style);
	}

}
