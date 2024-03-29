/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Synopsys -- MetaWare debugger integration
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model; 

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;
 
/**
 * A value containing an array of variables.
 */
public class CIndexedValue extends AbstractCValue implements IIndexedValue {

	/**
	 * The underlying CDI value.
	 */
	private ICDIArrayValue fCDIValue;

	/**
	 * Child variables. Only the variables from loaded partitions will be held.
	 * Use map instead of a java array to scale for large number of children. 
	 */
	private Map<Integer, IVariable> fVariables;

	/**
	 * The index of the first variable contained in this value.
	 */
	private int fOffset;

	/**
	 * The number of entries in this indexed collection.
	 */
	private int fSize;

	/**
	 * The type of this value.
	 */
	private ICType fType;

	/** 
	 * Constructor for CIndexedValue. 
	 */
	public CIndexedValue( AbstractCVariable parent, ICDIArrayValue cdiValue, int offset, int size ) {
		super( parent );
		fVariables = new HashMap<Integer, IVariable>(getPreferredPartitionSize());
		fCDIValue = cdiValue;
		fOffset = offset;
		fSize = size;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#setChanged(boolean)
	 */
	protected void setChanged( boolean changed ) {
		for (IVariable var : fVariables.values()) {
			((AbstractCVariable)var).setChanged( changed );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#dispose()
	 */
	public void dispose() {
		for (IVariable var : fVariables.values()) {
			((AbstractCVariable)var).dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#reset()
	 */
	protected void reset() {
		for (IVariable var : fVariables.values()) {
			((AbstractCVariable)var).resetValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCValue#preserve()
	 */
	protected void preserve() {
		resetStatus();
		for (IVariable var : fVariables.values()) {
			((AbstractCVariable)var).preserve();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICValue#getType()
	 */
	public ICType getType() throws DebugException {
		if ( fType == null ) {
			synchronized( this ) {
				if ( fType == null ) {
					try {
						ICDIType cdiType = getCDIValue().getType();
						if ( cdiType != null )
							fType = new CType( cdiType );
					}
					catch( CDIException e ) {
						targetRequestFailed( e.getMessage(), null );
					}
				}
			}
		}
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		ICType type = getType(); 
		return ( type != null ) ? type.getName() : ""; //$NON-NLS-1$
	}

	/**
	 * Please note that this function returns the address of the array, not the
	 * contents of the array, as long as the underlying ICDIArrayValue is an
	 * instance of ICDIPointerValue.  Otherwise, it returns an empty string.
	 * 
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
	    return getValueString(getParentVariable().getFormat());
	}
	
	public String getValueString(CVariableFormat format) throws DebugException {

		if ( fCDIValue instanceof ICDIPointerValue ) {
			try {
				IAddressFactory factory = ((CDebugTarget)getDebugTarget()).getAddressFactory();
				BigInteger pv = ((ICDIPointerValue)fCDIValue).pointerValue();
				if ( pv == null )
					return ""; //$NON-NLS-1$
				IAddress address = factory.createAddress( pv );
				if ( address == null )
					return ""; //$NON-NLS-1$
				if ( CVariableFormat.NATURAL.equals( format ) || CVariableFormat.HEXADECIMAL.equals( format ) )
					return address.toHexAddressString();
				if ( CVariableFormat.DECIMAL.equals( format ) )
					return address.toString();
				if ( CVariableFormat.BINARY.equals( format ) )
					return address.toBinaryAddressString();
				return null;
			} catch (CDIException e) {
				requestFailed( e.getMessage(), null );
			}
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return getVariables0( getInitialOffset(), getSize() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return getSize() > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariable(int)
	 */
	public IVariable getVariable( int offset ) throws DebugException {
		if ( offset >= getSize() ) {
			requestFailed( CoreModelMessages.getString( "CIndexedValue.0" ), null ); //$NON-NLS-1$
		}
		return getVariables0( offset, 1 )[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariables(int, int)
	 */
	public IVariable[] getVariables( int offset, int length ) throws DebugException {
		if ( offset >= getSize() ) {
			requestFailed( CoreModelMessages.getString( "CIndexedValue.1" ), null ); //$NON-NLS-1$
		}
		if ( (offset + length - 1) >= getSize() ) {
			requestFailed( CoreModelMessages.getString( "CIndexedValue.2" ), null ); //$NON-NLS-1$
		}
		return getVariables0( offset, length );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getSize()
	 */
	public int getSize() throws DebugException {
		return getSize0();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getInitialOffset()
	 */
	public int getInitialOffset() {
		return fOffset;
	}

	protected ICDIArrayValue getCDIValue() {
		return fCDIValue;
	}

	private int getPartitionSize( int index ) {
		int psize = getPreferredPartitionSize();
		int size = getSize0();
		int pcount = size/psize + 1;
		if ( pcount - 1 < index )
			return 0;
		return ( pcount - 1 == index ) ? size % psize : psize;
	}

	private int getPartitionIndex( int offset ) {
		return offset / getPreferredPartitionSize();
	}

	private int getPreferredPartitionSize() {
		return 100;
	}

	private IVariable[] getVariables0( int offset, int length ) throws DebugException {
		IVariable[] result = new IVariable[length];
		int firstIndex = getPartitionIndex( offset );
		int lastIndex = getPartitionIndex( offset + Math.max( length - 1, 0 ) );
		for ( int i = firstIndex; i <= lastIndex; ++i ) {
			synchronized( this ) {
				if ( !isPartitionLoaded( i ) ) {
					loadPartition( i );
				}
			}
		}
		for (int i = 0; i < length; i++) {
			result[i] = fVariables.get( offset + i );
		}
		return result;
	}

	private boolean isPartitionLoaded( int index ) {
		return fVariables.containsKey(index * getPreferredPartitionSize());
	}

	private void loadPartition( int index ) throws DebugException {
		int prefSize = getPreferredPartitionSize();
		int psize = getPartitionSize( index );
		ICDIVariable[] cdiVars = new ICDIVariable[0];
		try {
			cdiVars = getCDIValue().getVariables( index * prefSize, psize );
		}
		catch( CDIException e ) {
			requestFailed( e.getMessage(), null );
		}
		for( int i = 0; i < cdiVars.length; ++i )
			fVariables.put(i + index * prefSize, CVariableFactory.createLocalVariable( this, cdiVars[i] ));
	}

	private int getSize0() {
		return fSize;
	}
}
