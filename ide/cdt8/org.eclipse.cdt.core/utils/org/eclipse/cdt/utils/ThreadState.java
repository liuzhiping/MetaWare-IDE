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
package org.eclipse.cdt.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class implemented to get around bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=224218.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class ThreadState {
    
    private static Set<Thread> blockedThreads = Collections.synchronizedSet(new HashSet<Thread>());
    
    public static void addBlockedThread(Thread t){
        blockedThreads.add(t);
    }
    
    public static boolean isBlocked(Thread t){
        return blockedThreads.contains(t);
    }
    
    public static void removeBlockedThread(Thread t){
        blockedThreads.remove(t);
    }

}
