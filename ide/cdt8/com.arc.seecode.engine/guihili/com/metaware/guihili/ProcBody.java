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
package com.metaware.guihili;

import java.util.List;


/**
 * A procedure body consisting of a Lisp procedure and
 * a symbol-lookup environment
 *
 * @author David Pickens
 * @version May 13, 2002
 */
public class ProcBody {
	public ProcBody(List<Object> body, IEnvironment env) {
		mList = body;
		mEnv = env;
	}

	public List<Object> getBody() {
		return mList;
	}

	public IEnvironment getEnvironment() {
		return mEnv;
	}

	private List<Object> mList;

	private IEnvironment mEnv;
}
