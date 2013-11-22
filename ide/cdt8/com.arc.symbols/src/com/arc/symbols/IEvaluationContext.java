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
package com.arc.symbols;

/**
 * A context by which we can evaluate an expression involving symbols read from
 * debug information.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IEvaluationContext {
    /**
     * Return the value of a register
     * @param registerNumber
     * @return the value of a register.
     */
    long getRegisterValue(int registerNumber);
    int readByte(long address);
    int readShort(long address);
    int readWord(long address);
}
