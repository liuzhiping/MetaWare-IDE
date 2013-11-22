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
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;

import com.arc.cdt.debug.seecode.internal.core.cdi.event.ChangedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.CreatedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.DestroyedEvent;

/**
 * Manages snippets of code that are evaluated in a display.
 * 
 * @author David Pickens
 */
class ExpressionManager extends Manager {
    private List<Expression> mList = new ArrayList<Expression>();

    /**
     * @param target the associated target.
     */
    public ExpressionManager(Target target) {
        super(target, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#createExpression(java.lang.String)
     */
    public ICDIExpression createExpression(String name) {
        Expression e = new Expression(getTarget(), name, this);
        mList.add(e);
        ((EventManager) getSession().getEventManager()).enqueueEvent(new CreatedEvent(e));
        return e;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#getExpressions()
     */
    public ICDIExpression[] getExpressions() {
        return mList.toArray(new Expression[mList.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager#destroyExpression(org.eclipse.cdt.debug.core.cdi.model.ICDIExpression)
     */
    public void destroyExpression(ICDIExpression expression) {
        mList.remove(expression);
        ((EventManager) getSession().getEventManager())
                .enqueueEvent(new DestroyedEvent(expression));
    }

    public void destroyExpressions(ICDIExpression[] expressions) {
        ArrayList<ICDIEvent>list = new ArrayList<ICDIEvent>();
        for (ICDIExpression e: expressions){
            if (mList.remove(e)){
                list.add(new DestroyedEvent(e));
            }
        }
        if (list.size() > 0){
        ((EventManager) getSession().getEventManager())
                .enqueueEvents(list.toArray(new ICDIEvent[list.size()]));
        }
    }
    
    public void destroyAllExpressions(){
        if (mList.size() > 0)
            destroyExpressions(mList.toArray(new ICDIExpression[mList.size()]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIManager#update()
     */
    @Override
	public void update(Target target) throws CDIException {
        List<ICDIObject> changeList = new ArrayList<ICDIObject>();
        for (Expression e : mList) {
            if (e.update(changeList)) {
                changeList.add(e);
            }
        }
        if (changeList.size() > 0) {
        	ICDIEvent events[] = new ICDIEvent[changeList.size()];
        	for (int i = 0; i < events.length; i++){
        		events[i] = new ChangedEvent(changeList.get(i));
        	}
            ((EventManager) getSession().getEventManager())
                    .enqueueEvents(events);
        }
    }
}
