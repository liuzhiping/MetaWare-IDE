/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.seecode.internal.display;


/**
 * A callback for recording the state of a checkbox so that it
 * can later be restored in a future session.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICheckBoxRecorder {
    /**
     * Save the state of a check box so that it can later be restored.
     * @param id the id of the checkbox.
     * @param v the value of the checkbox.
     */
    public void saveCheckBoxValue(String id, boolean v);
}
