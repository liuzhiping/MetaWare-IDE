package com.arc.cdt.toolchain.ui.bcf;

import org.eclipse.cdt.managedbuilder.core.IToolChain;

import com.arc.cdt.toolchain.internal.ui.bcf.properties.metaware.MetaWareToolChainFlavor;

public class ToolChainFlavorFactory {

    private static IToolChainFlavor metaware = null;

    public static IToolChainFlavor Get(IToolChain tc) throws BadPropertyException {
        if (tc == null)
            throw new IllegalArgumentException("toolchain is null");
        String s = tc.getBaseId();
        if (s.indexOf("com.arc.cdt.toolchain") >= 0){
            s = s.substring(21);
            for (String a: new String[]{".av2",".arc","arc600","a6","a5"}){
                if (s.indexOf(a) >= 0)
                    return Get("ac");
            }
        }
        throw new IllegalStateException("Unrecognized toolchain to convert from:" + tc.getBaseId());
    }
    
    public static IToolChainFlavor Get(String target) throws BadPropertyException {
        if (target == null) return null;
        if (target.equals("ac") || target.equals("ac2") || target.equals("arc")){
            if (metaware == null){
                metaware = new MetaWareToolChainFlavor();
            }
            return metaware;
        }
        return null;
    }

    private ToolChainFlavorFactory() {
        // TODO Auto-generated constructor stub
    }

}
