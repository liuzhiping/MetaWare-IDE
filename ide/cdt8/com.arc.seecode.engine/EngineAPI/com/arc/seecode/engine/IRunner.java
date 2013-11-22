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
 * A wrapper for invoking code in such a way as to
 * place it in an arbitrary thread.
 * <P>
 * Under SWT, we want to invoke the code in the UI thread, but if
 * we do this synchronously, we could end up deadlocking. But sometimes
 * we must do it asynchronously because a result is being created.
 * So, pass  flag to indicate whether or not we can invoke asynchronously.
 * @author David Pickens
 */
public interface IRunner {
    public void invoke(Runnable run, boolean async) throws Throwable;
}
