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
package com.arc.seecode.display;

import com.arc.widgets.IContainer;
import com.arc.widgets.IToolBarBuilder;

/**
 * An object that creates a toolbar builder.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IToolBarBuilderFactory {
    /**
     * Given a container, return a toolbar that will construct a toolbar
     * within the container.
     * @param parent the parent container for the toolbar.
     * @return a toolbar builder
     */
    IToolBarBuilder createToolBarBuilder(IContainer parent);
}
