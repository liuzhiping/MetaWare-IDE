/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 * Synopsys - MetaWare debugger integration
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.icu.text.MessageFormat;

/**
 * A tracepoint that collects data when a particular line of code is reached.
 *
 * @since 6.1
 */
public class CLineTracepoint extends AbstractTracepoint implements ICTracepoint {

	private static final String C_LINE_TRACEPOINT_MARKER = "org.eclipse.cdt.debug.core.cLineTracepointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CLineTracepoint.
	 */
	public CLineTracepoint() {
	}

	/**
	 * Constructor for CLineTracepoint.
	 */
	public CLineTracepoint( IResource resource, Map<String, Object> attributes, boolean add ) throws CoreException {
		super( resource, getMarkerType(), attributes, add );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType() {
		return C_LINE_TRACEPOINT_MARKER;
	}

	/*(non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException {
		return MessageFormat.format( BreakpointMessages.getString( "CLineTracepoint.0" ), new String[] { CDebugUtils.getBreakpointText( this, false ) } ); //$NON-NLS-1$
	}

    @Override
    public int getOffsetFromFunction () throws CoreException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getOffsetFromLine () throws CoreException {
        // TODO Auto-generated method stub
        return 0;
    }
}
