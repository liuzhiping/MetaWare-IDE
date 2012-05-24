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
package com.arc.seecode.engine;

/**
 * An interface for accessing elements of an aggregate. The {@link EngineInterface} implements
 * this and is used in the {@link Value} class to retrieve elements lazily from the debugger 
 * engine.
 * <p>
 * It is also implemented to access elements of a register that is many words long (e.g.,
 * vector register).
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IAggregateAccessor {
    
    /**
     * @param cookie
     * @param elementIndex
     * @return the value of the element of an aggregate variable.
     */
    public Value getValueElement(int cookie, int elementIndex)
            throws EngineException;
    
    /**
     * Called from {@link Value#setElement(int,String,StackFrameRef)} to
     * set the value of an element.
     * @param cookie
     * @param elementIndex
     * @param newValue
     * @throws EngineException
     */
    public void setValueElement(int cookie, int elementIndex, String newValue, int frameID)
            throws EngineException, EvaluationException ;
    
    /**
     * Free value "cookie" that was used to lazily retrieve aggregate elements.
     * It doesn't need to be guarded because the implementation enqueues. Called
     * from the garbage collector via {@link Value#finalize()}.
     * 
     * @param cookie
     */
    public void freeValueCookie(int cookie) throws EngineException;

}
