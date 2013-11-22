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
package com.arc.cdt.debug.seecode.internal.ui;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

import com.arc.cdt.debug.seecode.core.ISeeCodeConstants;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;


/**
 * Page for preferences that apply specifically to SeeCode Debugger.
 * <P>
 * NOTE: the bulk of this code was plagiarized from the corresponding GDB version.
 */
public class SeeCodePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPropertyChangeListener {

    /**
     * This class exists to provide visibility to the <code>refreshValidState</code> method and to perform more
     * intelligent clearing of the error message.
     */
    protected class SCIntegerFieldEditor extends IntegerFieldEditor {

        public SCIntegerFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }
        
        /**
         * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
         */
        @Override
        protected void refreshValidState() {
            super.refreshValidState();
        }
        
        void setToolTipText(String tip){
            getLabelControl().setToolTipText(tip);
            getTextControl().setToolTipText(tip);
        }

        /**
         * Clears the error message from the message line if the error message is the error message from this field
         * editor.
         */
        @Override
        protected void clearErrorMessage () {
            if (canClearErrorMessage()) {
                super.clearErrorMessage();
            }
        }
    }
    
    protected class SCBooleanFieldEditor extends BooleanFieldEditor{
        private Button fButton;
        public SCBooleanFieldEditor(String name, String labelText, Composite parent){
            super(name,labelText,parent);
        }
        @Override
        protected Button getChangeControl(Composite parent){
            fButton = super.getChangeControl(parent);
            return fButton;
        }
        /**
         * Materialize checkbox so that we can place listener on it.
         * @return underlying checkbox
         */
        Button getButton(){
            return fButton;
        }
    }

    public class SCPreferenceStore implements IPreferenceStore {

        private IEclipsePreferences fPreferences;
        private boolean fDirty = false;

        private Map<IPropertyChangeListener, IPreferenceChangeListener> fListeners = new HashMap<IPropertyChangeListener, IPreferenceChangeListener>();

        public SCPreferenceStore(IEclipsePreferences pref) {
            fPreferences = pref;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
         */
        @Override
        public void addPropertyChangeListener (final IPropertyChangeListener listener) {
            IPreferenceChangeListener l = new IPreferenceChangeListener() {

                @Override
                public void preferenceChange(PreferenceChangeEvent event) {
                    listener.propertyChange(new PropertyChangeEvent(SCPreferenceStore.this, event.getKey(), event
                        .getNewValue(), event.getOldValue()));
                }
            };
            fListeners.put(listener, l);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
         */
        @Override
        public boolean contains (String name) {
            return getPreferences().get(name,null) != null;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String,
         * java.lang.Object, java.lang.Object)
         */
        @Override
        public void firePropertyChangeEvent (String name, Object oldValue, Object newValue) {
            for (IPropertyChangeListener l : fListeners.keySet()) {
                l.propertyChange(new PropertyChangeEvent(this, name, oldValue, newValue));
            }
        }

      
        @Override
        public boolean getBoolean (String name) {
            return fPreferences.getBoolean(name,false);
        }

        
        @Override
        public boolean getDefaultBoolean (String name) {
            return false;
        }

      
        @Override
        public double getDefaultDouble (String name) {
            return 0.0;
        }

      
        @Override
        public float getDefaultFloat (String name) {
            return 0.0f;
        }

       
        @Override
        public int getDefaultInt (String name) {
            if (name.equals(ISeeCodeConstants.PREF_REQUEST_TIMEOUT)){
                return ISeeCodeConstants.DEF_REQUEST_TIMEOUT;
            }
            if (name.equals(ISeeCodeConstants.PREF_REQUEST_LAUNCH_TIMEOUT)){
                return ISeeCodeConstants.DEF_REQUEST_LAUNCH_TIMEOUT;
            }
            if (name.equals(ISeeCodeConstants.PREF_REQUEST_LOAD_TIMEOUT)){
                return ISeeCodeConstants.DEF_REQUEST_LOAD_TIMEOUT;
            }
            if (name.equals(ISeeCodeConstants.PREF_MAX_ERROR_LOG_SIZE)){
                return ISeeCodeConstants.DEF_MAX_ERROR_LOG_SIZE;
            }
            return -1; // Shouldn't get here
        }

        
        @Override
        public long getDefaultLong (String name) {
            return 0L;
        }

        
        @Override
        public String getDefaultString (String name) {
            return null;
        }

     
        @Override
        public double getDouble (String name) {
            return fPreferences.getDouble(name,0.0);
        }

        @Override
        public float getFloat (String name) {
            return fPreferences.getFloat(name,0);
        }

      
        @Override
        public int getInt (String name) {
            return fPreferences.getInt(name,getDefaultInt(name));
        }

        @Override
        public long getLong (String name) {
            return fPreferences.getLong(name,0L);
        }

     
        @Override
        public String getString (String name) {
            return fPreferences.get(name,null);
        }

       
        @Override
        public boolean isDefault (String name) {
            return fPreferences.get(name,null) == null;
        }

       
        @Override
        public boolean needsSaving () {
            return fDirty;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
         */
        @Override
        public void putValue (String name, String value) {
            if (value == null){
                if (fPreferences.get(name,null) != null) {
                    fPreferences.remove(name);
                    fDirty = true;
                }
            }
            else
                if (!value.equals(fPreferences.get(name,null))){
                    fDirty = true;
                    fPreferences.put(name, value);
                }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
         */
        @Override
        public void removePropertyChangeListener (IPropertyChangeListener listener) {
            fListeners.remove(listener);
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
         */
        @Override
        public void setDefault (String name, double value) {
            throw new UnsupportedOperationException("setDefault");
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
         */
        @Override
        public void setDefault (String name, float value) {
            throw new UnsupportedOperationException("setDefault");
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
         */
        @Override
        public void setDefault (String name, int value) {
            throw new UnsupportedOperationException("setDefault");
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
         */
        @Override
        public void setDefault (String name, long value) {
            throw new UnsupportedOperationException("setDefault");
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
         */
        @Override
        public void setDefault (String name, String defaultObject) {
            throw new UnsupportedOperationException("setDefault");
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
         */
        @Override
        public void setDefault (String name, boolean value) {
            throw new UnsupportedOperationException("setDefault");
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
         */
        @Override
        public void setToDefault (String name) {
            throw new UnsupportedOperationException("setDefault");
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
         */
        @Override
        public void setValue (String name, double value) {
            if (fPreferences.getDouble(name, 0.0) != value){
                fDirty = true;
                fPreferences.putDouble(name,value);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
         */
        @Override
        public void setValue (String name, float value) {
            if (fPreferences.getFloat(name, 0.0f) != value){
                fDirty = true;
                fPreferences.putFloat(name,value);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
         */
        @Override
        public void setValue (String name, int value) {
            if (getDefaultInt(name) == value){
                if (fPreferences.getInt(name,-1) != -1){
                    fPreferences.remove(name);
                    fDirty = true;
                }
            }
            else
            if (fPreferences.getInt(name, getDefaultInt(name)) != value){
                fDirty = true;
                fPreferences.putInt(name,value);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
         */
        @Override
        public void setValue (String name, long value) {
            if (fPreferences.getLong(name, 0) != value){
                fDirty = true;
                fPreferences.putLong(name,value);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
         */
        @Override
        public void setValue (String name, String value) {
            if (value == null){
                if (fPreferences.get(name,null) != null){
                    fPreferences.remove(name);
                    fDirty = true;
                }
            }
            else
            if (!value.equals(fPreferences.get(name, null))){
                fDirty = true;
                fPreferences.put(name,value);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
         */
        @Override
        public void setValue (String name, boolean value) {
            if (fPreferences.getBoolean(name, false) != value){
                fDirty = true;
                fPreferences.putBoolean(name,value);
            }
        }

        protected IEclipsePreferences getPreferences () {
            return fPreferences;
        }
    }

    // Debugger timeout preference widgets
    private SCIntegerFieldEditor fDebugTimeoutText;
    
    // Debugger timeout preference widgets
    private SCIntegerFieldEditor fLoadTimeoutText;
    
    private SCIntegerFieldEditor fErrorLogLimitText;

    // Launch timeout preference widgets
    private SCIntegerFieldEditor fLaunchTimeoutText;

    private SCPreferenceStore fSCCorePreferenceStore = new SCPreferenceStore(SeeCodePlugin.getDefault()
        .getPreferences());

    private SCBooleanFieldEditor fLicensingAlert;

    private SCIntegerFieldEditor fExpirationDays = null;

    private Composite fExpirationDaysParentComposite = null;

    /**
     * Constructor for MIPreferencePage.
     */
    public SeeCodePreferencePage () {
        super();
        setPreferenceStore(fSCCorePreferenceStore);
        setDescription("General Settings for the MetaWare Debugger"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
     */
    @Override
    protected Control createContents (Composite parent) {
        //getWorkbench().getHelpSystem().setHelp(getControl(), IMIHelpContextIds.MI_PREFERENCE_PAGE);
        // The main composite
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);
        createSpacer(composite, 1);
        createCommunicationPreferences(composite);
        
        createErrorLogPreferences(composite);
//        if (false) // Don't make license alert configurable
//            createLicensingPreferences(composite);
//        createEngineOutOfDatePreferences(composite);
        return composite;
    }

    /**
     * Creates composite group and sets the default layout data.
     * @param parent the parent of the new composite
     * @param numColumns the number of columns for the new composite
     * @param labelText the text label of the new composite
     * @return the newly-created composite
     */
    private Composite createGroupComposite (Composite parent, int numColumns, String labelText) {
        return ControlFactory.createGroup(parent, labelText, numColumns);
    }
    
//    private void createEngineOutOfDatePreferences(Composite parent){
//        Composite group = ControlFactory.createGroup(parent,"Debugger Engine Version Strategy",1);
//        fPromptButton = createButton(group,"Prompt user","Prompt the user when the bundled engine is older than the toolkit version",
//            ISeeCodeConstants.ENGINE_VERSION_PROMPT);
//        createButton(group,"Use newest engine","Use which ever engine is the latest.",ISeeCodeConstants.ENGINE_VERSION_USE_LATEST);
//        createButton(group,"Use bundled engine","Use bundled engine regardless of how it compares with the toolkit version",
//            ISeeCodeConstants.ENGINE_VERSION_USE_BUNDLED);
//        createButton(group,"Use installed toolkit engine","Always use the toolkit engine, even if older than bundled version",
//            ISeeCodeConstants.ENGINE_VERSION_USE_TOOLSET);       
//    }
    
//    private Button createButton(Composite parent, String label, String tooltip, final int value){
//        final Button b = new Button(parent,SWT.RADIO);
//        b.setText(label);
//        b.setToolTipText(tooltip);
//        b.addSelectionListener(new SelectionListener(){
//
//            @Override
//            public void widgetDefaultSelected (SelectionEvent e) {
//                // @todo Auto-generated method stub
//                
//            }
//
//            @Override
//            public void widgetSelected (SelectionEvent e) {
//                if (b.getSelection())
//                    SeeCodePreferencePage.this.engineVersionStrategy = value;
//                
//            }});
//        if (this.getSCCorePreferenceStore().getInt(ISeeCodeConstants.PREF_ENGINE_VERSION_MANAGEMENT) == value){
//            b.setSelection(true);
//            this.engineVersionStrategy = value;
//        }
//        else b.setSelection(false);
//        return b;
//    }

    /**
     * @see IPreferencePage#performOk()
     */
    @Override
    public boolean performOk () {
        boolean result = super.performOk();
        storeValues();
        try {
            SeeCodePlugin.getDefault().getPreferences().flush();
        }
        catch (BackingStoreException e) {
            SeeCodePlugin.log(e);
        }
        return result;
    }

    /**
     * Sets the default preferences.
     * @see PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults () {
        setDefaultValues();
        super.performDefaults();
    }

    private void setDefaultValues () {
        fDebugTimeoutText.loadDefault();
        fLoadTimeoutText.loadDefault();
        fLaunchTimeoutText.loadDefault();
        fErrorLogLimitText.loadDefault();
        fPromptButton.setSelection(true);
        this.engineVersionStrategy = ISeeCodeConstants.ENGINE_VERSION_PROMPT;
        
        updateLicensingEnablement();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
     */
    @Override
    public void init (IWorkbench workbench) {
    }

    protected void createSpacer (Composite composite, int columnSpan) {
        Label label = new Label(composite, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalSpan = columnSpan;
        label.setLayoutData(gd);
    }

    private void createCommunicationPreferences (Composite composite) {
        Composite comp = createGroupComposite(composite, 1, "Communication"); //$NON-NLS-1$
        // Add in an intermediate composite to allow for spacing
        Composite spacingComposite = new Composite(comp, SWT.NONE);
        GridLayout layout = new GridLayout();
        spacingComposite.setLayout(layout);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        spacingComposite.setLayoutData(data);
        fLaunchTimeoutText = createTimeoutField(ISeeCodeConstants.PREF_REQUEST_LAUNCH_TIMEOUT, "launch timeout","Launch timeout (sec)", 
            "Maximum wait time for connecting to the debugger engine during launch",spacingComposite); //$NON-NLS-1$
        fLaunchTimeoutText.setPropertyChangeListener(this);
        fDebugTimeoutText = createTimeoutField(ISeeCodeConstants.PREF_REQUEST_TIMEOUT, "response timeout","Engine response timeout (sec)", 
            "Maximum wait time for each debugger engine interaction",spacingComposite); //$NON-NLS-1$
        fDebugTimeoutText.setPropertyChangeListener(this);
        fLoadTimeoutText = createTimeoutField(ISeeCodeConstants.PREF_REQUEST_LOAD_TIMEOUT, "load timeout","Program load timeout (sec)", 
            "Wait time for loading a program before the user is prompted",spacingComposite.getParent()); //$NON-NLS-1$
        fLoadTimeoutText.setPropertyChangeListener(this);

    }
    
    private void createErrorLogPreferences( Composite parent){
        Composite comp = createGroupComposite(parent,1,"Debugger Error Log");
        // Add in an intermediate composite to allow for spacing
        Composite spacingComposite = new Composite(comp, SWT.NONE);
        GridLayout layout = new GridLayout();
        spacingComposite.setLayout(layout);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        spacingComposite.setLayoutData(data);
        fErrorLogLimitText = createIntField(ISeeCodeConstants.PREF_MAX_ERROR_LOG_SIZE,"max error log","Maximum error log size (bytes)",
            "When the debugger error log gets to this size, it will be truncated from the top.",
            spacingComposite,ISeeCodeConstants.MIN_ERROR_LOG_SIZE,ISeeCodeConstants.MAX_ERROR_LOG_SIZE);
        fErrorLogLimitText.setPropertyChangeListener(this);
    }
    
/*    private void createLicensingPreferences (Composite composite) {
 
        Composite comp = createGroupComposite(composite, 1, "Licensing");
        // Add in an intermediate composite to allow for spacing
        Composite spacingComposite = new Composite(comp, SWT.NONE);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        spacingComposite.setLayoutData(data);
        fLicensingAlert = createBooleanField(
            ISeeCodeConstants.PREF_LICENSE_EXPIRATION_ALERT,
            "Alert when license is about to expire",
            spacingComposite); //$NON-NLS-1$
        fLicensingAlert.setPropertyChangeListener(this);
        spacingComposite = new Composite(comp, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 2;
        spacingComposite.setLayoutData(data);
        fExpirationDaysParentComposite = spacingComposite;
        fExpirationDays = createIntField(
            ISeeCodeConstants.PREF_LICENSE_EXPIRATION_DAYS,
            "    Days until expiration",
            spacingComposite,
            0,
            Integer.MAX_VALUE); //$NON-NLS-1$
        fExpirationDays.setPropertyChangeListener(this);
        updateLicensingEnablement();
        fLicensingAlert.getButton().addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected (SelectionEvent e) {
            }

            public void widgetSelected (SelectionEvent e) {
                updateLicensingEnablement();

            }
        });
        
    }*/

//    private SCBooleanFieldEditor createBooleanField (String preference, String label, Composite parent) {
//        SCBooleanFieldEditor field = new SCBooleanFieldEditor( preference, label, parent );
//        field.setPage( this );
//        field.setPreferenceStore( getSCCorePreferenceStore() );
//        field.load();
//        return field;
//    }
    
    private SCIntegerFieldEditor createIntField (String preference, String name,String label, 
                String tooltip, Composite parent,
                    int lowValue, int highValue) {
        SCIntegerFieldEditor toText = new SCIntegerFieldEditor(preference, label, parent);
        if (tooltip != null) toText.setToolTipText(tooltip);
        GridData data = new GridData();
        data.widthHint = convertWidthInCharsToPixels(10);
        toText.getTextControl(parent).setLayoutData(data);
        toText.setPreferenceStore(getSCCorePreferenceStore());
        toText.setPage(this);
        toText.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        toText.setValidRange(lowValue,highValue);
        String minValue = Integer.toString(lowValue);
        String maxValue = Integer.toString(highValue);
        toText.setErrorMessage(MessageFormat.format(
            "The {2} value range is [{0},{1}]",  minValue, maxValue,name)); //$NON-NLS-1$
        toText.load();
        return toText;
    }


    /**
     * Store the preference values based on the state of the component widgets
     */
    private void storeValues () {
        fDebugTimeoutText.store();
        fLaunchTimeoutText.store();
        fLoadTimeoutText.store();
        fErrorLogLimitText.store();
        this.getSCCorePreferenceStore().setValue(ISeeCodeConstants.PREF_ENGINE_VERSION_MANAGEMENT, this.engineVersionStrategy);
        if (fLicensingAlert != null)
            this.fLicensingAlert.store();
        if (fExpirationDays != null)
            this.fExpirationDays.store();
    }

    private SCIntegerFieldEditor createTimeoutField (String preference, String name,String label, 
                String tooltip, Composite parent) {
        return createIntField(preference,name,label,tooltip,
                    parent,ISeeCodeConstants.MIN_REQUEST_TIMEOUT,ISeeCodeConstants.MAX_REQUEST_TIMEOUT);
    }


    protected SCPreferenceStore getSCCorePreferenceStore () {
        return fSCCorePreferenceStore;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
     */
    @Override
    public void dispose () {
        fDebugTimeoutText.dispose();
        fLaunchTimeoutText.dispose();
        fLoadTimeoutText.dispose();
        fErrorLogLimitText.dispose();
        super.dispose();
    }

    protected SCIntegerFieldEditor getLaunchTimeoutText () {
        return fLaunchTimeoutText;
    }

    protected SCIntegerFieldEditor getDebugTimeoutText () {
        return fDebugTimeoutText;
    }
    
    protected SCIntegerFieldEditor getLoadTimeoutText () {
        return fLoadTimeoutText;
    }


    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange (PropertyChangeEvent event) {
        // If the new value is true then we must check all field editors.
        // If it is false, then the page is invalid in any case.
       
        if (event.getProperty().equals(FieldEditor.IS_VALID)) {
            boolean newValue = ((Boolean) event.getNewValue()).booleanValue();
            SCIntegerFieldEditor launchTimeout = getLaunchTimeoutText();
            SCIntegerFieldEditor debugTimeout = getDebugTimeoutText();
            SCIntegerFieldEditor loadTimeout = getLoadTimeoutText();
            if (newValue) {
                if (event.getSource() != launchTimeout) {
                    launchTimeout.refreshValidState();
                }
                if (event.getSource() != debugTimeout) {
                    debugTimeout.refreshValidState();                   
                }
                if (event.getSource() != loadTimeout) {
                    loadTimeout.refreshValidState();
                }
                if (event.getSource() != fErrorLogLimitText) {
                    
                }
            }
            setValid(launchTimeout.isValid() && debugTimeout.isValid() && loadTimeout.isValid() && checkValues());
            updateLicensingEnablement();
            getContainer().updateButtons();
        }
        else {
            setValid(checkValues());
        }
    }
    
    private boolean checkValues(){
        SCIntegerFieldEditor debugTimeout = getDebugTimeoutText();
        SCIntegerFieldEditor loadTimeout = getLoadTimeoutText();
        if (debugTimeout.getIntValue() > loadTimeout.getIntValue()){
            this.setErrorMessage("Load timeout must not be less than engine timeout");
            return false;
        }
        return true;
    }
    
    private void updateLicensingEnablement(){      
        if (fExpirationDays != null) {
            boolean v = this.fLicensingAlert.getBooleanValue();
            this.fExpirationDays.setEnabled(v, fExpirationDaysParentComposite);
        }
    }

    protected boolean canClearErrorMessage () {
        SCIntegerFieldEditor launchTimeout = getLaunchTimeoutText();
        SCIntegerFieldEditor debugTimeout = getDebugTimeoutText();
        boolean validLaunch = false;
        boolean validDebug = false;
        if (launchTimeout != null) {
            validLaunch = launchTimeout.isValid();
        }
        if (debugTimeout != null) {
            validDebug = debugTimeout.isValid();
        }
        return validLaunch && validDebug;
    }
    
    private int engineVersionStrategy = ISeeCodeConstants.ENGINE_VERSION_PROMPT;
    private Button fPromptButton = null;

}
