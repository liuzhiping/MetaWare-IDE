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

import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.value.ValueFactory;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.Variable;


/**
 * An expression is a snippet of code to be evaluated.
 * @author David Pickens
 */
class Expression implements ICDIExpression {
    private Target mTarget;
    private ICDIValue mValue;
    private Value mSeecodeValue;
    private String mExpression;
    private ExpressionManager mManager;
    /**
     * 
     */
    public Expression(Target target, String expression, ExpressionManager emgr) {
        mTarget = target;
        mExpression = expression;
        mManager = emgr;
    }

    
    /**
     * Evaluate the expression and return true
     * if its value has changed.
     * @return whether or not the expression has changed
     * after updating.
     */
    boolean update(List<ICDIObject> changeList) throws CDIException{
        CDIThread thread = mTarget.getThread();
        if (thread != null)
            return update(thread.getTopFrame(),changeList);
        return false;
    }


	private boolean update(StackFrame sf,List<ICDIObject>changeList) throws CDIException {
		Value v;
		//NOTE: because this can be a variable that is an aggregate, we really need to know
		// the parent variable. Engine interface needs to modify "Value" to return parent variable,
		// if applicable. For nwo we hack things as seen below.
        try {
            v = sf.getSeeCodeStackFrame().evaluate(mExpression);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            v = null;
        }
        if (v == null){
            v = new Value(mTarget.getEngineInterface());
            v.setSimpleValue("???");
        }
        if (!v.equals(mSeecodeValue)){
        	ICDIVariable cdiVar = null;
        	StackFrameRef ssf = sf.getSeeCodeStackFrame();
        	//<HACK> see note above.
        	if (v.getElementCount() > 0 && v.hasAddress()){
        		Variable var = ssf.getEngine().getJavaFactory().newVariable();
        		var.setAddress(v.getAddress());
        		var.setStackFrame(ssf);
        		var.setType(v.getType());
        		var.setName(mExpression);
        		var.setValue(v);  
        		CDIVariableDescriptor desc = new CDIVariableDescriptor(mTarget,var,sf,mTarget.getVariableManager());
        		cdiVar = mTarget.getVariableManager().createVariable(desc);
        	}
        	//</HACK>
        	mSeecodeValue = v;
            mValue = ValueFactory.makeValue(cdiVar,v,mTarget,ssf);
            return true;
        }
// Expression display doesn't respond to element changes
//        if (mValue != null) {
//        	for (ICDIVariable element: mValue.getVariables()){
//        		if (element instanceof IRefresh){
//        			if (((IRefresh)element).refresh(changeList)){
//        				changeList.add(element);
//        			}
//        		}
//        	}
//        }
        return false;
	}

 
    @Override
    public String getExpressionText() {
        return mExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIExpression)
     */
    @Override
    public boolean equals(ICDIExpression expr) {
        return expr instanceof Expression && 
                expr.getExpressionText().equals(getExpressionText());
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#getValue(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame)
     */
    @Override
    public ICDIValue getValue(ICDIStackFrame context) throws CDIException {
        update((StackFrame)context,null);
        return mValue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExpression#dispose()
     */
    @Override
    public void dispose() throws CDIException {
        mManager.destroyExpression(this);
        
    }

}
