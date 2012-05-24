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
package com.metaware.guihili.parser;


/**
 * Implementation of {@link org.xml.sax.Locator} for use in SAX processor.
 *
 * @author David Pickens
 * @version April 30, 2002
 */
class SourceLocator implements org.xml.sax.Locator {
    public SourceLocator(SourceRef sref, int line, int column){
	mSref = sref;
	mLine = line;
	mColumn = column;
	}
    public SourceLocator(SourceRef sref, int line){
	this(sref,line,-1);
	}

    @Override
    public int getLineNumber() { return mLine; }
    @Override
    public int getColumnNumber() { return mColumn; }
    @Override
    public String getPublicId() { return ""; }
    @Override
    public String getSystemId() { return mSref.toString(); }

    private int mColumn;
    private SourceRef mSref;
    private int mLine;
    }
