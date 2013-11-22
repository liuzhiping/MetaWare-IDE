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
 * A method of a class.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ISymbolMethod extends ISymbolMember {
    /**
     * Return whether or not the method is "abstract".
     * @return whether or not the method is "abstract".
     */
    boolean isAbstract();
    
    /**
     * Return whether or not this method can be overridden in a subclass.
     * All abstract methods are assumed virtual.
     * @return whether or not this method can be overridden in a subclass.
     */
    boolean isVirtual();
}
