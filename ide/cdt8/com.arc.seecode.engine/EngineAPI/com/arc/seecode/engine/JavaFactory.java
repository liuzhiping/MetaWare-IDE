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
package com.arc.seecode.engine;

/**
 * This interface exists for the benefit of the C++ engine interface
 * so that it can allocate Java-based objects that it can then
 * wrap.
 * 
 * @author David Pickens
 */
public class JavaFactory {
    private EngineInterface mEngine;
    JavaFactory(EngineInterface engine){
        mEngine = engine;
    }
    JavaFactory(){
        mEngine = null;
    }
    /**
     * @param e
     * @nojni
     */
    void setEngineInterface(EngineInterface e){
        mEngine = e;
    }
    public AssemblyRecord newAssemblyRecord(){
        return new AssemblyRecord();
    }
    
    public RegisterContent newRegisterContent(){
        return new RegisterContent();
    }
    
    public Location newLocation(){
        return new Location();
    }
    
    public Value newValue(){
        return new Value(mEngine);
    }
 
    public Variable newVariable(){
        return new Variable(mEngine);
    }
    
    public WatchpointHit newWatchpointHit(){
        return new WatchpointHit();
    }

}
