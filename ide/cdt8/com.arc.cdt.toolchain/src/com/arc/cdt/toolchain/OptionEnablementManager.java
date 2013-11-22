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
package com.arc.cdt.toolchain;

import java.util.ArrayList;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;



public class OptionEnablementManager extends AbstractOptionEnablementManager {

    public static final String MAP_OPTION_ID = "com.arc.cdt.toolchain.linker.option.map";
    private static final String PREFIX = "com.arc.cdt.toolchain.linker.option";
    private static final String HEAP_SIZE = "arc.compiler.options.heapsize";
    private static final String HEAP_OPTION = "arc.compiler.options.heap";
    private static final String STACK_SIZE = "arc.compiler.options.stacksize";
    private static final String STACK_OPTION = "arc.compiler.options.stack";
    
    private static final String TOGGLES_CATEGORY = "com.arc.cdt.toolchain.option.compiler.toggles";
    
    
    
    private static final String[] MAPFILE_OPTION_IDS = {
        PREFIX + ".globals",
        PREFIX + ".crossref",
        PREFIX + ".sections",
        PREFIX + ".unmangle",
        PREFIX + ".tables",
        PREFIX + ".symbols",
        PREFIX + ".functions",
        PREFIX + ".crossfunc",
        PREFIX + ".size",
        "com.arc.cdt.toolchain.crossref", // misnamed      
    };
    
    private static final String ANSI_MODE = "arc.compiler.options.ansi";
    private static final String[] NONANSI_OPTIONS = {
    	"arc.compiler.options.pcc",
    	"arc.compiler.options.ptrscompat",
    	"arc.compiler.options.ptrint",
    	"arc.compiler.options.char_is_rep"   	
    };
    
    
    private static final String[] DISABLED_FOR_CLANG = {
        "arc.compiler.options.c++.ecpp",   // -Hecpp
        "arc.compiler.options.c++.bool",   // -Hoff=bool
        "arc.compiler.options.c++.forloop", // -Hoff=for_dcln_local
        "arc.compiler.options.ptrscompat",    // -Hon=pointers_compatible
        "arc.compiler.options.ptrint",    // -Hon=pointers_compatible_with_ints
        "arc.compiler.options.unsignedchar", // "-Hoff=char_default_unsigned
        "arc.compiler.options.char_is_rep", //  -Hon=char_is_rep
        "arc.compiler.options.pcc"          // -Hpcc
    };
    
    /**
     * The ID of the -Coutput option. Some targets (e.g. ARC EM4) have a different default and
     * are forced to override it.
     */
    private String MapOptionID = MAP_OPTION_ID;
    
    public OptionEnablementManager(){
        addObserver(new Observer());
    }
    
    /**
     * Change the name of the linker mapfile option to accomodate such things
     * as ARC em4 which overrides it to change its defaults.
     * @param id the new option ID for the linker mapfile option.
     */
    void setMapOptionID(String id){
    	MapOptionID = id;
    	doMapOptions(MapOptionID);
    }
    
    class Observer implements IObserver {
        Observer(){
            doMapOptions(MapOptionID);
        }

        @Override
        public void onOptionValueChanged (IOptionEnablementManager mgr, String optionId) {
            // If linker map requested or not requested, then enable/disable
            // related options.
            if (optionId.equals(MapOptionID)){
                doMapOptions(optionId);     
            }
            else
            // If -pg, then turn corresponding linker option
            if (optionId.endsWith(".call_graph")){
                setOptionValue("arc.link.options.profiling",mgr.getValue(optionId));
            }
            else if (optionId.endsWith(".keepasm")){
                // -keepasm enabled -Hanno
                boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
                setEnabled("arc.compiler.options.anno",v) ;            
            }
            else if (optionId.equals(ANSI_MODE)){
            	boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
                for (String o: NONANSI_OPTIONS){
            		setEnabled(o,!v);
            	}
            }
            else if (optionId.equals(HEAP_OPTION)){
            	boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
            	setEnabled(HEAP_SIZE, v);
            }
            else if (optionId.equals(STACK_OPTION)){
            	boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
            	setEnabled(STACK_SIZE, v);
            }
            else {
            	boolean ansiPermitted = true;
            	for (String o: NONANSI_OPTIONS){
            		if (Boolean.TRUE.equals(mgr.getValue(o))){
            			ansiPermitted = false;
            			break;
            		}
            	}
            	setEnabled(ANSI_MODE,ansiPermitted);           	
            }
            
            // Get suffix of option and make sure all with same suffix
            // are set to same value.
            // For example "arc.asm.options.arc5core" and
            // "arc.compiler.options.arc5core" must match.
            // HACK: except for ".level". We don't want optimization level to be mistaken for
            // debug level!
            Object v = mgr.getValue(optionId);
            if (v instanceof String || v instanceof Boolean) {
                String suffix = getSuffixOf(optionId);
                //Make copy to avoid occasional ConcurrentModificationException
                for (String id : new ArrayList<String>(getOptionIds())) {
                    if (suffix.equals(getSuffixOf(id)) && !id.equals(optionId) && !suffix.equals("level")) {
                        setOptionValue(id, v);
                    }
                }
            }
        }

        @Override
        public void onOptionEnablementChanged (IOptionEnablementManager mgr, String optionID) {
            // @todo Auto-generated method stub          
        }  
    }

    /**
     * Enable/disable the linker map options appropriately.
     * @param mgr the enablement manager.
     * @param optionId the name of the linker map option.
     */
    private void doMapOptions (String optionId) {
        boolean v = Boolean.TRUE.equals(getValue(optionId));
        for (String id: MAPFILE_OPTION_IDS){
           setEnabled(id,v);
        }
    }
    
    protected static String getSuffixOf (String id) {
        int lastDot = id.lastIndexOf('.');
        if (lastDot >= 0) {
            return id.substring(lastDot + 1);
        }
        return id;
    }
    
    public  boolean isApplicableToToolChain(IOption opt){
        String id = opt.getBaseId();
        if (isClangBased()) {
            for (String s: DISABLED_FOR_CLANG){
                if (s.equals(id)) 
                	return false;
            }
            // None of the "Esoteric toggles" applies to clang
            if (opt.getCategory() != null && TOGGLES_CATEGORY.equals(opt.getCategory().getBaseId())){
                return false;
            }
        }
        return true;
    }


    @Override
    protected void setClangBased(boolean v) {
        if (isClangBased() != v) {
            super.setClangBased(v);
            for (String s: DISABLED_FOR_CLANG){
                setEnabled(s,!v);
            }
            IToolChain tc = getToolChain();
            //Deal with "Esoteric toggles". None apply for ccac
            if (tc != null){
                for (ITool tool: tc.getTools()){
                    for (IOption option: tool.getOptions()){
                        if (option.getCategory() != null && TOGGLES_CATEGORY.equals(option.getCategory().getBaseId())){
                            setEnabled(option.getBaseId(),!v);
                        }
                    }
                }
            }
        }     
    }
    
}
