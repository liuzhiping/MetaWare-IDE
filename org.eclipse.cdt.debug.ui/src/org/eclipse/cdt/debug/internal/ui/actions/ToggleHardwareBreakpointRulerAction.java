/*****************************************************************
 * Copyright (c) 2006 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Texas Instruments - Initial API and implementation
 *****************************************************************/

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

public class ToggleHardwareBreakpointRulerAction extends Action implements IUpdate {
	
	private IVerticalRulerInfo fRuler;
	private IWorkbenchPart fTargetPart;
	private ToggleBreakpointAdapter fBreakpointAdapter;

	static class EmptySelection implements ISelection {

		public boolean isEmpty() {
			return true;
		}		
	}

	private static final ISelection EMPTY_SELECTION = new EmptySelection();  

	/**
	 * Constructor for ToggleHardwareBreakpointRulerAction.
	 * 
	 * @param part
	 * @param ruler
	 */
	public ToggleHardwareBreakpointRulerAction( IWorkbenchPart part, IVerticalRulerInfo ruler ) {
		super( ActionMessages.getString( "ToggleHardwareBreakpointRulerAction.Toggle_Breakpoint_1" ) ); //$NON-NLS-1$
		fRuler = ruler;
		setTargetPart( part );
		fBreakpointAdapter = new ToggleBreakpointAdapter();
		part.getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp( this, ICDebugHelpContextIds.TOGGLE_BREAKPOINT_ACTION );
		setId( IInternalCDebugUIConstants.ACTION_TOGGLE_HARDWARE_BREAKPOINT );
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		try {
			getBreakpointAdapter().toggleLineBreakpoints( getTargetPart(), getTargetSelection(), Boolean.TRUE );
		}
		catch( CoreException e ) {
			ErrorDialog.openError( getTargetPart().getSite().getShell(), 
								   ActionMessages.getString( "ToggleBreakpointRulerAction.Error_1" ), //$NON-NLS-1$
								   ActionMessages.getString( "ToggleBreakpointRulerAction.Operation_failed_1" ), //$NON-NLS-1$
								   e.getStatus() );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled( getBreakpointAdapter().canToggleHardwareLineBreakpoints( getTargetPart(), getTargetSelection() ) );
	}

	/**
	 * Disposes this action
	 */
	public void dispose() {
		setTargetPart( null );
		fRuler = null;
	}

	/**
	 * Returns this action's vertical ruler info.
	 *
	 * @return this action's vertical ruler
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRuler;
	}

	protected IWorkbenchPart getTargetPart() {
		return this.fTargetPart;
	}

	protected ToggleBreakpointAdapter getBreakpointAdapter() {
		return this.fBreakpointAdapter;
	}

	/**
	 * Returns the current selection in the active part, possibly
	 * and empty selection, but never <code>null</code>.
	 * 
	 * @return the selection in the active part, possibly empty
	 */
	protected ISelection getTargetSelection() {
		IDocument doc = getDocument();
		if ( doc != null ) {
			int line = getVerticalRulerInfo().getLineOfLastMouseButtonActivity();
			if(line < 0)
				return EMPTY_SELECTION;
			
			try {
				IRegion region = doc.getLineInformation( line );
				return new TextSelection( doc, region.getOffset(), region.getLength() );
			}
			catch( BadLocationException e ) {
				DebugPlugin.log( e );
			} 
		}
		return EMPTY_SELECTION;
	}

	private void setTargetPart( IWorkbenchPart targetPart ) {
		this.fTargetPart = targetPart;
	}

	private IDocument getDocument() {
		IWorkbenchPart targetPart = getTargetPart();
		if ( targetPart instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if ( provider != null )
				return provider.getDocument( textEditor.getEditorInput() );
		}
//		else if ( targetPart instanceof DisassemblyView ) {
//			DisassemblyView dv = (DisassemblyView)targetPart;
//			IDocumentProvider provider = dv.getDocumentProvider();
//			if ( provider != null )
//				return provider.getDocument( dv.getInput() );
//		}
		return null;
	}

}
