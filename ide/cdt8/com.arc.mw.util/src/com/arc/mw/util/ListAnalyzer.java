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
package com.arc.mw.util;

import java.util.List;

/**
 * A utility class for comparing a list of strings with
 * a previous version of the list, and then to indicate which
 * elements are new and which were deleted.
 * @author David Pickens
 */
public class ListAnalyzer {
    private String[] _list;
    private boolean[] _newElements;
    private boolean[] _followsDeleted;
    
    
    /**
     * 
     */
    public ListAnalyzer() {
    }
    
 
    
    public void analyze(List<String> newList, List<String> oldList){
        int size = Math.max(newList.size(),oldList.size());
        _newElements = new boolean[size];
        _followsDeleted = new boolean[size];
        
        for (int i = 0; i < newList.size(); i++){
            String e = newList.get(i);
            if (!oldList.contains(e)) {
                _newElements[i] = true;
            }
        }
        
        boolean lastDeleted = false;
        for (int i = 0; i < oldList.size(); i++){
            if (!newList.contains(oldList.get(i))){
                lastDeleted = true;               
            }
            else {
                if (lastDeleted){
                    int index = newList.indexOf(oldList.get(i));
                    if (index == 0 || !_newElements[index-1])
                        _followsDeleted[index] = true;
                }
                lastDeleted = false;
            }
        }
        
        _list =  newList.toArray(new String[newList.size()]);
    }
    
    public int getElementCount(){
        return _list.length;
    }
    
    public String getElement(int i){
        return _list[i];
    }
    
    public boolean isNew(int i){
        return _newElements[i];
    }
    
    public boolean followsDeletedElement(int i){
        return _followsDeleted[i];
    }

}
