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
package com.arc.seecode.internal.serialize;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.IEngineAPIObserver;


/**
 * Handles arguments to {@link IEngineAPIObserver} of 
 * type {@link IEngineAPI}. We pass these to the client
 * as <code>null</code>.
 * @author David Pickens
 */
class NullObjectSerializer implements ISerializer {

    private NullObjectSerializer(){}
    private static ISerializer sInstance = new NullObjectSerializer();
    public static ISerializer getInstance() {
        return sInstance;
    }
    /* (non-Javadoc)
     * @see com.arc.seecode.serialize.ISerializer#serialize(java.lang.Object, java.io.DataOutputStream)
     */
    @Override
    public void serialize(Object v, DataOutputStream output) throws IOException {
        if (v != null)
            throw new IllegalArgumentException("Can't serialize IEngineAPI arguments except null!");
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.serialize.ISerializer#deserialize(java.io.DataInputStream)
     */
    @Override
    public Object deserialize(DataInputStream input) throws IOException {
        return null;
    }

}
