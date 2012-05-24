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
package com.arc.cdt.debug.seecode.internal.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;


/**
 * Assembly instructions grouped under source lines.
 * @author David Pickens
 */
public final class MixedAssemblyInstructions implements ICDIMixedInstruction {

    private ICDITarget mTarget;
    private ICDIInstruction[] mInstr;
    private String mFile;
    private int mLine;

    /**
     * 
     */
    public MixedAssemblyInstructions(ICDITarget target, String file, int line, ICDIInstruction[]instr) {
       mTarget = target;
       mInstr = instr;
       mFile = file;
       mLine = line;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction#getLineNumber()
     */
    @Override
    public int getLineNumber() {
        return mLine;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction#getFileName()
     */
    @Override
    public String getFileName() {
        return mFile;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIMixedInstruction#getInstructions()
     */
    @Override
    public ICDIInstruction[] getInstructions() {
        return mInstr;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }

}
