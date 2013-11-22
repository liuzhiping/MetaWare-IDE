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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.internal.core.ISettingsChangeListener;
import org.eclipse.cdt.managedbuilder.internal.core.NotificationManager;
import org.eclipse.cdt.managedbuilder.internal.core.SettingsChangeEvent;


/**
 * CUSTOMIZATION by ARC
 * <P>
 * A convenient base class for all instance of IOptionEnablementManager
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
@SuppressWarnings("restriction")
public abstract class AbstractOptionEnablementManager implements IOptionEnablementManager, ISettingsChangeListener {

   

    private Map<String, Object> mValueMap = new HashMap<String, Object>();

    private Set<String> mDisabledSet = new HashSet<String>();

    private List<IObserver> mObservers = new ArrayList<IObserver>();

    private IResourceInfo mConfig;
    
    private IToolChain mLastToolChain;

    private transient boolean initializing = false;
    
    private boolean clangBased = false;

    private ITool[] mTools;
    
    public AbstractOptionEnablementManager() {
         NotificationManager.getInstance().subscribe(this);
    }
    
    protected IToolChain getToolChain() {
    	return mLastToolChain;
    }
    
    @Override
    public void initialize (IBuildObject config) {
        //cr92699: when the toolchain is changed, CDT still delivers the original configuration!
        // Thus, we are not aware that anything needs to be recomputed.
        // To get around this, we track a change in the toolchain.
        IToolChain toolchain = null;
        if (config instanceof IConfiguration){
            toolchain =((IConfiguration)config).getToolChain();
            config = ((IConfiguration)config).getRootFolderInfo();          
        }
        else if (config instanceof IResourceInfo){
            IConfiguration cfg =((IResourceInfo)config).getParent();
            if (cfg != null)
                toolchain = cfg.getToolChain();          
        }
        if ((config != mConfig || toolchain != mLastToolChain) && config instanceof IResourceInfo) {
            initializing = true;
            try {
                mConfig = (IResourceInfo)config;
                mLastToolChain = toolchain;
                ITool tools[] = mConfig.getTools();
                mTools = tools;
                for (ITool tool : tools) {
                    for (IOption option : tool.getOptions()) {
                        set(option.getBaseId(), option.getValue());
                    }
                }
            }
            finally {
                initializing = false;
            }
        }
    }
    
    protected boolean isClangBased() {return clangBased; }
    protected void setClangBased(boolean v) { clangBased = v; }
    
    /**
     * Return holder and option.
     * <P>
     * NOTE: option.getHolder() is not necessarily accurate!
     * @param id
     * @return holder and option or null.
     */
    private Object[] getOption(String id){
        // We cannot cannot create an ID-to-Option map because the IOption object
        // changes when it becomes dirty!
        if (mTools == null) return null;
        for (ITool tool : mTools) {
            for (IOption option : tool.getOptions()) {
                if (id.equals(option.getBaseId())){
                    return new Object[]{tool,option};
                }
            }
        }
        return null;   
    }

    // Made public so that various subclasses can access this method from each other.
    public void setOptionValue (String id, Object value) {
        // If this is called as side-effect of initializing, we can get
        // NPE and stuff. 
        if (initializing) return;
        Object[] target = getOption(id);
        if (target == null) {
            // Don't complain about unknown ID. If someone dynamically changed
            // the project type, there may be old references that no longer apply.
            //throw new IllegalArgumentException("Unknown option id: " + id);
            return;
        }
        //NOTE: opt.getHolder() is not necessarily accurate!!
        IHoldsOptions h = (IHoldsOptions)target[0];
        IOption opt = (IOption)target[1];
        try {
            if (value instanceof String) {               
                mConfig.setOption(h, opt, (String) value);
            }
            else if (value instanceof Boolean) {
                mConfig.setOption(h, opt, ((Boolean) value).booleanValue());
            }
            else
                throw new IllegalArgumentException("Invalid value to set option " + id + ": " + value);
        }
        catch (BuildException e) {
            throw new IllegalArgumentException("Can't set value for " + id, e);
        }
        catch (ClassCastException e){
            throw new IllegalArgumentException("Cast exception for " + id, e);
        }
    }

    /**
     * Return the set of all options.
     * @return the set of all options.
     */
    protected Set<String> getOptionIds () {
        return mValueMap.keySet();
    }

    /**
     * This is expected to be overridden in subclasses, but to be called from the overriding method.
     * @param optionId the id of the option.
     * @param value the new value of the option.
     */
    @Override
    public void set (String optionId, Object value) {
        Object prev = mValueMap.put(optionId, value);
        if (prev == null || !prev.equals(value)) {
            fireValueChange(optionId);
        }
    }

    @Override
    public Object getValue (String optionId) {
        return mValueMap.get(optionId);
    }

    @Override
    public boolean isEnabled (String optionId) {
        return !mDisabledSet.contains(optionId);
    }

    // public so as to be called by other OptionEnablementManagers
    public void setEnabled (String optionId, boolean v) {
        if (isEnabled(optionId) != v) {
            if (v)
                mDisabledSet.remove(optionId);
            else
                mDisabledSet.add(optionId);
            fireEnablementChange(optionId);
        }
    }

    @Override
    public void addObserver (IObserver observer) {
        synchronized (mObservers) {
            mObservers.add(observer);
        }
    }
    
    @Override
    public void removeObserver (IObserver observer) {
        synchronized (mObservers) {
            mObservers.remove(observer);
        }
    }

    /**
     * Notify observers that the value of an option changed.
     * @param id the id of the option.
     */
    private void fireValueChange (String id) {
        fireChange(id, true);
    }

    /**
     * Notify observers that the enble property of an option changed.
     * @param id the id of the option.
     */
    private void fireEnablementChange (String id) {
        fireChange(id, false);
    }

    private int mNestedChangeLevel = 0;

    private void fireChange (String id, boolean valueChanged) {
        IObserver[] observers;
        synchronized (mObservers) {
            int cnt = mObservers.size();
            if (cnt == 0)
                return;
            observers = mObservers.toArray(new IObserver[cnt]);
        }
        mNestedChangeLevel++;
        try {
        	//TN: mNestedChangeLevel increased from 10 to 20 because we have many level hierachical auto settings
        	// which will increase this value faster than it can be released. Ex: mpy hierachical option settings for ARCV2HS)
            if (mNestedChangeLevel > 20) {
                throw new IllegalStateException("change observers are looping!");
            }
            for (IObserver o : observers) {
                if (valueChanged)
                    o.onOptionValueChanged(this, id);
                else
                    o.onOptionEnablementChanged(this, id);
            }
        }
        finally {
            mNestedChangeLevel--;
        }
    }

    @Override
    public void settingsChanged(SettingsChangeEvent event) {
        IOption option = event.getOption();
        set(option.getBaseId(),option.getValue());
    }

}
