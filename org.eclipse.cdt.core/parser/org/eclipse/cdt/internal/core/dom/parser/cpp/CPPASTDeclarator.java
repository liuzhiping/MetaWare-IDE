/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * C++ specific declarator.
 */
public class CPPASTDeclarator extends ASTNode implements ICPPASTDeclarator, IASTImplicitNameOwner {
    private IASTInitializer initializer;
    private IASTName name;
	private IASTImplicitName[] implicitNames; 
    private IASTDeclarator nested;
    private IASTPointerOperator[] pointerOps;
    private boolean isPackExpansion;
   
    public CPPASTDeclarator() {
	}

	public CPPASTDeclarator(IASTName name) {
		setName(name);
	}

    public CPPASTDeclarator(IASTName name, IASTInitializer initializer) {
		this(name);
		setInitializer(initializer);
	}

    public CPPASTDeclarator copy() {
		CPPASTDeclarator copy = new CPPASTDeclarator();
		copyBaseDeclarator(copy);
		return copy;
	}

    protected void copyBaseDeclarator(CPPASTDeclarator copy) {
    	copy.setName(name == null ? null : name.copy());
    	copy.setInitializer(initializer == null ? null : initializer.copy());
		copy.setNestedDeclarator(nested == null ? null : nested.copy());
		copy.isPackExpansion= isPackExpansion;
		for (IASTPointerOperator pointer : getPointerOperators())
			copy.addPointerOperator(pointer == null ? null : pointer.copy());
		copy.setOffsetAndLength(this);
    }

	public boolean declaresParameterPack() {
		return isPackExpansion;
	}

	public IASTPointerOperator[] getPointerOperators() {
        if (pointerOps == null) return IASTPointerOperator.EMPTY_ARRAY;
        pointerOps = (IASTPointerOperator[]) ArrayUtil.trim(IASTPointerOperator.class, pointerOps);
        return pointerOps;
    }

    public IASTDeclarator getNestedDeclarator() {
        return nested;
    }

    public IASTName getName() {
        return name;
    }

    public IASTInitializer getInitializer() {
        return initializer;
    }

    public void setInitializer(IASTInitializer initializer) {
        assertNotFrozen();
        this.initializer = initializer;
        if (initializer != null) {
			initializer.setParent(this);
			initializer.setPropertyInParent(INITIALIZER);
		}
    }

    public void addPointerOperator(IASTPointerOperator operator) {
        assertNotFrozen();
    	if (operator != null) {
    		operator.setParent(this);
			operator.setPropertyInParent(POINTER_OPERATOR);
    		pointerOps = (IASTPointerOperator[]) ArrayUtil.append(IASTPointerOperator.class, pointerOps, operator);
    	}
    }

    public void setNestedDeclarator(IASTDeclarator nested) {
        assertNotFrozen();
        this.nested = nested;
        if (nested != null) {
			nested.setParent(this);
			nested.setPropertyInParent(NESTED_DECLARATOR);
		}
    }

    public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(DECLARATOR_NAME);
		}
    }

    public void setDeclaresParameterPack(boolean val) {
    	assertNotFrozen();
    	isPackExpansion= val;
	}

	@Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitDeclarators) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}

        if (pointerOps != null) {
        	for (IASTPointerOperator op : pointerOps) {
        		if (op == null)
        			break;
                if (!op.accept(action))
                	return false;
        	}
        }

        if (nested == null && name != null) {
        	IASTDeclarator outermost= ASTQueries.findOutermostDeclarator(this);
        	if (outermost.getPropertyInParent() != IASTTypeId.ABSTRACT_DECLARATOR) {
        		if (!name.accept(action))
        			return false;
                if (action.shouldVisitImplicitNames) {
                	for (IASTImplicitName implicitName : getImplicitNames()) {
                		if (!implicitName.accept(action))
                			return false;
                	}
                }
            }
		}

        if (nested != null && !nested.accept(action))
        	return false;

        if (!postAccept(action))
        	return false;

        if (action.shouldVisitDeclarators && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

        return true;
    }

    protected boolean postAccept(ASTVisitor action) {
		return initializer == null || initializer.accept(action);
    }

	public int getRoleForName(IASTName n) {
		// 3.1.2
        IASTNode parent = ASTQueries.findOutermostDeclarator(this).getParent();
        if (parent instanceof IASTDeclaration) {
        	// a declaration is a definition unless ...
            if (parent instanceof IASTFunctionDefinition)
                return r_definition;

            if (parent instanceof IASTSimpleDeclaration) {
            	final IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) parent;

            	// unless it declares a function without body
            	if (this instanceof IASTFunctionDeclarator) {
            		return r_declaration;
            	}

				final int storage = sdecl.getDeclSpecifier().getStorageClass();
            	// unless it contains the extern specifier or a linkage-specification and neither initializer nor function-body
            	if (getInitializer() == null && (storage == IASTDeclSpecifier.sc_extern || isSimpleLinkageSpec(sdecl))) {
            		return r_declaration;
            	}
            	// unless it declares a static data member in a class declaration
            	if (storage == IASTDeclSpecifier.sc_static && CPPVisitor.getContainingScope(parent) instanceof ICPPClassScope) {
            		return r_declaration;
            	}
            	// unless it is a class name declaration: no declarator in this case
            	// unless it is a typedef declaration
            	if (storage == IASTDeclSpecifier.sc_typedef)
            		return r_definition; // should actually be a declaration

            	// unless it is a using-declaration or using-directive: no declarator in this case
            }

            // all other cases
        	return r_definition;
        }

        if (parent instanceof IASTTypeId)
            return r_reference;

        if (parent instanceof IASTParameterDeclaration)
            return (n.getLookupKey().length > 0) ? r_definition : r_declaration;

        return r_unclear;
	}

	private boolean isSimpleLinkageSpec(IASTSimpleDeclaration sdecl) {
		IASTNode parent= sdecl.getParent();
		if (parent instanceof ICPPASTLinkageSpecification) {
			ICPPASTLinkageSpecification spec= (ICPPASTLinkageSpecification) parent;
			// todo distinction between braced enclose and simple linkage specification
			if (spec.getDeclarations().length == 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see IASTImplicitNameOwner#getImplicitNames()
	 */
	public IASTImplicitName[] getImplicitNames() {
		if (implicitNames == null) {
			ICPPConstructor ctor = CPPSemantics.findImplicitlyCalledConstructor(this);
			if (ctor == null) {
				implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName ctorName = new CPPASTImplicitName(ctor.getNameCharArray(), this);
				ctorName.setBinding(ctor);
				IASTName id = name;
				if (id instanceof ICPPASTQualifiedName) {
					id = ((ICPPASTQualifiedName) id).getLastName();
				}
				ctorName.setOffsetAndLength((ASTNode) id);
				implicitNames = new IASTImplicitName[] { ctorName };
			}
    	}

    	return implicitNames;  
	}
}
