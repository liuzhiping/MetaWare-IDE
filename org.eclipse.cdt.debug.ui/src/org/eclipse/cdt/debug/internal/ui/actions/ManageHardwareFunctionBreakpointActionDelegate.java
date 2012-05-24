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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * The delegate of the "Toggle Hardware Function Breakpoint" action.
 */
public class ManageHardwareFunctionBreakpointActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private ToggleBreakpointAdapter fBreakpointAdapter;
	private IWorkbenchPart fTargetPart;
	private ISelection fSelection;

	public ManageHardwareFunctionBreakpointActionDelegate() {
		fBreakpointAdapter = new ToggleBreakpointAdapter();
	}

	/*(non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		try {
			getBreakpointAdapter().toggleMethodBreakpoints( getTargetPart(), getSelection(), true );
		}
		catch( CoreException e ) {
			ErrorDialog.openError( getTargetPart().getSite().getShell(),
								   ActionMessages.getString( "ManageFunctionBreakpointActionDelegate.Error_1" ), //$NON-NLS-1$
					   			   ActionMessages.getString( "ManageFunctionBreakpointActionDelegate.Operation_failed_1" ), //$NON-NLS-1$
					   			   e.getStatus() );
		}
	}

	/*(non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
		fTargetPart = targetPart;
	}

	/*(non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		setSelection( selection );
		action.setEnabled( getBreakpointAdapter().canToggleHardwareMethodBreakpoints( getTargetPart(), getSelection() ) );
	}

	protected IWorkbenchPart getTargetPart() {
		return fTargetPart;
	}

	protected ISelection getSelection() {
		return fSelection;
	}

	protected ToggleBreakpointAdapter getBreakpointAdapter() {
		return fBreakpointAdapter;
	}

	protected void setSelection( ISelection selection ) {
		fSelection = selection;
	}
}
