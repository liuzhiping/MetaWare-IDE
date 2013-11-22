package com.arc.cdt.toolchain;

import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ILanguageInfoCalculator;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;

public class ARCLanguageCalculator implements ILanguageInfoCalculator {

    public ARCLanguageCalculator() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getLanguageName(IResourceInfo rcInfo, ITool tool, IInputType type) {
        if (type.getBaseId().endsWith("CPP")) return "MetaWare C++";
        return "MetaWare C";
    }

    @Override
    public String getLanguageId(IResourceInfo rcInfo, ITool tool, IInputType type) {
        // TODO Auto-generated method stub
        return null;
    }

}
