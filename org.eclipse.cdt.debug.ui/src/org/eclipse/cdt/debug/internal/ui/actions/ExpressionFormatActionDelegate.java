/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.expressions.ExpressionFormatManager;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The superclass of the all format action delegates.
 */
public abstract class ExpressionFormatActionDelegate implements IObjectActionDelegate {

	private CVariableFormat fFormat = CVariableFormat.NATURAL;

	private IWatchExpression[] fExpressions = null;

	/**
	 * Constructor for VariableFormatActionDelegate.
	 */
	public ExpressionFormatActionDelegate( CVariableFormat format ) {
		fFormat = format;
	}

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action ) {
		IExpression[] vars = getExpressions();
		if ( vars != null && vars.length > 0 ) {
			final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, "", null ); //$NON-NLS-1$
			BusyIndicator.showWhile( Display.getCurrent(), new Runnable() {

				public void run() {
					try {
						doAction( getExpressions() );
					}
					catch( DebugException e ) {
						ms.merge( e.getStatus() );
					}
				}
			} );
			if ( !ms.isOK() ) {
				IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
				if ( window != null ) {
					CDebugUIPlugin.errorDialog( ActionMessages.getString( "VariableFormatActionDelegate.0" ), ms ); //$NON-NLS-1$
				}
				else {
					CDebugUIPlugin.log( ms );
				}
			}
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@SuppressWarnings("unchecked")
    public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			List<IWatchExpression> list = new ArrayList<IWatchExpression>();
			IStructuredSelection ssel = (IStructuredSelection)selection;
			Iterator i = ssel.iterator();
			while( i.hasNext() ) {
				Object o = i.next();
				if ( o instanceof IWatchExpression ) {
					IWatchExpression expr = (IWatchExpression)o;
//					boolean enabled = expr.supportsFormatting();
//					action.setEnabled( enabled );
					if ( true /*enabled*/ ) {
					    CVariableFormat f = ExpressionFormatManager.getInstance().getFormat(expr);
						action.setChecked( f == fFormat );
						list.add( expr );
					}
				}
			}
			setExpressions( list.toArray( new IWatchExpression[list.size()] ) );
		}
		else {
			action.setChecked( false );
			action.setEnabled( false );
		}
	}

	protected void doAction( IWatchExpression[] exprs ) throws DebugException {
	    ExpressionFormatManager.getInstance().setFormat(exprs,fFormat);
	}

	protected IWatchExpression[] getExpressions() {
		return fExpressions;
	}

	private void setExpressions ( IWatchExpression[] exprs ) {
		fExpressions = exprs;
	}
}
