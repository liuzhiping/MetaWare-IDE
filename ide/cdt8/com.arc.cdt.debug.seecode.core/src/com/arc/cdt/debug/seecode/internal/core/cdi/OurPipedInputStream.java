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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


/**
 * We customize PipedInputStream to not throw a 
 * "broken pipe" exception if the reader's target suddenly
 * goes away. This prevents stuff being written in
 * error log that is benign.
 *
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
 class OurPipedInputStream extends PipedInputStream {
    OurPipedInputStream(PipedOutputStream output) throws IOException{
        super(output);
    }
    @Override
    public int read(byte b[], int off, int len) throws IOException{
        try {
            return super.read(b,off,len);
        }
        catch (IOException e) {
            //The thread that is reading this
            // will sometimes get a "Pipe broken"
            // exception when when just want it
            // to see an EOF.
            return -1;
        }                     
    }     
}
