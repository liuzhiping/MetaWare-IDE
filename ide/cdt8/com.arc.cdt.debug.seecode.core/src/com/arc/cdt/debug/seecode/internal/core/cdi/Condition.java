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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.cdi.ICDICondition;


/**
 * @author David Pickens
 */
class Condition implements ICDICondition {
    private static final String[] EMPTY = new String[0];
    private String[] mThreadIDs;
    /**
     * 
     */
    public Condition(int ignoreCount, String expr, String threadIDs[]) {
         mIgnoreCount = ignoreCount;
         if (expr != null){
             // Empty expression means no expression
             if (expr.trim().length() == 0)
                 expr = null;
         }
         mExpr = expr;
         mThreadIDs = threadIDs != null?threadIDs:EMPTY;
    }

    /*override*/
    @Override
    public String getExpression() {
        return mExpr;
    }

    /*override*/
    @Override
    public int getIgnoreCount() {
        return mIgnoreCount;
    }
    
    private String mExpr;
    private int mIgnoreCount;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.ICDICondition#getThreadIds()
     */
    @Override
    public String[] getThreadIds() {
        return mThreadIDs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.ICDICondition#equals(org.eclipse.cdt.debug.core.cdi.ICDICondition)
     */
    @Override
    public boolean equals(ICDICondition cond) {
        if (cond instanceof Condition){
            Condition ccond = (Condition)cond;
            if (ccond.mExpr == null && mExpr != null){
                return false;
            }
            if (ccond.mExpr == null || !ccond.mExpr.equals(mExpr)){
                return false;
            }
            if (ccond.mIgnoreCount != mIgnoreCount)
                return false;
            if (ccond.mThreadIDs.length != mThreadIDs.length){
                return false;
            }
            if (mThreadIDs.length == 0)
                return true;
            Set<String> threads1 = new HashSet<String>(Arrays.asList(mThreadIDs));
            Set<String> threads2 = new HashSet<String>(Arrays.asList(ccond.mThreadIDs));
            return threads1.equals(threads2);           
        }
        return false;
        
    }

}
