/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.views;


import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetDndUtil;
import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class AddTargetAction extends SelectionListenerAction {
	private final Shell shell;

	public AddTargetAction(Shell shell) {
		super(MakeUIPlugin.getResourceString("AddTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;

		setToolTipText(MakeUIPlugin.getResourceString("AddTargetAction.tooltip")); //$NON-NLS-1$
		MakeUIImages.setImageDescriptors(this, "tool16", MakeUIImages.IMG_TOOLS_MAKE_TARGET_ADD); //$NON-NLS-1$
		setEnabled(false);
	}

	@Override
	public void run() {
		Object selection = getSelectedElement();
		try {
			if (selection instanceof IContainer) {
				MakeTargetDialog dialog = new MakeTargetDialog(shell, (IContainer) selection);
				dialog.open();
			} else if (selection instanceof IMakeTarget) {
				IMakeTarget makeTarget = (IMakeTarget)selection;
				MakeTargetDndUtil.copyOneTarget(makeTarget, makeTarget.getContainer(), DND.DROP_COPY, shell, false);
			}
		} catch (CoreException e) {
			MakeUIPlugin.errorDialog(shell, MakeUIPlugin.getResourceString("AddTargetAction.exception.title"), //$NON-NLS-1$
				MakeUIPlugin.getResourceString("AddTargetAction.exception.message"), e); //$NON-NLS-1$
		}

	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection) && getSelectedElement()!=null;
	}

	private Object getSelectedElement() {
		if (getStructuredSelection().size()==1) {
			Object element = getStructuredSelection().getFirstElement();
			if (element instanceof IContainer || element instanceof IMakeTarget) {
				return element;
			}
		}
		return null;
	}

}
