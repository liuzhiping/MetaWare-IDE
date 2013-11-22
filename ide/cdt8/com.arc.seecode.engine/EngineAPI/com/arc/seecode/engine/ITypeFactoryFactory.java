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

import com.arc.seecode.engine.type.ITypeFactory;

/**
 * A factory for making type factories. The EngineInterface constructor calls this to
 * create type factories for the benefit of Eclipse/CDT. Each CMPD process has its own
 * {@link EngineInterface} and each must have its own type factory because it is associated with
 * the ICDITarget.
 *
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ITypeFactoryFactory {
    public ITypeFactory makeTypeFactory();
}
