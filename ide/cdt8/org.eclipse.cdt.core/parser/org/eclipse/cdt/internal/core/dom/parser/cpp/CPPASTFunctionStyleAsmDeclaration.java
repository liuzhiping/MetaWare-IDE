/**CUSTOMIZATION
 * SYNOPSYS INC.
 */

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleAsmDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class CPPASTFunctionStyleAsmDeclaration extends CPPASTFunctionDefinition
		implements IASTFunctionStyleAsmDeclaration {
	
    public CPPASTFunctionStyleAsmDeclaration() {
	}

	public CPPASTFunctionStyleAsmDeclaration(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement) {
		super(declSpecifier, declarator, bodyStatement);
	}

}
