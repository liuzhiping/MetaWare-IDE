/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Synopsys Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IInternalVariable;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public class CPPVariable extends PlatformObject implements ICPPVariable, ICPPInternalBinding, IInternalVariable {
    public static class CPPVariableProblem extends ProblemBinding implements ICPPVariable {
        public CPPVariableProblem(IASTNode node, int id, char[] arg) {
            super(node, id, arg);
        }

        public IType getType() throws DOMException {
            throw new DOMException(this);
        }

        public boolean isStatic() throws DOMException {
            throw new DOMException(this);
        }
        public String[] getQualifiedName() throws DOMException {
            throw new DOMException(this);
        }
        public char[][] getQualifiedNameCharArray() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isGloballyQualified() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isMutable() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isExtern() throws DOMException {
             throw new DOMException(this);
        }
        public boolean isExternC() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isAuto() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isRegister() throws DOMException {
            throw new DOMException(this);
        }
		public IValue getInitialValue() {
			return null;
		}
    }

	private IASTName fDefinition = null;
	private IASTName fDeclarations[] = null;
	private IType fType = null;
	private boolean fAllResolved;
	
	public CPPVariable(IASTName name) {
	    boolean isDef = name == null ? false : name.isDefinition();
	    if (name instanceof ICPPASTQualifiedName) {
	        IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ns.length - 1];
	    }
	    
	    if (isDef)
	        fDefinition = name;
	    else 
	        fDeclarations = new IASTName[] { name };
	    
	    // built-in variables supply a null
	    if (name != null) {
	    	name.setBinding(this);
	    } else {
	    	assert this instanceof CPPBuiltinVariable;
	    }
	}
	
	private IASTDeclarator findDeclarator(IASTName name) {
	    IASTNode node = name.getParent();
	    if (node instanceof ICPPASTQualifiedName)
	        node = node.getParent();
	    
	    if (!(node instanceof IASTDeclarator))
	        return null;
	    
	    IASTDeclarator dtor = (IASTDeclarator) node;
	    while (dtor.getParent() instanceof IASTDeclarator)
	        dtor = (IASTDeclarator) dtor.getParent();
	    
	    return dtor;
	}		
	
	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		IASTName name = (IASTName) node;
		if (fDefinition == null && name.isDefinition()) {
			fDefinition = name;
		} else if (fDeclarations == null) {
			fDeclarations = new IASTName[] { name };
		} else {
			// keep the lowest offset declaration at the first position
			if (fDeclarations.length > 0
					&& ((ASTNode) node).getOffset() < ((ASTNode) fDeclarations[0]).getOffset()) {
				fDeclarations = (IASTName[]) ArrayUtil.prepend(IASTName.class, fDeclarations, name);
			} else {
				fDeclarations = (IASTName[]) ArrayUtil.append(IASTName.class, fDeclarations, name);
			}
		}
		// array types may be incomplete
		if (fType instanceof IArrayType) {
			fType = null;
		}
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return fDeclarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return fDefinition;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		if (fType != null) {
			return fType;
		}
		
		IArrayType firstCandidate= null;
		final int length = fDeclarations == null ? 0 : fDeclarations.length;
		for (int i = -1; i < length; i++) {
			IASTName n = i==-1 ? fDefinition : fDeclarations[i];
			if (n != null) {
				while (n.getParent() instanceof IASTName)
					n = (IASTName) n.getParent();

				IASTNode node = n.getParent();
				if (node instanceof IASTDeclarator) {
					IType t= CPPVisitor.createType((IASTDeclarator) node);
					if (t instanceof IArrayType && ((IArrayType) t).getSize() == null) {
						if (firstCandidate == null) {
							firstCandidate= (IArrayType) t;
						}
					} else {
						return fType= t;
					}
				}
			}
		}
		fType= firstCandidate;
		if (!fAllResolved) {
			resolveAllDeclarations();
			return getType();
		}
		return fType;
	}

	private void resolveAllDeclarations() {
		if (fAllResolved)
			return;
		fAllResolved= true;
		final int length = fDeclarations == null ? 0 : fDeclarations.length;
		for (int i = -1; i < length; i++) {
			IASTName n = i==-1 ? fDefinition : fDeclarations[i];
			if (n != null) {
			    IASTTranslationUnit tu = n.getTranslationUnit();
		        if (tu != null) {
		            CPPVisitor.getDeclarations(tu, this);
		            return;
		        }
		    }
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return new String(getNameCharArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    if (fDeclarations != null) {
	        return fDeclarations[0].getSimpleID();
	    } 
	    return fDefinition.getSimpleID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope(fDefinition != null ? fDefinition : fDeclarations[0]);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while (scope != null) {
            if (scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		addDeclaration(node);
	}

	public boolean hasStorageClass(int storage) {
	    IASTName name = (IASTName) getDefinition();
        IASTNode[] ns = getDeclarations();
        
        int i = -1;
        do {
            if (name != null) {
                IASTNode parent = name.getParent();
	            while (!(parent instanceof IASTDeclaration))
	                parent = parent.getParent();
	            
	            if (parent instanceof IASTSimpleDeclaration) {
	                IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) parent).getDeclSpecifier();
	                if (declSpec.getStorageClass() == storage) {
	                	return true;
	                }
	            }
            }
            if (ns != null && ++i < ns.length)
                name = (IASTName) ns[i];
            else
                break;
        } while (name != null);
        return false;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable#isMutable()
     */
    public boolean isMutable() {
        //7.1.1-8 the mutable specifier can only be applied to names of class data members
        return false;
    }

    public boolean isStatic() {
		return hasStorageClass(IASTDeclSpecifier.sc_static);
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExtern() {
        return hasStorageClass(IASTDeclSpecifier.sc_extern);
    }

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExternC() {
	    if (CPPVisitor.isExternC(getDefinition())) {
	    	return true;
	    }
        IASTNode[] ds= getDeclarations();
        if (ds != null) {
        	for (IASTNode element : ds) {
        		if (CPPVisitor.isExternC(element)) {
        			return true;
        		}
			}
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isAuto()
     */
    public boolean isAuto() {
        return hasStorageClass(IASTDeclSpecifier.sc_auto);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
     */
    public boolean isRegister() {
        return hasStorageClass(IASTDeclSpecifier.sc_register);
    }
    
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}
	
	public IBinding getOwner() throws DOMException {
		IASTName node = fDefinition != null ? fDefinition : fDeclarations[0];
		return CPPVisitor.findNameOwner(node, !hasStorageClass(IASTDeclSpecifier.sc_extern)); 
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public IValue getInitialValue() {
		return getInitialValue(Value.MAX_RECURSION_DEPTH);
	}
	
	public IValue getInitialValue(int maxDepth) {
		if (fDefinition != null) {
			final IValue val= getInitialValue(fDefinition, maxDepth);
			if (val != null)
				return val;
		}
		if (fDeclarations != null) {
			for (IASTName decl : fDeclarations) {
				if (decl == null)
					break;
				final IValue val= getInitialValue(decl, maxDepth);
				if (val != null)
					return val;
			}
		}		
		return null;
	}
	
	private IValue getInitialValue(IASTName name, int maxDepth) {
		IASTDeclarator dtor= findDeclarator(name);
		if (dtor != null) {
			IASTInitializer init= dtor.getInitializer();
			if (init != null) {
				IASTInitializerClause clause= null;
				if (init instanceof IASTEqualsInitializer) {
					clause= ((IASTEqualsInitializer) init).getInitializerClause();
				} else if (init instanceof ICPPASTConstructorInitializer) {
					IASTInitializerClause[] args= ((ICPPASTConstructorInitializer) init).getArguments();
					if (args.length == 1 && args[0] instanceof IASTExpression) {
						IType type= SemanticUtil.getUltimateTypeUptoPointers(getType());
						if (type instanceof IPointerType || type instanceof IBasicType) {
							clause= args[0];
						}
					}
				} else if (init instanceof ICPPASTInitializerList) {
					ICPPASTInitializerList list= (ICPPASTInitializerList) init;
					switch (list.getSize()) {
					case 0:
						return Value.create(0);
					case 1:
						clause= list.getClauses()[0];
					}
				}
				if (clause instanceof IASTExpression) {
					return Value.create((IASTExpression) clause, maxDepth);
				}
				return Value.UNKNOWN;
			}
		}
		return null;
	}
	
	
	public void setType(IType type){  // CUSTOMIZATION for iterator for loop variables
		this.fType = type;
	}

}
