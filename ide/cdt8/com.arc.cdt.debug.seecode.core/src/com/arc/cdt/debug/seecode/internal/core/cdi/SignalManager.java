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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;

/**
 * Signal manager that wraps the {@link EngineInterface}.
 * 
 * @author David Pickens
 */
class SignalManager extends Manager  {

    // List<ICDISignal>
    private List<ICDISignal> mSignalList = null;

    // Map<Integer,ICDISignal>
    private Map<Integer,ICDISignal> mSignalMap = null;

    /**
     * @param target the associated target.
     */
    public SignalManager(Target target) {
        super(target, false);
    }

    /* override */
    public ICDISignal[] getSignals() throws CDIException {
        if (mSignalList == null) initSignals();
        return mSignalList.toArray(new ICDISignal[mSignalList.size()]);
    }

    ICDISignal getSignal(int id) throws CDIException{
        if (mSignalList == null) initSignals();
        return  mSignalMap.get(new Integer(id));
    }

    private void initSignals() throws CDIException {
        Target target = getTarget();
        EngineInterface engine = target
                .getEngineInterface();
        try {
            int ids[] = engine.getExceptionIDs();
            mSignalList = new ArrayList<ICDISignal>(40);
            mSignalMap = new HashMap<Integer,ICDISignal>();
            for (int i = 0; i < ids.length; i++) {
                String name = engine.getExceptionName(ids[i]);
                String desc = engine.getExceptionDescription(ids[i]);
                Signal s = new Signal(target, ids[i], name, desc);
                mSignalList.add(s);
                mSignalMap.put(new Integer(ids[i]), s);
            }
        } catch (IllegalArgumentException e) {
            throw new CDIException(e.getMessage());
        } catch (EngineException e) {
            throw new CDIException(e.getMessage());
        }

    }

    static class Signal implements ICDISignal {

        private EngineInterface mEngine;

        private int mID;

        private String mName;

        private String mDesc;

        private boolean mIgnored = false;

        private ICDITarget mTarget;

        private boolean mStopSet = false;

        Signal(Target target, int id, String name, String desc) {
            mTarget = target;
            mEngine = target.getEngineInterface();
            mID = id;
            mName = name;
            mDesc = desc;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDISignal#getName()
         */
        @Override
        public String getName() {
            return mName;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDISignal#getDescription()
         */
        @Override
        public String getDescription() {
            return mDesc;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDISignal#isIgnore()
         */
        @Override
        public boolean isIgnore() {
            return mIgnored;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDISignal#isStopSet()
         */
        @Override
        public boolean isStopSet() {
            return mStopSet;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDISignal#signal()
         */
        @Override
        public void signal() throws CDIException {
            try {
                mEngine.throwException(mID);
            } catch (EngineException e) {
                throw new CDIException(e.getMessage());
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDISignal#handle(boolean,
         *      boolean)
         */
        @Override
        public void handle(boolean ignore, boolean stop) throws CDIException {
            try {
                mEngine.setSuspendOnException(mID,stop);
                mStopSet = stop;
                mEngine.setIgnoreException(mID,ignore);
                mIgnored = ignore;
            } catch (IllegalArgumentException e) {
                throw new CDIException(e.getMessage());
            } catch (EngineException e) {
                throw new CDIException(e.getMessage());
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
         */
        @Override
        public ICDITarget getTarget() {
            return mTarget;
        }

    }

}
