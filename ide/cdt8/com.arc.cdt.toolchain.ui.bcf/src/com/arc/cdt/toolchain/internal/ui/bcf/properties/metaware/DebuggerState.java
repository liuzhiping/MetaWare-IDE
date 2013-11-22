package com.arc.cdt.toolchain.internal.ui.bcf.properties.metaware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DebuggerState {
    private Map<String,String> guihiliProps;
    private List<String> args;
    private boolean dirty;

    public DebuggerState(Map<String,String> guihili, List<String> args) {
        this.guihiliProps = new HashMap<String,String>(guihili);
        this.args = new ArrayList<String>(args);
        this.dirty = false;
    }
    
    public static boolean isTrue(String v){
        if (v == null) return false;
        if (v.equals("0")) return false;
        if (v.equals("1")) return true;
        v = v.toLowerCase();
        if (v.equals("true")) return true;
        return false;
    }
        
    public void doBoolean(String key,String value, String arg){
        doValue(key,value,"0",arg);
    }
    
    public void removeArgs(String items[]){
        args.removeAll(Arrays.asList(items));
    }
    
    private void removeArg(String arg) {
        if (arg.endsWith("=")){
            for (int i = 0, e = args.size(); i != e; ++i){
                if (args.get(i).startsWith(arg)){
                    args.remove(i);
                    break;
                }
            }
        }
        else args.remove(arg);
    }
    
    public void setProperty(String key, String value){
        String old = guihiliProps.get(key);
        if (value == null){
            if (old != null) dirty = true;
            guihiliProps.remove(key);
        }
        else
        if (!value.equals(old)) {
            guihiliProps.put(key, value);
            dirty = true;
        }
    }

    public void doValue(String key, String value, String defaultValue, String arg){
        String oldValue = guihiliProps.get(key);
        if (value == null){
            if (oldValue == null) return;
        }
        else
        if (value.equals(oldValue)) return;
        if (value != null) {
            guihiliProps.put(key, value);
            if (arg != null) {
                removeArg(arg);
                if (!value.equals(defaultValue)){
                    if (arg.endsWith("="))
                        args.add(arg + value);
                    else
                        args.add(arg);
                }
            }
        }
        else {
            guihiliProps.remove(key);
        }
        dirty = true;      
    }
    
    public void setArg(String arg, String value){
        removeArg(arg);
        if (value != null){
            args.add(arg + value);
        }
        else args.add(arg);
    }
    
    public Map<String,String> getProperties() {return guihiliProps; }
    public List<String> getArgs() { return args; }
    
    public boolean isDirty() { return dirty; }

}
