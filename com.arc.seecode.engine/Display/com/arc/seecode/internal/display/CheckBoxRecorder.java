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

import com.arc.seecode.display.ISeeCodeTextViewer;


/**
 * A recorder of a checkbox state.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class CheckBoxRecorder implements ICheckBoxRecorder {
    private ISeeCodeTextViewer mViewer;
    CheckBoxRecorder(ISeeCodeTextViewer viewer){
        mViewer = viewer;
    }
    @Override
    public void saveCheckBoxValue (String id, boolean v) {
       mViewer.addValueUpdate(id,v?"1":"0");

    }

}
