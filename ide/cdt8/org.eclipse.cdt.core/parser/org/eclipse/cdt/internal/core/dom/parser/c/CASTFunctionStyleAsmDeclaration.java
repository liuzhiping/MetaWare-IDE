/**CUSTOMIZATION
 * SYNOPSYS INC.
 */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleAsmDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class CASTFunctionStyleAsmDeclaration extends CASTFunctionDefinition
		implements IASTFunctionStyleAsmDeclaration {
	
	public CASTFunctionStyleAsmDeclaration() {

	}
	
	public CASTFunctionStyleAsmDeclaration(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement) {
		super(declSpecifier, declarator, bodyStatement);
	}

}
