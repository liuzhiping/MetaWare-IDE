/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.cview;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.ui.CElementGrouping;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * VirtualGrouping
 */
public class LibraryRefContainer extends CElementGrouping {

	private Object[] EMPTY = new Object[0];
	private ICProject fCProject;

	/**
	 * 
	 */
	public LibraryRefContainer(ICProject cproject) {
		super(LIBRARY_REF_CONTAINER);
		fCProject = cproject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		if (adapter == ICProject.class) {
			return fCProject;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object o) {
		try {
			ILibraryReference[] references = fCProject.getLibraryReferences();
			ArrayList<ILibraryReference> list = new ArrayList<ILibraryReference>(references.length);
			for (ILibraryReference reference : references) {
				IPath path = reference.getPath();
				IFile file = reference.getCModel().getWorkspace().getRoot().getFileForLocation(path);
				if (file == null || !file.isAccessible()) {
					list.add(reference);
				}
			}
			return list.toArray();
		} catch (CModelException e) {
		}
		return EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return CPluginImages.DESC_OBJS_LIBRARY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object o) {
		return CViewMessages.LibraryRefContainer_Libraries; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object o) {
		return getCProject();
	}

	public ICProject getCProject() {
		return fCProject;
	}

}
