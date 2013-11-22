package com.arc.cdt.debug.seecode.internal.core.cdi.value;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolValue;

import com.arc.seecode.engine.Value;

class BooleanValue extends IntegerValue implements ICDIBoolValue {

    public BooleanValue(Value value, ICDITarget target) {
        super(value, target);
    }

    @Override
    public long longValue() throws CDIException {
        String v = getSeeCodeValue().getValue();
        if (v != null){
            v = v.toLowerCase();
            if (v.equals("false")) return 0;
            if (v.equals("true")) return 1;
        }
        return super.longValue();
    }

  

}
