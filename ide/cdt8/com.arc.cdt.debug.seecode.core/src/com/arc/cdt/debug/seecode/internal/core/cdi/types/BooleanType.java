package com.arc.cdt.debug.seecode.internal.core.cdi.types;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolType;

import com.arc.seecode.engine.type.IType;

public class BooleanType extends AbstractType implements ICDIBoolType {

    public BooleanType(String name, int size, ICDITarget target) {
        super(name, size, target);
    }

    @Override
    public int getKind() {
        return IType.BOOLEAN;
    }
  
}
