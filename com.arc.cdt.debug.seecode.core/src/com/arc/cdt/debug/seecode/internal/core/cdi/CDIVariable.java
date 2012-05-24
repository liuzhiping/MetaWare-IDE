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

import java.math.BigInteger;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ChangedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.types.FloatType;
import com.arc.cdt.debug.seecode.internal.core.cdi.types.IntType;
import com.arc.cdt.debug.seecode.internal.core.cdi.value.IValueChangeable;
import com.arc.cdt.debug.seecode.internal.core.cdi.value.ValueFactory;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.Variable;


/**
 * Represent a CDI variable. This class is complicated by the need to handle
 * a variable that is "cast" to another type other than its declared type.
 * 
 * @author David Pickens
 */
class CDIVariable implements ISeeCodeVariable {

    private ISeeCodeVariableDescriptor mDescriptor;

    private ICDIValue mValue = null;
    
    private ICDIType mCastToType = null; // type if this variable was cast to an alternate type.

    private VariableManager mVariableManager;
    
    class CDIFloatValue implements ICDIFloatValue {
    	private float _value;
    	private ICDIType _type;
    	CDIFloatValue(float f){
    		_value = f;
    		_type = new FloatType("float",mDescriptor.getTarget());
    	}
		@Override
        public float floatValue() throws CDIException {	return _value;	}
		@Override
        public double doubleValue() throws CDIException { return _value;}
		@Override
        public String getTypeName() throws CDIException {	return "float";	}
		@Override
        public ICDIType getType() throws CDIException {	return _type;	}
		@Override
        public String getValueString() throws CDIException {return ""+_value;}
		@Override
        public int getChildrenNumber() throws CDIException {return 0;}
		@Override
        public boolean hasChildren() throws CDIException {	return false;}
		@Override
        public ICDIVariable[] getVariables() throws CDIException {return null;}
		@Override
        public ICDITarget getTarget() {	return mDescriptor.getTarget();	}   	
    }
    
    class CDIIntValue implements ICDIIntValue {
    	private int _value;
    	private ICDIType _type;
    	CDIIntValue(int f){
    		_value = f;
    		_type = new IntType("int",true,mDescriptor.getTarget());
    	}
		@Override
        public String getTypeName() throws CDIException {	return "float";	}
		@Override
        public ICDIType getType() throws CDIException {	return _type;	}
		@Override
        public String getValueString() throws CDIException {return ""+_value;}
		@Override
        public int getChildrenNumber() throws CDIException {return 0;}
		@Override
        public boolean hasChildren() throws CDIException {	return false;}
		@Override
        public ICDIVariable[] getVariables() throws CDIException {return null;}
		@Override
        public ICDITarget getTarget() {	return mDescriptor.getTarget();	}
		@Override
        public BigInteger bigIntegerValue() throws CDIException {
			return BigInteger.valueOf(_value);
		}
		@Override
        public long longValue() throws CDIException { return _value;}
		@Override
        public int intValue() throws CDIException { return _value; }
		@Override
        public short shortValue() throws CDIException { return(short)_value; }
		@Override
        public int byteValue() throws CDIException { return _value & 0xFF; }   	
    }

    /**
     * 
     */
    public CDIVariable(ISeeCodeVariableDescriptor vd, VariableManager vmgr) {
        mDescriptor = vd;
        mVariableManager = vmgr;
        mValue = null;
    }

    @Override
    public ISeeCodeVariableDescriptor getDescriptor() {
        return mDescriptor;
    }

    /**
     * @param varDesc
     * @return whether or not this descriptor matches another.
     */
    @Override
    public boolean equals(ICDIVariableDescriptor varDesc) {
        return varDesc == this || mDescriptor.equals(varDesc);
    }

    /**
     * @return the variable's name.
     */
    @Override
    public String getName() {
        return mDescriptor.getName();
    }

    /**
     * @return the fully-qualified name.
     * @throws CDIException
     */
    @Override
    public String getQualifiedName() throws CDIException {
        return mDescriptor.getQualifiedName();
    }

    /**
     * @return the associated target.
     */
    @Override
    public ICDITarget getTarget() {
        return mDescriptor.getTarget();
    }

    /**
     * @return the variable's type.
     * @throws CDIException
     */
    @Override
    public ICDIType getType() throws CDIException {
    	//<HACK>
    	// If the variable is being cast to an althernate type, we don't have an engine API call to
    	// compute a type descriptor from a type name. Thus, we cache the cast type from the value
    	// of the variable when the value is requested.
    	// Thus, the cast type will not be valid until the variable's value is retrieved.
    	// Seems to work in practice in regard to variable display.
    	// </HACK>
    	if (mCastToType != null && getCastType() != null) return mCastToType;
        return mDescriptor.getType();
    }

    /**
     * @return the name of the variable's type.
     * @throws CDIException
     */
    @Override
    public String getTypeName() throws CDIException {
        return mDescriptor.getTypeName();
    }

    /**
     * @param start
     * @param length
     * @return a descriptor that represents a slice of this one, assuming it to
     *         be an array.
     * @throws CDIException
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsArray(int start,
            int length) throws CDIException {
        return mDescriptor.getVariableDescriptorAsArray(start, length);
    }

    /**
     * @param type
     * @return this variable cast with an alternate type.
     * @throws CDIException
     */
    @Override
    public ICDIVariableDescriptor getVariableDescriptorAsType(String type)
            throws CDIException {
        return mDescriptor.getVariableDescriptorAsType(type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return mDescriptor.hashCode();
    }

    /**
     * @return size of this vairable
     * @throws CDIException
     */
    @Override
    public int sizeof() throws CDIException {
        return mDescriptor.sizeof();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return mDescriptor.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#isEditable()
     */
    @Override
    public boolean isEditable() throws CDIException {
        return true;
    }
    
    static boolean isArray(ICDIType type){
    	return type instanceof ICDIArrayType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
     */
    @Override
    public ICDIValue getValue() throws CDIException {

		String castType = mDescriptor.getCastType();
		if (castType == null && !isArrayPartition()) {
			Variable cookie = mDescriptor.getSeeCodeCookie();
			if (mValue == null && cookie.getValue() != null) {
				mValue = ValueFactory.makeValue(this, cookie.getValue(), getTarget(), cookie
						.getStackFrame());
			}
			mCastToType = null;
		} else {
			// Locate a stackframe to evaluate the qualified expression.
			// The last stackframe may be dead; we grab the one in the SeeCode "Variable" object.
			// It will have recently been updated.
			// Having out-of-date cached stackframes is indicative of an engineering problem that
			// we need to address.
			StackFrame sf = (StackFrame) mDescriptor.getStackFrame();
			StackFrameRef ssf;
			if (sf  != null && sf.getSeeCodeStackFrame().isValid()) {
				ssf = sf.getSeeCodeStackFrame();
			}
			else {
				ssf = mDescriptor.getSeeCodeCookie().getStackFrame();
				if (!ssf.isValid()) ssf = null;
			}
			// First try "(casttype)(expr)". If that fails, then try:
			// *(casttype*)(&expr)
			// if that fails, then emit "(casttype)(value)"

			Value v = null;
			String expr = mDescriptor.getQualifiedName();
			try {
				if (ssf != null) {
					v = ssf.evaluate(expr);
				} else {
					EngineInterface e = ((Target) getTarget()).getEngineInterface();
					v = e.evaluate(expr, 0);
				}
			} catch (EngineException e) {
				throw new CDIException(e.getMessage());
			} catch (EvaluationException e) {
				String msg = e.getMessage();
				if (msg.indexOf(expr) < 0){  // If expression isn't in message; add it.
					msg = expr + ": " + msg;
				}
			    throw new CDIException(msg);
			}
			if (mValue == null || mValue.getChildrenNumber() != v.getElementCount() ||
					!(mValue instanceof IValueChangeable))
		    	mValue = ValueFactory.makeValue(this, v, getTarget(), ssf);
			else
				((IValueChangeable)mValue).setValue(v);
			mCastToType = mValue.getType();
		}
		return mValue;
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(java.lang.String)
	 */
    @Override
    public void setValue(String expression) throws CDIException {
        Target target = (Target) getTarget();
        EngineInterface engine = target.getEngineInterface();
        Variable cookie = mDescriptor.getSeeCodeCookie();
        try {
            engine.setVariableValue(cookie, expression);
            cookie.update(); // re-read it to make sure we see true value.
            setValue(cookie.getValue());
        } catch (EngineException e) {
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            throw new CDIException(e.getMessage());
        }
        EventManager emgr = (EventManager) target.getSession()
                .getEventManager();
        ChangedEvent varChange = new ChangedEvent(this);
        if (cookie.getRegister() >= 0) {
            // Variable is mapped to a register,
            // we need to fire an event to indicate
            // that register changed also.
            RegisterManager rmgr = target.getRegisterManager();
            rmgr.update(target);
        }
        emgr.enqueueEvent(varChange);
        // In case we have memory display that it
        // showing the variable's contents, update.
        MemoryManager mmgr = target.getMemoryManager();
        if (mmgr.isAutoUpdate())
            mmgr.update(target);
    }

    /**
     * @param v
     */
    private void setValue(Value v) throws CDIException {
        if (mValue instanceof IValueChangeable) {
            ((IValueChangeable) mValue).setValue(v);
        }
        else mValue = null; // force recomputation
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(org.eclipse.cdt.debug.core.cdi.model.ICDIValue)
     */
    @Override
    public void setValue(ICDIValue value) throws CDIException {
        setValue(value.getValueString());
        mValue = value;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#dispose()
     */
    @Override
    public void dispose() throws CDIException {
        mVariableManager.destroyVariable(this);
    }

    @Override
    public Variable getSeeCodeCookie() {
        return mDescriptor.getSeeCodeCookie();
    }

    @Override
    public void setSeeCodeCookie(Variable v) throws CDIException {
        if (getSeeCodeCookie() != v) {
            mDescriptor.setSeeCodeCookie(v);
            setValue(v.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.cdt.debug.seecode.core.cdi.IRefresh#refresh()
     */
    @Override
    public boolean refresh(List<ICDIObject> listToUpdate) throws CDIException {
        try {
            Variable cookie = mDescriptor.getSeeCodeCookie();
            boolean changed = cookie.update();
            
            if (cookie.isActive()) { // may have gone out of scope
            	// If a cast or an array representation, the base variable may have already been updated.
                // Make it recompute, since it is an expression evaluation.
                if (!isOrdinaryVariable()) {
                	getValue(); // Force recomputation            	
                }
                if (changed) {   
                	if (isOrdinaryVariable()){
                		// This is what we did before supporting casts and array-representation
                		// in Variable view.
                        setValue(cookie.getValue());
                	}
                	else {
                	    // This is required if we are an pointer variable converted to array
                		// or a cast variable. We want to see the result value properly.
                		// already changed.
                	}
                    //To get things to work correctly in highlighting array changes,
                    // we fire a change event for the entire array even though
                    // only an element changed.
                    // The CDT 3.1 CVariable stuff for managing this is a mess.
                    // The Eclipse 3.2 variable viewer was seeing orphaned elements
                    // and everything else. So it had to be modified also.
                    listToUpdate.add(this);
                }
                if (mValue instanceof IRefresh) {
                    // Now make change event for any individual element.
                    if (((IRefresh) mValue).refresh(listToUpdate))
                        changed = true;
                } 
            }
            return changed;
        } catch (EngineDisconnectedException e) {
            // Displays can be updating as the engine shutsdown.
            // Don't issue an error.
            return true;
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /**
     * Indicate whether or not this variable is "ordinary. That is, it isn't something that
     * the Variables view is attempting to cast or display a pointer as an array.
     * @return
     */
	private boolean isOrdinaryVariable() {
		return !this.isArrayPartition() && this.getCastType() == null;
	}

    /**
     * @return whether or not this variable is out of bounds
     */
    @Override
    public boolean isOutOfScope() {
        return mDescriptor.isOutOfScope();
    }

    @Override
    public ICDIStackFrame getStackFrame() throws CDIException {
        return mDescriptor.getStackFrame();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariable)
     */
    @Override
    public boolean equals(ICDIVariable variable) {
        if (variable instanceof CDIVariable) {
            CDIVariable cvar = (CDIVariable) variable;
            return cvar.mDescriptor.equals(mDescriptor);
        }
        return false;
    }
    

	@Override
    public String getCastType() {
		return mDescriptor.getCastType();
	}

    @Override
    public ISeeCodeVariable allocateVariable () {
        return this;
    }
    
    protected VariableManager getVariableManager(){
        return mVariableManager;
    }

	@Override
    public boolean isArrayPartition() {
		return mDescriptor.isArrayPartition();
	}
}
