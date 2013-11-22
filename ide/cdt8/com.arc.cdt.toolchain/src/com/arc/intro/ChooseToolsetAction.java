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
package com.arc.intro;

import java.util.Properties;

import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;

/**
 * Called when the user selects the toolset from the introductory page that is dynamically
 * generated from code in {@link CompilerSamplesProvider}.
 */
public class ChooseToolsetAction implements IIntroAction {

    public void run (IIntroSite site, Properties params) {
        String toolset = params.getProperty("toolset");
        if (toolset != null){
            CompilerSamplesProvider.setChosenToolset(toolset);
            IIntroURL link = IntroURLFactory.createIntroURL("http://org.eclipse.ui.intro/showPage?id=categories");
            link.execute();
        }

    }
}
