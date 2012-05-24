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
package com.arc.seecode.server;

import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.IEnginePoller;


/**
 * A composite poller than makes a collection of processes act like a single one.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class CompositePoller implements IEnginePoller {
    
    private IEnginePoller pollers[];
    private int nextOneToPoll = 0;

    CompositePoller(EngineInterface engines[]){
        pollers = new IEnginePoller[engines.length];
        for (int i = 0; i < pollers.length; i++){
            pollers[i] = (IEnginePoller)engines[i].getAPI();
        }
    }

    @Override
    public void interruptPollDelay () {
        for (IEnginePoller poll: pollers){
            poll.interruptPollDelay();
        }
    }

    // We poll in a round robin.
    @Override
    public boolean poll (boolean delayIfRunning) {
        boolean somethingRunning = false;
        int cnt = 0;
        while (true) {
            if (cnt == pollers.length) break;
            cnt++;
            if (pollers[nextOneToPoll].poll(delayIfRunning)){
                somethingRunning = true;
            }
            nextOneToPoll++;
            if (nextOneToPoll == pollers.length){
                nextOneToPoll = 0;
            }     
        }
        return somethingRunning;
    }

}
