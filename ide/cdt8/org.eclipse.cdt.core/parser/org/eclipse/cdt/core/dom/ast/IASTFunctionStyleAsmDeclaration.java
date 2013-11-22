/**CUSTOMIZATION
 * SYNOPSYS INC.
 */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author Justin You
 * 
 * create an new AST node for functionStyleAsmDeclaration, for example _Asm int foo(int x) { //all ARC assembly code }
 * this AST node make CDT Codan to recognize this specific syntax and don't throw error marker on the GUI for non return warning.
 * this functionStyleAsmDeclaration is the child of the IASTFunctionDefinition and may implicitly return the value by assembly code.
 */
public interface IASTFunctionStyleAsmDeclaration extends IASTFunctionDefinition {

}
