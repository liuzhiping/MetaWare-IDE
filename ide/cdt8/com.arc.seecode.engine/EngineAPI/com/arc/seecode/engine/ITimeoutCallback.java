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
 * A callback that is invoked when the engine is about to timeout.
 * It gives the client an opportunity to extend the time.
 * 
 */
public interface ITimeoutCallback extends com.arc.seecode.connect.ITimeoutCallback {

}