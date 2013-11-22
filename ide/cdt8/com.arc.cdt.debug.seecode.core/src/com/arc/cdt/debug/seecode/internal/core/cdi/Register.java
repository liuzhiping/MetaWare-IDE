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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntegralType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.IRawValue;
import org.eclipse.core.runtime.IAdaptable;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ChangedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.value.IValueChangeable;
import com.arc.cdt.debug.seecode.internal.core.cdi.value.ValueFactory;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.Format;
import com.arc.seecode.engine.IAggregateAccessor;
import com.arc.seecode.engine.RegisterContent;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Value;
import com.arc.seecode.engine.type.IType;
import com.arc.seecode.engine.type.ITypeFactory;

/**
 * An object that references a register identity. 
 * <B>NOTE:</B> we implement <code>ICDIVariable</code> because CDT's
 * register display won't update otherwise. The view subclasses the
 * variable view and expects its elements to implement <code>ICDIVariable</code>.
 * 
 * @author David Pickens
 */
class Register extends RegisterDescriptor implements ICDIRegister, ICDIVariable, IAggregateAccessor, IAdaptable{

    private ICDIValue mValue = null;

    private RegisterContent mContent = null;

    private ICDIRegisterDescriptor mDesc;

    private StackFrameRef mSF;

    private RegisterManager mRegManager;

    private ICDIType mType;

    Register(RegisterDescriptor rdesc,  RegisterManager rmgr) {
        super(rdesc.getTarget(), rdesc.getName(),rdesc.getID());
        mDesc = rdesc;
        mSF = null;
        mRegManager = rmgr;
    }
    
    public ICDIRegisterDescriptor getDescriptor(){
        return mDesc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#isEditable()
     */
    @Override
    public boolean isEditable() throws CDIException {
        return true;
    }

//    String getValueString() throws CDIException {
//        if (mValue == null) update();
//        if (mValueString == null && mValue != null) { return mValue
//                .getValueString(); }
//        return mValueString;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
     */
    @Override
    public ICDIValue getValue (ICDIStackFrame context) throws CDIException {
        // CDT has been known to pass null under rare conditions.
        if (context == null) return null;
        StackFrameRef oldSF = mSF;
        mSF = ((StackFrame)context).getSeeCodeStackFrame();
        if (mValue == null || oldSF != mSF){
            update(mSF);
        }
        // Since the UI is presumably looking at this register,
        // record it in the register manager so that we can
        // fire change events when the register changes.
        // But this only concerns us for top-level stackframes.
        if (mSF.isTopMostFrame()){
            mRegManager.record(this,context.getThread());
        }
        return mValue;
    }
    
//    /**
//     * Refresh the value of this register from the engine's copy.
//     * @return true if the value changed.
//     * @throws CDIException
//     */
//    boolean update () throws CDIException {
//        try {
//            String oldValue = mValueString;
//            String value = mSF.getRegisterValue(getID(),mFormat);
//            setValueString(value);
//            return !value.equals(oldValue);
//        }
//        catch (EngineException e) {
//            throw new CDIException(e.getMessage());
//        }
//    }
    
    /**
     * Update register with a new stackframe.
     * @param sf
     * @return true if value changed.
     * @throws CDIException
     */
    boolean update(StackFrameRef sf) throws CDIException{
        try {
            return update(sf,sf.getRegisterContent(getID()));
        }
        catch (EngineException e) {
            throw new CDIException(e.getMessage());
        }
    }
    
    /**
     * Update register with a new value.
     * @param sf
     * @param newValue the latest value received from the engine for this register.
     * @return true if value changed.
     * @throws CDIException
     */
    boolean update(StackFrameRef sf, RegisterContent newValue) throws CDIException{
        mSF = sf;
        RegisterContent oldValue = mContent;
        setContent(newValue);
        return !newValue.equals(oldValue);
    }

    private EngineInterface getEngine(){
        return ((Target)getTarget()).getEngineInterface();
    }

    /**
     * Set value from what was retreived from the engine..
     * 
     * @param content the register content.
     */
    public void setContent(RegisterContent content) throws CDIException {
        Value v = new Value(this);
        mContent = content;
        ITypeFactory typeFactory = getEngine().getTypeFactory();
        if (content.isScalar()){
            v.setSimpleValue("0x" + content.toString(Format.HEXADECIMAL));
            //Our type factory implements ICDIType
            if (mType == null || !(mType instanceof ICDIIntegralType)) {
                mType = (ICDIType)typeFactory.createInteger(null,content.getLength(),false);
            }
        }
        else if (content.isSpecial()){
            v.setSimpleValue(content.getSpecialValue());
        }
        else {
            // Any non-zero value will work for the cookie.
            int dimension = content.getLength() / content.getUnitSize();
            v.setElements(1, dimension);
            if (mType == null || !(mType instanceof ICDIArrayType)) {
                IType baseType = typeFactory.createInteger(null, content.getUnitSize(), true);
                mType = (ICDIType)typeFactory.createArray("VECTOR",baseType,dimension);
            }
        }
        if (mValue == null) {
            v.setType((IType)mType);
            mValue = ValueFactory.makeValue(this, v, getTarget(), mSF,ICDIFormat.HEXADECIMAL);
        }
        else {
           // A bug (feature?) of the register display refresh
            // is that it has cached the "CDIValue" for this
            // register and doesn't refresh it! Therefore,
            // if we change the value of this register, then
            // we must not recompute a new CDIValue. Rather,
            // we zap its contents.
            ((IValueChangeable)mValue).setValue(v);
        }
    }
    
    @Override
    public ICDIType getType () throws CDIException {
        if (mType != null)
            return mType;
        return super.getType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(java.lang.String)
     */
    @Override
    public void setValue(String expression) throws CDIException {
        if (mSF == null) return;
        try {
             mSF.setRegisterValue(getID(),expression);
             setContent(mSF.getRegisterContent(getID()));
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            throw new CDIException(e.getMessage());
        }
        EventManager emgr = (EventManager)getTarget().getSession().getEventManager();
        emgr.enqueueEvent(new ChangedEvent((ICDIRegister)this));
        //Update the variable manager in case
        // one of the varibles is mapped to register
        // that was just changed.
        ((Target)getTarget()).getVariableManager().update((Target)getTarget());
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(org.eclipse.cdt.debug.core.cdi.model.ICDIValue)
     */
    @Override
    public void setValue(ICDIValue value) throws CDIException {
       setValue(value.getValueString());
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#dispose()
     */
    @Override
    public void dispose() throws CDIException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariable)
     */
    @Override
    public boolean equals(ICDIRegister variable) {
        if (variable instanceof Register){
            Register r = (Register)variable;
            return r.getID() == getID() &&
                   r.mSF == mSF;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIVariable)
     */
    @Override
    public boolean equals(ICDIVariableDescriptor variable) {
        if (variable instanceof Register){
            Register r = (Register)variable;
            return r.getID() == getID() &&
                   r.mSF == mSF;
        }
        return false;
    }

	@Override
    public ICDIValue getValue() throws CDIException {
		return mValue; // need stackframe, actually
	}

	@Override
    public boolean equals(ICDIVariable variable) {
		return variable instanceof ICDIRegister &&
			equals((ICDIRegister)variable);
	}

    @Override
    public void freeValueCookie (int cookie) throws EngineException {
        
    }

    @Override
    public Value getValueElement (int cookie, int elementIndex) throws EngineException {
        long v;
        try {
            switch (mContent.getUnitSize()){
                case 1: v = ((byte[])mContent.getValueAsObject())[elementIndex] & 0xFF;break;
                case 2: v = ((short[])mContent.getValueAsObject())[elementIndex] & 0xFFFF; break;
                case 4: v = ((int[])mContent.getValueAsObject())[elementIndex] & 0xFFFFFFFFL; break;
                case 8: v = ((long[])mContent.getValueAsObject())[elementIndex]; break;
                default: {
                    Value value = new Value(this);
                    value.setSimpleValue("<Bad unit size>");
                    return value;
                }
            }
        }
        catch (ClassCastException e) {
            Value value = new Value(this);
            value.setSimpleValue(e.getMessage());
            return value;
        }
        Value value = new Value(this);
        String s = Long.toHexString(v);
        if (s.startsWith("-")) s = "-0x" + s.substring(1);
        else s = "0x" + s;
        value.setSimpleValue(s);
        return value;
    }

    @Override
    public void setValueElement (int cookie, int elementIndex, String newValue, int frameID) throws EngineException,
        EvaluationException {
        // @todo Auto-generated method stub
        
    }
    
    @Override
    @SuppressWarnings({ "rawtypes" })
    public Object getAdapter(Class klass){
        if (klass == IRawValue.class && this.mContent != null){
            return new MyRawValue(mContent);
        }
        if (klass == ICDIRegister.class)
            return this;
        return null;
    }
    
    static class MyRawValue implements IRawValue {

        private RegisterContent mContent;

        MyRawValue(RegisterContent content) {
            mContent = content;
        }

        @Override
        public int getLength () {
            return mContent.getLength();
        }

        @Override
        public int getUnitSize () {
            return mContent.getUnitSize();
        }

        @Override
        public Object getValueObject () {
            return mContent.getValueAsObject();
        }

        @Override
        public boolean isScalar () {
            return mContent.isScalar();
        }

        @Override
        public boolean isValid () {
            return mContent.isValid();
        }

        @Override
        public String getSpecialValue () {
            return mContent.getSpecialValue();
        }

        @Override
        public boolean isSpecial () {
            return mContent.isSpecial();
        }

        @Override
        public long getValue () {
            return mContent.getValue();
        }
    }



}
